package me.pikashrey.glimzocore.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.player.PlayerSettings;
import me.pikashrey.glimzocore.api.player.PlayerStats;

import java.sql.*;
import java.util.UUID;

public class MySQLManager {

    private final GlimzoCore plugin;
    private HikariDataSource dataSource;

    public MySQLManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }


    public boolean connect() {
        try {
            String host     = plugin.getConfig().getString("mysql.host",     "localhost");
            int    port     = plugin.getConfig().getInt   ("mysql.port",     3306);
            String database = plugin.getConfig().getString("mysql.database", "glimzo").replace("\"", "").trim();
            String username = plugin.getConfig().getString("mysql.username", "root");
            String password = plugin.getConfig().getString("mysql.password", "");
            int    poolSize = plugin.getConfig().getInt   ("mysql.pool-size", 10);

            // MySQL 8.0 compatible connection string
            // Using mysql-connector-java 5.1.49 (Java 8 compatible) which doesn't check deprecated tx_isolation
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&rewriteBatchedStatements=true";

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30_000);
            config.setIdleTimeout(600_000);
            config.setMaxLifetime(1_800_000);
            // Ping idle connections every 60s to prevent them being dropped
            // by the server's wait_timeout (MySQL default: 8 hours, some hosts set lower)
            config.setKeepaliveTime(60_000);
            // Do NOT set connection test query - it may trigger isolation checks
            config.addDataSourceProperty("cachePrepStmts",          "true");
            config.addDataSourceProperty("prepStmtCacheSize",        "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit",    "2048");
            config.addDataSourceProperty("useServerPrepStmts",       "true");
            config.addDataSourceProperty("useUnicode",               "true");
            config.addDataSourceProperty("characterEncoding",        "UTF-8");
            config.addDataSourceProperty("allowLoadLocalInFile",    "false");
            config.addDataSourceProperty("allowLoadLocalInFileInPath", "/");
            config.setPoolName("GlimzoCore-Pool");

            try {
                dataSource = new HikariDataSource(config);
            } catch (Exception e) {
                // If pool init fails due to tx_isolation, retry with a delay
                if (e.getMessage().contains("tx_isolation")) {
                    plugin.log("&e[MySQL] Retrying connection after tx_isolation error...");
                    Thread.sleep(1000);
                    dataSource = new HikariDataSource(config);
                } else {
                    throw e;
                }
            }
            createTables();
            verifySelfTest();
            return true;
        } catch (Exception e) {
            plugin.log("&c[MySQL] Connection failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }


    public int getActiveConnections()         { return dataSource == null ? 0 : dataSource.getHikariPoolMXBean().getActiveConnections(); }
    public int getIdleConnections()           { return dataSource == null ? 0 : dataSource.getHikariPoolMXBean().getIdleConnections(); }
    public int getTotalConnections()          { return dataSource == null ? 0 : dataSource.getHikariPoolMXBean().getTotalConnections(); }
    public int getThreadsAwaitingConnection() { return dataSource == null ? 0 : dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(); }


    private void verifySelfTest() {
        java.util.Set<String> required = new java.util.LinkedHashSet<>(java.util.Arrays.asList(
                "glimzo_players", "glimzo_player_settings", "glimzo_player_stats",
                "glimzo_grants", "glimzo_punishments", "glimzo_clans", "glimzo_clan_members",
                "glimzo_friends", "glimzo_ignore", "glimzo_cosmetic_unlocks", "glimzo_achievements"
        ));
        try (Connection con = getConnection()) {
            try (Statement stmt = con.createStatement();
                 ResultSet rs   = stmt.executeQuery("SELECT 1")) { /* alive */ }

            java.sql.DatabaseMetaData meta = con.getMetaData();
            java.util.Set<String> existing = new java.util.HashSet<>();
            try (ResultSet rs = meta.getTables(null, null, "glimzo_%", new String[]{"TABLE"})) {
                while (rs.next()) existing.add(rs.getString("TABLE_NAME").toLowerCase());
            }
            boolean allOk = true;
            for (String table : required) {
                if (!existing.contains(table)) {
                    plugin.log("&c[MySQL] MISSING TABLE: " + table);
                    allOk = false;
                }
            }
            if (allOk) plugin.log("&a[MySQL] Self-test passed - all " + required.size() + " tables verified.");
        } catch (Exception e) {
            plugin.log("&c[MySQL] Self-test FAILED: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        // Each DDL gets its own Statement - reusing a Statement across DDLs can
        // cause issues with some MySQL 8.0 connector versions.
        try (Connection con = getConnection()) {
            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_players (" +
                    "uuid                    VARCHAR(36)  NOT NULL PRIMARY KEY," +
                    "name                    VARCHAR(16)  NOT NULL," +
                    "nick                    VARCHAR(32)  DEFAULT NULL," +
                    "nick_rank               VARCHAR(32)  DEFAULT NULL," +
                    "custom_prefix           VARCHAR(32)  DEFAULT NULL," +
                    "coins                   BIGINT       NOT NULL DEFAULT 0," +
                    "gems                    BIGINT       NOT NULL DEFAULT 0," +
                    "level                   INT          NOT NULL DEFAULT 1," +
                    "prestige                INT          NOT NULL DEFAULT 0," +
                    "experience              BIGINT       NOT NULL DEFAULT 0," +
                    "season_rank             INT          NOT NULL DEFAULT 0," +
                    "season_xp               BIGINT       NOT NULL DEFAULT 0," +
                    "season_pass_active      TINYINT(1)   NOT NULL DEFAULT 0," +
                    "banned                  TINYINT(1)   NOT NULL DEFAULT 0," +
                    "ban_expiry              BIGINT       NOT NULL DEFAULT -1," +
                    "ban_reason              VARCHAR(256) DEFAULT NULL," +
                    "muted                   TINYINT(1)   NOT NULL DEFAULT 0," +
                    "mute_expiry             BIGINT       NOT NULL DEFAULT -1," +
                    "mute_reason             VARCHAR(256) DEFAULT NULL," +
                    "clan_id                 VARCHAR(36)  DEFAULT NULL," +
                    "clan_role               INT          NOT NULL DEFAULT 0," +
                    "staff_mode              TINYINT(1)   NOT NULL DEFAULT 0," +
                    "vanished                TINYINT(1)   NOT NULL DEFAULT 0," +
                    "social_spy              TINYINT(1)   NOT NULL DEFAULT 0," +
                    "first_join              TINYINT(1)   NOT NULL DEFAULT 1," +
                    "first_join_time         BIGINT       NOT NULL DEFAULT 0," +
                    "last_seen_time          BIGINT       NOT NULL DEFAULT 0," +
                    "total_playtime_minutes  INT          NOT NULL DEFAULT 0," +
                    "fly_enabled             TINYINT(1)   NOT NULL DEFAULT 0," +
                    "INDEX idx_season_xp (season_xp)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_player_settings (" +
                    "uuid                     VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "private_messages         TINYINT(1)  NOT NULL DEFAULT 1," +
                    "friend_requests          TINYINT(1)  NOT NULL DEFAULT 1," +
                    "party_invites            TINYINT(1)  NOT NULL DEFAULT 1," +
                    "clan_invites             TINYINT(1)  NOT NULL DEFAULT 1," +
                    "scoreboard               TINYINT(1)  NOT NULL DEFAULT 1," +
                    "bossbar                  TINYINT(1)  NOT NULL DEFAULT 1," +
                    "player_visibility        TINYINT(1)  NOT NULL DEFAULT 1," +
                    "join_messages            TINYINT(1)  NOT NULL DEFAULT 1," +
                    "cosmetics                TINYINT(1)  NOT NULL DEFAULT 1," +
                    "other_cosmetics          TINYINT(1)  NOT NULL DEFAULT 1," +
                    "achievement_alerts       TINYINT(1)  NOT NULL DEFAULT 1," +
                    "level_up_alerts          TINYINT(1)  NOT NULL DEFAULT 1," +
                    "season_rank_up_alerts    TINYINT(1)  NOT NULL DEFAULT 1," +
                    "language                 VARCHAR(8)  NOT NULL DEFAULT 'en'," +
                    "FOREIGN KEY (uuid) REFERENCES glimzo_players(uuid) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_player_stats (" +
                    "uuid                        VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "total_logins                INT         NOT NULL DEFAULT 0," +
                    "total_playtime_minutes      BIGINT      NOT NULL DEFAULT 0," +
                    "total_coins_earned          BIGINT      NOT NULL DEFAULT 0," +
                    "total_coins_spent           BIGINT      NOT NULL DEFAULT 0," +
                    "total_gems_earned           BIGINT      NOT NULL DEFAULT 0," +
                    "total_gems_spent            BIGINT      NOT NULL DEFAULT 0," +
                    "highest_level               INT         NOT NULL DEFAULT 1," +
                    "total_prestiges             INT         NOT NULL DEFAULT 0," +
                    "total_xp_earned             BIGINT      NOT NULL DEFAULT 0," +
                    "highest_season_rank         INT         NOT NULL DEFAULT 0," +
                    "seasons_completed           INT         NOT NULL DEFAULT 0," +
                    "total_season_xp_earned      BIGINT      NOT NULL DEFAULT 0," +
                    "total_friends_added         INT         NOT NULL DEFAULT 0," +
                    "total_parties_created       INT         NOT NULL DEFAULT 0," +
                    "total_clans_joined          INT         NOT NULL DEFAULT 0," +
                    "total_achievements_unlocked INT         NOT NULL DEFAULT 0," +
                    "total_cosmetics_unlocked    INT         NOT NULL DEFAULT 0," +
                    "total_bans                  INT         NOT NULL DEFAULT 0," +
                    "total_mutes                 INT         NOT NULL DEFAULT 0," +
                    "total_warnings              INT         NOT NULL DEFAULT 0," +
                    "FOREIGN KEY (uuid) REFERENCES glimzo_players(uuid) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_grants (" +
                    "id              INT         NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "target_uuid     VARCHAR(36) NOT NULL," +
                    "target_name     VARCHAR(16) NOT NULL," +
                    "issued_by_uuid  VARCHAR(36) DEFAULT NULL," +
                    "issued_by_name  VARCHAR(32) NOT NULL," +
                    "rank_id         VARCHAR(32) NOT NULL," +
                    "issued_at       BIGINT      NOT NULL," +
                    "expires_at      BIGINT      NOT NULL DEFAULT -1," +
                    "staff_grant     TINYINT(1)  NOT NULL DEFAULT 0," +
                    "active          TINYINT(1)  NOT NULL DEFAULT 1," +
                    "INDEX idx_target (target_uuid)," +
                    "INDEX idx_active (target_uuid, active)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_rank_logs (" +
                    "id          INT         NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "staff_uuid  VARCHAR(36) DEFAULT NULL," +
                    "target_uuid VARCHAR(36) NOT NULL," +
                    "rank_id     VARCHAR(32) NOT NULL," +
                    "action      VARCHAR(16) NOT NULL," +
                    "server      VARCHAR(32) NOT NULL," +
                    "timestamp   BIGINT      NOT NULL," +
                    "INDEX idx_target (target_uuid)," +
                    "INDEX idx_time   (timestamp)," +
                    "INDEX idx_ts_srv (timestamp, server)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_clans (" +
                    "id          VARCHAR(36)  NOT NULL PRIMARY KEY," +
                    "name        VARCHAR(32)  NOT NULL UNIQUE," +
                    "tag         VARCHAR(8)   NOT NULL UNIQUE," +
                    "description VARCHAR(256) DEFAULT ''," +
                    "motd        VARCHAR(256) DEFAULT ''," +
                    "leader_uuid VARCHAR(36)  NOT NULL," +
                    "leader_name VARCHAR(16)  NOT NULL," +
                    "level       INT          NOT NULL DEFAULT 1," +
                    "xp          BIGINT       NOT NULL DEFAULT 0," +
                    "created_at  BIGINT       NOT NULL," +
                    "INDEX idx_xp (xp)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_clan_members (" +
                    "clan_id   VARCHAR(36) NOT NULL," +
                    "uuid      VARCHAR(36) NOT NULL," +
                    "name      VARCHAR(16) NOT NULL," +
                    "role      INT         NOT NULL DEFAULT 0," +
                    "joined_at BIGINT      NOT NULL," +
                    "PRIMARY KEY (clan_id, uuid)," +
                    "INDEX idx_uuid (uuid)," +
                    "FOREIGN KEY (clan_id) REFERENCES glimzo_clans(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_punishments (" +
                    "id          INT         NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "target_uuid VARCHAR(36) NOT NULL," +
                    "target_name VARCHAR(16) NOT NULL," +
                    "staff_uuid  VARCHAR(36) DEFAULT NULL," +
                    "staff_name  VARCHAR(32) NOT NULL," +
                    "type        VARCHAR(16) NOT NULL," +
                    "reason      VARCHAR(256) NOT NULL," +
                    "issued_at   BIGINT      NOT NULL," +
                    "expires_at  BIGINT      NOT NULL DEFAULT -1," +
                    "active      TINYINT(1)  NOT NULL DEFAULT 1," +
                    "appealed    TINYINT(1)  NOT NULL DEFAULT 0," +
                    "INDEX idx_target  (target_uuid)," +
                    "INDEX idx_active  (target_uuid, active)," +
                    "INDEX idx_history (target_uuid, issued_at)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_friends (" +
                    "uuid        VARCHAR(36) NOT NULL," +
                    "friend_uuid VARCHAR(36) NOT NULL," +
                    "created_at  BIGINT      NOT NULL DEFAULT 0," +
                    "PRIMARY KEY (uuid, friend_uuid)," +
                    "INDEX idx_uuid (uuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_ignore (" +
                    "uuid         VARCHAR(36) NOT NULL," +
                    "ignored_uuid VARCHAR(36) NOT NULL," +
                    "PRIMARY KEY (uuid, ignored_uuid)," +
                    "INDEX idx_uuid (uuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_cosmetic_unlocks (" +
                    "uuid         VARCHAR(36) NOT NULL," +
                    "cosmetic_id  VARCHAR(64) NOT NULL," +
                    "unlocked_at  BIGINT      NOT NULL," +
                    "source       VARCHAR(32) NOT NULL DEFAULT 'admin'," +
                    "PRIMARY KEY (uuid, cosmetic_id)," +
                    "INDEX idx_uuid (uuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_achievements (" +
                    "uuid           VARCHAR(36) NOT NULL," +
                    "achievement_id VARCHAR(64) NOT NULL," +
                    "unlocked_at    BIGINT      NOT NULL," +
                    "PRIMARY KEY (uuid, achievement_id)," +
                    "INDEX idx_uuid (uuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_social_events (" +
                    "id         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "event_type VARCHAR(32) NOT NULL," +
                    "payload    VARCHAR(256) NOT NULL," +
                    "server_id  VARCHAR(64) NOT NULL," +
                    "created_at BIGINT      NOT NULL," +
                    "INDEX idx_created (created_at)," +
                    "INDEX idx_type    (event_type, created_at)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_player_ips (" +
                    "uuid      VARCHAR(36) NOT NULL," +
                    "ip        VARCHAR(45) NOT NULL," +
                    "last_seen BIGINT      NOT NULL," +
                    "PRIMARY KEY (uuid, ip)," +
                    "INDEX idx_ip (ip)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            exec(con, "CREATE TABLE IF NOT EXISTS glimzo_mail (" +
                    "id             INT          NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "sender_uuid    VARCHAR(36)  NOT NULL," +
                    "sender_name    VARCHAR(16)  NOT NULL," +
                    "recipient_uuid VARCHAR(36)  NOT NULL," +
                    "subject        VARCHAR(128) NOT NULL DEFAULT ''," +
                    "body           TEXT         NOT NULL," +
                    "sent_at        BIGINT       NOT NULL," +
                    "read_at        BIGINT       NOT NULL DEFAULT -1," +
                    "INDEX idx_recipient      (recipient_uuid)," +
                    "INDEX idx_rcpt_date      (recipient_uuid, sent_at)," +
                    "INDEX idx_rcpt_unread    (recipient_uuid, read_at)," +
                    "INDEX idx_sender         (sender_uuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        }
    }

    /** Execute a single DDL statement on its own Statement object. */
    private void exec(Connection con, String sql) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        }
    }

    //
    // Runs 6 SELECT queries on a SINGLE pooled connection instead of 6 separate
    // checkouts.  Called from PlayerJoinListener.  Returns a JoinData record that
    // PlayerJoinListener then distributes to each manager's in-memory cache.
    //
    // This halves the HikariCP checkout/return overhead on every player join.

    public static class JoinData {
        public final java.util.List<java.util.Map<String, Object>> grants      = new java.util.ArrayList<>();
        public final java.util.List<java.util.Map<String, Object>> punishments = new java.util.ArrayList<>();
        public final java.util.Set<String>                          friendUuids = new java.util.HashSet<>();
        public final java.util.Set<String>                          achievementIds = new java.util.HashSet<>();
        public final java.util.Set<String>                          ignoredUuids = new java.util.HashSet<>();
        public final java.util.Map<String, String>                  cosmeticState = new java.util.HashMap<>();
    }

    public JoinData loadJoinData(UUID uuid) {
        JoinData data = new JoinData();
        String uuidStr = uuid.toString();

        try (Connection con = getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM glimzo_grants WHERE target_uuid = ?")) {
                ps.setString(1, uuidStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                        row.put("id",            rs.getInt("id"));
                        row.put("rank_id",        rs.getString("rank_id"));
                        row.put("issued_by_uuid", rs.getString("issued_by_uuid"));
                        row.put("issued_by_name", rs.getString("issued_by_name"));
                        row.put("issued_at",      rs.getLong("issued_at"));
                        row.put("expires_at",     rs.getLong("expires_at"));
                        row.put("staff_grant",    rs.getBoolean("staff_grant"));
                        row.put("active",         rs.getBoolean("active"));
                        row.put("target_name",    rs.getString("target_name"));
                        data.grants.add(row);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM glimzo_punishments WHERE target_uuid = ? ORDER BY issued_at DESC")) {
                ps.setString(1, uuidStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                        row.put("id",          rs.getInt("id"));
                        row.put("target_name", rs.getString("target_name"));
                        row.put("staff_uuid",  rs.getString("staff_uuid"));
                        row.put("staff_name",  rs.getString("staff_name"));
                        row.put("type",        rs.getString("type"));
                        row.put("reason",      rs.getString("reason"));
                        row.put("issued_at",   rs.getLong("issued_at"));
                        row.put("expires_at",  rs.getLong("expires_at"));
                        row.put("active",      rs.getBoolean("active"));
                        row.put("appealed",    rs.getBoolean("appealed"));
                        data.punishments.add(row);
                    }
                }
            }

            // 3 - Friends
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT friend_uuid FROM glimzo_friends WHERE uuid = ?")) {
                ps.setString(1, uuidStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) data.friendUuids.add(rs.getString("friend_uuid"));
                }
            }

            // 4 - Achievements
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT achievement_id FROM glimzo_achievements WHERE uuid = ?")) {
                ps.setString(1, uuidStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) data.achievementIds.add(rs.getString("achievement_id"));
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT ignored_uuid FROM glimzo_ignore WHERE uuid = ?")) {
                ps.setString(1, uuidStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) data.ignoredUuids.add(rs.getString("ignored_uuid"));
                }
            }

            // 6 - Cosmetic state
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM glimzo_player_cosmetics WHERE uuid = ?")) {
                ps.setString(1, uuidStr);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String[] cols = {"active_aura","active_wings","active_ally",
                                         "active_morph","active_chat_tag",
                                         "active_join_effect","active_join_message"};
                        for (String col : cols) data.cosmeticState.put(col, rs.getString(col));
                    }
                }
            }

        } catch (SQLException e) {
            plugin.log("&c[MySQL] Batched join load failed for " + uuid + ": " + e.getMessage());
        }
        return data;
    }


    public void logSocialEvent(String eventType, String payload, String serverId) {
        String sql = "INSERT INTO glimzo_social_events (event_type, payload, server_id, created_at) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setString(2, payload);
            ps.setString(3, serverId);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[MySQL] logSocialEvent failed: " + e.getMessage());
        }
    }

    public java.util.List<String[]> pollSocialEvents(long sinceTimestamp, String excludeServer) {
        java.util.List<String[]> events = new java.util.ArrayList<>();
        String sql = "SELECT event_type, payload, created_at FROM glimzo_social_events " +
                     "WHERE created_at > ? AND server_id <> ? ORDER BY created_at ASC LIMIT 200";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, sinceTimestamp);
            ps.setString(2, excludeServer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new String[]{
                            rs.getString("event_type"),
                            rs.getString("payload"),
                            String.valueOf(rs.getLong("created_at"))
                    });
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[MySQL] pollSocialEvents failed: " + e.getMessage());
        }
        return events;
    }

    public void logIp(UUID uuid, String ip) {
        String sql = "INSERT INTO glimzo_player_ips (uuid, ip, last_seen) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE last_seen = VALUES(last_seen)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, ip);
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[MySQL] IP log failed for " + uuid + ": " + e.getMessage());
        }
    }

    public java.util.List<String> getAlts(UUID uuid) {
        java.util.List<String> alts = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT p.name FROM glimzo_player_ips a " +
                     "JOIN glimzo_player_ips b ON a.ip = b.ip " +
                     "JOIN glimzo_players p ON b.uuid = p.uuid " +
                     "WHERE a.uuid = ? AND b.uuid != ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) alts.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            plugin.log("&c[MySQL] getAlts failed for " + uuid + ": " + e.getMessage());
        }
        return alts;
    }


    public PlayerData loadPlayer(UUID uuid, String name) {
        PlayerData data = new PlayerData(uuid, name);

        try (Connection con = getConnection()) {

            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM glimzo_players WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.setNick(rs.getString("nick"));
                        data.setNickRank(rs.getString("nick_rank"));
                        data.setCustomPrefix(rs.getString("custom_prefix"));
                        data.setCoins(rs.getLong("coins"));
                        data.setGems(rs.getLong("gems"));
                        data.setLevel(rs.getInt("level"));
                        data.setPrestige(rs.getInt("prestige"));
                        data.setExperience(rs.getLong("experience"));
                        data.setSeasonRank(rs.getInt("season_rank"));
                        data.setSeasonXp(rs.getLong("season_xp"));
                        data.setSeasonPassActive(rs.getBoolean("season_pass_active"));

                        // Load raw ban/mute state - PlayerData.isBanned()/isMuted() will
                        // check expiry at runtime, so we load the stored flag as-is.
                        data.setBanned(rs.getBoolean("banned"));
                        data.setBanExpiry(rs.getLong("ban_expiry"));
                        data.setBanReason(rs.getString("ban_reason"));
                        data.setMuted(rs.getBoolean("muted"));
                        data.setMuteExpiry(rs.getLong("mute_expiry"));
                        data.setMuteReason(rs.getString("mute_reason"));

                        data.setClanId(rs.getString("clan_id"));
                        data.setClanRole(rs.getInt("clan_role"));
                        data.setStaffMode(false);  // always reset on join
                        data.setVanished(false);    // always reset on join
                        data.setSocialSpyEnabled(rs.getBoolean("social_spy"));
                        data.setFirstJoin(rs.getBoolean("first_join"));
                        data.setFirstJoinTime(rs.getLong("first_join_time"));
                        data.setLastSeenTime(rs.getLong("last_seen_time"));
                        data.setTotalPlaytimeMinutes(rs.getInt("total_playtime_minutes"));
                        data.setFlyEnabled(rs.getBoolean("fly_enabled"));
                    } else {
                        insertNewPlayer(con, uuid, name);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM glimzo_player_settings WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        PlayerSettings s = data.getSettings();
                        s.setPrivateMessagesEnabled(rs.getBoolean("private_messages"));
                        s.setFriendRequestsEnabled(rs.getBoolean("friend_requests"));
                        s.setPartyInvitesEnabled(rs.getBoolean("party_invites"));
                        s.setClanInvitesEnabled(rs.getBoolean("clan_invites"));
                        s.setScoreboardEnabled(rs.getBoolean("scoreboard"));
                        s.setBossbarEnabled(rs.getBoolean("bossbar"));
                        s.setPlayerVisibilityEnabled(rs.getBoolean("player_visibility"));
                        s.setJoinMessagesEnabled(rs.getBoolean("join_messages"));
                        s.setCosmeticsEnabled(rs.getBoolean("cosmetics"));
                        s.setOtherCosmeticsEnabled(rs.getBoolean("other_cosmetics"));
                        s.setAchievementAlertsEnabled(rs.getBoolean("achievement_alerts"));
                        s.setLevelUpAlertsEnabled(rs.getBoolean("level_up_alerts"));
                        s.setSeasonRankUpAlertsEnabled(rs.getBoolean("season_rank_up_alerts"));
                        s.setLanguage(rs.getString("language"));
                    } else {
                        insertNewSettings(con, uuid);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM glimzo_player_stats WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        PlayerStats st = data.getStats();
                        st.setTotalLogins(rs.getInt("total_logins"));
                        st.setTotalPlaytimeMinutes(rs.getLong("total_playtime_minutes"));
                        st.setTotalCoinsEarned(rs.getLong("total_coins_earned"));
                        st.setTotalCoinsSpent(rs.getLong("total_coins_spent"));
                        st.setTotalGemsEarned(rs.getLong("total_gems_earned"));
                        st.setTotalGemsSpent(rs.getLong("total_gems_spent"));
                        st.setHighestLevel(rs.getInt("highest_level"));
                        st.setTotalPrestiges(rs.getInt("total_prestiges"));
                        st.setTotalXPEarned(rs.getLong("total_xp_earned"));
                        st.setHighestSeasonRank(rs.getInt("highest_season_rank"));
                        st.setSeasonsCompleted(rs.getInt("seasons_completed"));
                        st.setTotalSeasonXPEarned(rs.getLong("total_season_xp_earned"));
                        st.setTotalFriendsAdded(rs.getInt("total_friends_added"));
                        st.setTotalPartiesCreated(rs.getInt("total_parties_created"));
                        st.setTotalClansJoined(rs.getInt("total_clans_joined"));
                        st.setTotalAchievementsUnlocked(rs.getInt("total_achievements_unlocked"));
                        st.setTotalCosmeticsUnlocked(rs.getInt("total_cosmetics_unlocked"));
                        st.setTotalBans(rs.getInt("total_bans"));
                        st.setTotalMutes(rs.getInt("total_mutes"));
                        st.setTotalWarnings(rs.getInt("total_warnings"));
                    } else {
                        insertNewStats(con, uuid);
                    }
                }
            }

        } catch (SQLException e) {
            plugin.log("&c[MySQL] Failed to load player " + name + ": " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }


    public void savePlayer(PlayerData data) {
        try (Connection con = getConnection()) {
            saveMainRow(con, data);
            saveSettings(con, data);
            saveStats(con, data);
        } catch (SQLException e) {
            plugin.log("&c[MySQL] Failed to save player " + data.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveMainRow(Connection con, PlayerData d) throws SQLException {
        // Use row alias (MySQL 8.0.19+) instead of deprecated VALUES() function
        String sql =
            "INSERT INTO glimzo_players " +
            "(uuid, name, nick, nick_rank, custom_prefix, coins, gems, level, prestige, experience, " +
            " season_rank, season_xp, season_pass_active, banned, ban_expiry, ban_reason, " +
            " muted, mute_expiry, mute_reason, clan_id, clan_role, staff_mode, vanished, " +
            " social_spy, first_join, first_join_time, last_seen_time, total_playtime_minutes, fly_enabled) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) AS new_row " +
            "ON DUPLICATE KEY UPDATE " +
            "name=new_row.name, nick=new_row.nick, nick_rank=new_row.nick_rank, custom_prefix=new_row.custom_prefix, " +
            "coins=new_row.coins, gems=new_row.gems, level=new_row.level, " +
            "prestige=new_row.prestige, experience=new_row.experience, " +
            "season_rank=new_row.season_rank, season_xp=new_row.season_xp, " +
            "season_pass_active=new_row.season_pass_active, " +
            "banned=new_row.banned, ban_expiry=new_row.ban_expiry, ban_reason=new_row.ban_reason, " +
            "muted=new_row.muted, mute_expiry=new_row.mute_expiry, mute_reason=new_row.mute_reason, " +
            "clan_id=new_row.clan_id, clan_role=new_row.clan_role, " +
            "staff_mode=new_row.staff_mode, vanished=new_row.vanished, social_spy=new_row.social_spy, " +
            "first_join=new_row.first_join, first_join_time=new_row.first_join_time, " +
            "last_seen_time=new_row.last_seen_time, total_playtime_minutes=new_row.total_playtime_minutes, fly_enabled=new_row.fly_enabled";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,  d.getUuid().toString());
            ps.setString(2,  d.getName());
            ps.setString(3,  d.getNick());
            ps.setString(4,  d.getNickRank());
            ps.setString(5,  d.getCustomPrefix());
            ps.setLong(6,    d.getCoins());
            ps.setLong(7,    d.getGems());
            ps.setInt(8,     d.getLevel());
            ps.setInt(9,     d.getPrestige());
            ps.setLong(10,   d.getExperience());
            ps.setInt(11,    d.getSeasonRank());
            ps.setLong(12,   d.getSeasonXp());
            ps.setBoolean(13, d.hasSeasonPass());
            ps.setBoolean(14, d.isBanned());
            ps.setLong(15,   d.getBanExpiry());
            ps.setString(16, d.getBanReason());
            ps.setBoolean(17, d.isMuted());
            ps.setLong(18,   d.getMuteExpiry());
            ps.setString(19, d.getMuteReason());
            ps.setString(20, d.getClanId());
            ps.setInt(21,    d.getClanRole());
            ps.setBoolean(22, d.isStaffMode());
            ps.setBoolean(23, d.isVanished());
            ps.setBoolean(24, d.isSocialSpyEnabled());
            ps.setBoolean(25, d.isFirstJoin());
            ps.setLong(26,   d.getFirstJoinTime());
            ps.setLong(27,   d.getLastSeenTime());
            ps.setInt(28,    d.getTotalPlaytimeMinutes());
            ps.setBoolean(29, d.isFlyEnabled());
            ps.executeUpdate();
        }
    }

    private void saveSettings(Connection con, PlayerData d) throws SQLException {
        PlayerSettings s = d.getSettings();
        String sql =
            "INSERT INTO glimzo_player_settings " +
            "(uuid, private_messages, friend_requests, party_invites, clan_invites, scoreboard, " +
            " bossbar, player_visibility, join_messages, cosmetics, other_cosmetics, " +
            " achievement_alerts, level_up_alerts, season_rank_up_alerts, language) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) AS new_row " +
            "ON DUPLICATE KEY UPDATE " +
            "private_messages=new_row.private_messages, friend_requests=new_row.friend_requests, " +
            "party_invites=new_row.party_invites, clan_invites=new_row.clan_invites, " +
            "scoreboard=new_row.scoreboard, bossbar=new_row.bossbar, " +
            "player_visibility=new_row.player_visibility, join_messages=new_row.join_messages, " +
            "cosmetics=new_row.cosmetics, other_cosmetics=new_row.other_cosmetics, " +
            "achievement_alerts=new_row.achievement_alerts, level_up_alerts=new_row.level_up_alerts, " +
            "season_rank_up_alerts=new_row.season_rank_up_alerts, language=new_row.language";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,   d.getUuid().toString());
            ps.setBoolean(2,  s.isPrivateMessagesEnabled());
            ps.setBoolean(3,  s.isFriendRequestsEnabled());
            ps.setBoolean(4,  s.isPartyInvitesEnabled());
            ps.setBoolean(5,  s.isClanInvitesEnabled());
            ps.setBoolean(6,  s.isScoreboardEnabled());
            ps.setBoolean(7,  s.isBossbarEnabled());
            ps.setBoolean(8,  s.isPlayerVisibilityEnabled());
            ps.setBoolean(9,  s.isJoinMessagesEnabled());
            ps.setBoolean(10, s.isCosmeticsEnabled());
            ps.setBoolean(11, s.isOtherCosmeticsEnabled());
            ps.setBoolean(12, s.isAchievementAlertsEnabled());
            ps.setBoolean(13, s.isLevelUpAlertsEnabled());
            ps.setBoolean(14, s.isSeasonRankUpAlertsEnabled());
            ps.setString(15,  s.getLanguage());
            ps.executeUpdate();
        }
    }

    private void saveStats(Connection con, PlayerData d) throws SQLException {
        PlayerStats st = d.getStats();
        String sql =
            "INSERT INTO glimzo_player_stats " +
            "(uuid, total_logins, total_playtime_minutes, total_coins_earned, total_coins_spent, " +
            " total_gems_earned, total_gems_spent, highest_level, total_prestiges, total_xp_earned, " +
            " highest_season_rank, seasons_completed, total_season_xp_earned, total_friends_added, " +
            " total_parties_created, total_clans_joined, total_achievements_unlocked, " +
            " total_cosmetics_unlocked, total_bans, total_mutes, total_warnings) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) AS new_row " +
            "ON DUPLICATE KEY UPDATE " +
            "total_logins=new_row.total_logins, total_playtime_minutes=new_row.total_playtime_minutes, " +
            "total_coins_earned=new_row.total_coins_earned, total_coins_spent=new_row.total_coins_spent, " +
            "total_gems_earned=new_row.total_gems_earned, total_gems_spent=new_row.total_gems_spent, " +
            "highest_level=new_row.highest_level, total_prestiges=new_row.total_prestiges, " +
            "total_xp_earned=new_row.total_xp_earned, highest_season_rank=new_row.highest_season_rank, " +
            "seasons_completed=new_row.seasons_completed, " +
            "total_season_xp_earned=new_row.total_season_xp_earned, " +
            "total_friends_added=new_row.total_friends_added, " +
            "total_parties_created=new_row.total_parties_created, " +
            "total_clans_joined=new_row.total_clans_joined, " +
            "total_achievements_unlocked=new_row.total_achievements_unlocked, " +
            "total_cosmetics_unlocked=new_row.total_cosmetics_unlocked, " +
            "total_bans=new_row.total_bans, total_mutes=new_row.total_mutes, " +
            "total_warnings=new_row.total_warnings";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,  d.getUuid().toString());
            ps.setInt(2,     st.getTotalLogins());
            ps.setLong(3,    st.getTotalPlaytimeMinutes());
            ps.setLong(4,    st.getTotalCoinsEarned());
            ps.setLong(5,    st.getTotalCoinsSpent());
            ps.setLong(6,    st.getTotalGemsEarned());
            ps.setLong(7,    st.getTotalGemsSpent());
            ps.setInt(8,     st.getHighestLevel());
            ps.setInt(9,     st.getTotalPrestiges());
            ps.setLong(10,   st.getTotalXPEarned());
            ps.setInt(11,    st.getHighestSeasonRank());
            ps.setInt(12,    st.getSeasonsCompleted());
            ps.setLong(13,   st.getTotalSeasonXPEarned());
            ps.setInt(14,    st.getTotalFriendsAdded());
            ps.setInt(15,    st.getTotalPartiesCreated());
            ps.setInt(16,    st.getTotalClansJoined());
            ps.setInt(17,    st.getTotalAchievementsUnlocked());
            ps.setInt(18,    st.getTotalCosmeticsUnlocked());
            ps.setInt(19,    st.getTotalBans());
            ps.setInt(20,    st.getTotalMutes());
            ps.setInt(21,    st.getTotalWarnings());
            ps.executeUpdate();
        }
    }


    private void insertNewPlayer(Connection con, UUID uuid, String name) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO glimzo_players (uuid, name) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private void insertNewSettings(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO glimzo_player_settings (uuid) VALUES (?)")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    private void insertNewStats(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO glimzo_player_stats (uuid) VALUES (?)")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }


    public void logRankAction(UUID staffUuid, UUID targetUuid,
                              String rankId, String action,
                              long timestamp, String server) {
        String sql = "INSERT INTO glimzo_rank_logs (staff_uuid, target_uuid, rank_id, action, server, timestamp) " +
                     "VALUES (?,?,?,?,?,?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffUuid != null ? staffUuid.toString() : null);
            ps.setString(2, targetUuid.toString());
            ps.setString(3, rankId);
            ps.setString(4, action);
            ps.setString(5, server);
            ps.setLong(6, timestamp);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[MySQL] Failed to log rank action: " + e.getMessage());
        }
    }
}
