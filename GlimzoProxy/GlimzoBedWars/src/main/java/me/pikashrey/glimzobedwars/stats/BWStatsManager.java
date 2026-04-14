package me.pikashrey.glimzobedwars.stats;

import me.pikashrey.glimzobedwars.GlimzoBedWars;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BWStatsManager {

    private final GlimzoBedWars plugin;
    private final Map<UUID, BWPlayerStats> cache = new ConcurrentHashMap<>();

    // Reuse GlimzoCore's datasource via JDBC directly.
    // We just need the same DB credentials from config.
    private Connection getConnection() throws SQLException {
        String host = plugin.getConfig().getString("database.host", "localhost");
        int    port = plugin.getConfig().getInt("database.port", 3306);
        String db   = plugin.getConfig().getString("database.name", "glimzo");
        String user = plugin.getConfig().getString("database.user", "root");
        String pass = plugin.getConfig().getString("database.password", "");
        return DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true",
                user, pass
        );
    }

    public BWStatsManager(GlimzoBedWars plugin) {
        this.plugin = plugin;
        createTable();
    }

    private void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = getConnection();
                 Statement st = con.createStatement()) {
                st.execute(
                    "CREATE TABLE IF NOT EXISTS bw_stats (" +
                    "  uuid         VARCHAR(36) PRIMARY KEY," +
                    "  wins         INT DEFAULT 0," +
                    "  losses       INT DEFAULT 0," +
                    "  kills        INT DEFAULT 0," +
                    "  final_kills  INT DEFAULT 0," +
                    "  deaths       INT DEFAULT 0," +
                    "  final_deaths INT DEFAULT 0," +
                    "  beds_broken  INT DEFAULT 0," +
                    "  games_played INT DEFAULT 0" +
                    ")"
                );
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to create bw_stats table: " + e.getMessage());
            }
        });
    }

    /** Load a player's stats into cache asynchronously. Call on join. */
    public void loadAsync(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM bw_stats WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                BWPlayerStats stats = new BWPlayerStats(uuid);
                if (rs.next()) {
                    stats.setWins(rs.getInt("wins"));
                    stats.setLosses(rs.getInt("losses"));
                    stats.setKills(rs.getInt("kills"));
                    stats.setFinalKills(rs.getInt("final_kills"));
                    stats.setDeaths(rs.getInt("deaths"));
                    stats.setFinalDeaths(rs.getInt("final_deaths"));
                    stats.setBedsBreoken(rs.getInt("beds_broken"));
                    stats.setGamesPlayed(rs.getInt("games_played"));
                }
                cache.put(uuid, stats);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load BW stats for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /** Save a player's stats asynchronously. Call on quit and game end. */
    public void saveAsync(UUID uuid) {
        BWPlayerStats stats = cache.get(uuid);
        if (stats == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO bw_stats (uuid, wins, losses, kills, final_kills, deaths, final_deaths, beds_broken, games_played) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "wins=VALUES(wins), losses=VALUES(losses), kills=VALUES(kills), " +
                     "final_kills=VALUES(final_kills), deaths=VALUES(deaths), " +
                     "final_deaths=VALUES(final_deaths), beds_broken=VALUES(beds_broken), " +
                     "games_played=VALUES(games_played)")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, stats.getWins());
                ps.setInt(3, stats.getLosses());
                ps.setInt(4, stats.getKills());
                ps.setInt(5, stats.getFinalKills());
                ps.setInt(6, stats.getDeaths());
                ps.setInt(7, stats.getFinalDeaths());
                ps.setInt(8, stats.getBedsBreoken());
                ps.setInt(9, stats.getGamesPlayed());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save BW stats for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public void unload(UUID uuid) {
        saveAsync(uuid);
        cache.remove(uuid);
    }

    /** Get cached stats. Returns a blank object if not loaded yet (safety fallback). */
    public BWPlayerStats get(UUID uuid) {
        return cache.getOrDefault(uuid, new BWPlayerStats(uuid));
    }
}
