package me.pikashrey.glimzocore.features.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.events.impl.PlayerPunishEvent;
import me.pikashrey.glimzocore.api.events.impl.PlayerUnPunishEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PunishmentManager {

    protected final GlimzoCore plugin;
    private final Map<UUID, List<Punishment>> cache = new ConcurrentHashMap<>();

    public PunishmentManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void loadPunishments(UUID uuid) {
        cache.put(uuid, new CopyOnWriteArrayList<>(fetchFromDb(uuid)));
    }

    public void loadPunishmentsFromData(UUID uuid, java.util.List<java.util.Map<String, Object>> rows) {
        List<Punishment> list = new CopyOnWriteArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            String staffStr = (String) row.get("staff_uuid");
            try {
                list.add(new Punishment(
                        (int) row.get("id"), uuid,
                        (String) row.get("target_name"),
                        staffStr != null ? UUID.fromString(staffStr) : null,
                        (String) row.get("staff_name"),
                        PunishmentType.valueOf((String) row.get("type")),
                        (String) row.get("reason"),
                        (long) row.get("issued_at"), (long) row.get("expires_at"),
                        (boolean) row.get("active"), (boolean) row.get("appealed")
                ));
            } catch (Exception ignored) {}
        }
        cache.put(uuid, list);
    }

    public void unloadPunishments(UUID uuid) { cache.remove(uuid); }

    public boolean punish(Punishment punishment) {
        PlayerPunishEvent event = new PlayerPunishEvent(punishment);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        cache.computeIfAbsent(punishment.getTargetUuid(), k -> new CopyOnWriteArrayList<>()).add(punishment);

        PlayerData data = GlobalPlayer.get(punishment.getTargetUuid());
        if (data != null) syncToPlayerData(data, punishment);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveToDb(punishment);
            if (punishment.getType().isBan()) {
                stampBanInDb(punishment);
            } else if (punishment.getType().isMute()) {
                stampMuteInDb(punishment);
            }
        });

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player online = Bukkit.getPlayer(punishment.getTargetUuid());
            if (online == null) return;
            if (punishment.getType().isBan()) {
                online.kickPlayer(buildBanScreen(punishment));
                plugin.getDiscordManager().sendBanNotification(
                        punishment.getTargetName(), punishment.getReason(), punishment.getStaffName());
            }
        });

        if (data != null) {
            if (punishment.getType().isBan())  data.getStats().incrementBans();
            if (punishment.getType().isMute()) data.getStats().incrementMutes();
            if (punishment.getType() == PunishmentType.WARN) data.getStats().incrementWarnings();
        }

        return true;
    }

    public void unban(UUID targetUuid, UUID staffUuid, String staffName) {
        deactivateByType(targetUuid, PunishmentType.BAN, PunishmentType.TEMP_BAN);

        PlayerData data = GlobalPlayer.get(targetUuid);
        if (data != null) {
            data.setBanned(false);
            data.setBanExpiry(-1);
            data.setBanReason(null);
        }

        Bukkit.getPluginManager().callEvent(buildUnpunishEvent(targetUuid, staffUuid, staffName, false));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            deactivateInDb(targetUuid, PunishmentType.BAN, PunishmentType.TEMP_BAN);
            clearBanFlagInDb(targetUuid);
        });
    }

    public void unmute(UUID targetUuid, UUID staffUuid, String staffName) {
        deactivateByType(targetUuid, PunishmentType.MUTE, PunishmentType.TEMP_MUTE);

        PlayerData data = GlobalPlayer.get(targetUuid);
        if (data != null) {
            data.setMuted(false);
            data.setMuteExpiry(-1);
            data.setMuteReason(null);
        }

        Bukkit.getPluginManager().callEvent(buildUnpunishEvent(targetUuid, staffUuid, staffName, true));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                deactivateInDb(targetUuid, PunishmentType.MUTE, PunishmentType.TEMP_MUTE));
    }

    public boolean isBanned(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) return data.isBanned();
        for (Punishment p : cache.getOrDefault(uuid, Collections.emptyList())) {
            if (p.getType().isBan() && p.isActive()) return true;
        }
        return false;
    }

    public boolean isMuted(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) return data.isMuted();
        for (Punishment p : cache.getOrDefault(uuid, Collections.emptyList())) {
            if (p.getType().isMute() && p.isActive()) return true;
        }
        return false;
    }

    public Punishment getActiveBan(UUID uuid)  { return getActiveByType(uuid, PunishmentType.BAN, PunishmentType.TEMP_BAN); }
    public Punishment getActiveMute(UUID uuid) { return getActiveByType(uuid, PunishmentType.MUTE, PunishmentType.TEMP_MUTE); }

    public List<Punishment> getHistory(UUID uuid) {
        return Collections.unmodifiableList(cache.getOrDefault(uuid, Collections.emptyList()));
    }

    public String buildBanScreen(Punishment p) {
        me.pikashrey.glimzocore.managers.ConfigManager cfg = GlimzoCore.getInstance().getConfigManager();
        String header    = cfg.getMessage("ban-screen.header",    "&c&lYou are banned.\n");
        String reasonFmt = cfg.getMessage("ban-screen.reason",    "&7Reason: &f{reason}");
        String permFmt   = cfg.getMessage("ban-screen.permanent", "&7Duration: &cPermanent");
        String expFmt    = cfg.getMessage("ban-screen.expires",   "&7Expires in: &f{duration}");
        String reason    = reasonFmt.replace("{reason}", p.getReason() != null ? p.getReason() : "none");
        String expiry    = p.isPermanent() ? permFmt : expFmt.replace("{duration}", formatExpiry(p.getExpiresAt()));
        return CC.translate(header + "\n" + reason + "\n&fBanned by: &e" + p.getStaffName() + "\n" + expiry);
    }

    public String buildMuteMessage(Punishment p) {
        me.pikashrey.glimzocore.managers.ConfigManager cfg = GlimzoCore.getInstance().getConfigManager();
        String header  = cfg.getMessage("mute-screen.header",    "&cYou are muted.");
        String permFmt = cfg.getMessage("mute-screen.permanent", "&7Duration: &cPermanent");
        String expFmt  = cfg.getMessage("mute-screen.expires",   "&7Expires: &f{expires}");
        String reason  = cfg.getMessage("mute-screen.reason",    "&7Reason: &f{reason}")
                .replace("{reason}", p.getReason() != null ? p.getReason() : "none");
        String expiry  = p.isPermanent() ? permFmt : expFmt.replace("{expires}", formatExpiry(p.getExpiresAt()));
        return CC.translate(header + "\n" + reason + "\n" + expiry);
    }

    private void syncToPlayerData(PlayerData data, Punishment p) {
        if (p.getType().isBan()) {
            data.setBanned(true);
            data.setBanExpiry(p.getExpiresAt());
            data.setBanReason(p.getReason());
        } else if (p.getType().isMute()) {
            data.setMuted(true);
            data.setMuteExpiry(p.getExpiresAt());
            data.setMuteReason(p.getReason());
        }
    }

    private void deactivateByType(UUID uuid, PunishmentType... types) {
        List<Punishment> list = cache.get(uuid);
        if (list == null) return;
        Set<PunishmentType> typeSet = new HashSet<>(Arrays.asList(types));
        for (Punishment p : list) {
            if (typeSet.contains(p.getType())) p.setActive(false);
        }
    }

    private Punishment getActiveByType(UUID uuid, PunishmentType... types) {
        Set<PunishmentType> typeSet = new HashSet<>(Arrays.asList(types));
        for (Punishment p : cache.getOrDefault(uuid, Collections.emptyList())) {
            if (typeSet.contains(p.getType()) && p.isActive()) return p;
        }
        return null;
    }

    private PlayerUnPunishEvent buildUnpunishEvent(UUID target, UUID staff, String staffName, boolean mute) {
        Punishment p = new Punishment(-1, target, "?", staff, staffName,
                mute ? PunishmentType.MUTE : PunishmentType.BAN,
                "revoked", System.currentTimeMillis(), -1, false, false);
        return new PlayerUnPunishEvent(p);
    }

    private String formatExpiry(long epochMs) {
        if (epochMs == -1) return "Permanent";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date(epochMs));
    }

    private List<Punishment> fetchFromDb(UUID uuid) {
        List<Punishment> list = new ArrayList<>();
        String sql = "SELECT * FROM glimzo_punishments WHERE target_uuid = ? ORDER BY issued_at DESC";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String staffStr = rs.getString("staff_uuid");
                    list.add(new Punishment(
                            rs.getInt("id"), uuid, rs.getString("target_name"),
                            staffStr != null ? UUID.fromString(staffStr) : null,
                            rs.getString("staff_name"),
                            PunishmentType.valueOf(rs.getString("type")),
                            rs.getString("reason"),
                            rs.getLong("issued_at"), rs.getLong("expires_at"),
                            rs.getBoolean("active"), rs.getBoolean("appealed")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[PunishmentManager] Failed to load punishments for " + uuid + ": " + e.getMessage());
        }
        return list;
    }

    private void saveToDb(Punishment p) {
        String sql = "INSERT INTO glimzo_punishments " +
                "(target_uuid, target_name, staff_uuid, staff_name, type, reason, issued_at, expires_at, active, appealed) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTargetUuid().toString());
            ps.setString(2, p.getTargetName());
            ps.setString(3, p.getStaffUuid() != null ? p.getStaffUuid().toString() : null);
            ps.setString(4, p.getStaffName());
            ps.setString(5, p.getType().name());
            ps.setString(6, p.getReason());
            ps.setLong(7, p.getIssuedAt());
            ps.setLong(8, p.getExpiresAt());
            ps.setBoolean(9, p.isActive());
            ps.setBoolean(10, p.isAppealed());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[PunishmentManager] Failed to save punishment: " + e.getMessage());
        }
    }

    private void stampBanInDb(Punishment p) {
        String sql = "UPDATE glimzo_players SET banned = 1, ban_expiry = ?, ban_reason = ? WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, p.getExpiresAt());
            ps.setString(2, p.getReason());
            ps.setString(3, p.getTargetUuid().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[PunishmentManager] Failed to stamp ban for " + p.getTargetUuid() + ": " + e.getMessage());
        }
    }

    private void stampMuteInDb(Punishment p) {
        String sql = "UPDATE glimzo_players SET muted = 1, mute_expiry = ?, mute_reason = ? WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, p.getExpiresAt());
            ps.setString(2, p.getReason());
            ps.setString(3, p.getTargetUuid().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[PunishmentManager] Failed to stamp mute for " + p.getTargetUuid() + ": " + e.getMessage());
        }
    }

    private void clearBanFlagInDb(UUID uuid) {
        String sql = "UPDATE glimzo_players SET banned = 0, ban_expiry = -1, ban_reason = NULL WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[PunishmentManager] Failed to clear ban flag for " + uuid + ": " + e.getMessage());
        }
    }

    private void deactivateInDb(UUID uuid, PunishmentType... types) {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < types.length; i++) placeholders.append(i == 0 ? "?" : ",?");
        String sql = "UPDATE glimzo_punishments SET active = 0 " +
                "WHERE target_uuid = ? AND type IN (" + placeholders + ") AND active = 1";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            for (int i = 0; i < types.length; i++) ps.setString(i + 2, types[i].name());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[PunishmentManager] Failed to deactivate punishments: " + e.getMessage());
        }
    }
}
