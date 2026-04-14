package me.pikashrey.glimzocore120.player;

import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore120.GlimzoCore120;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager120 {

    private final GlimzoCore120 plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    public void loadAsync(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData data = loadFromDb(player.getUniqueId(), player.getName());
            cache.put(player.getUniqueId(), data);
            // Update tab/scoreboard/chat once data is loaded
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getTablistManager().update(player);
                    plugin.getScoreboardManager().setup(player);
                    plugin.getChatManager().applyNameTag(player);
                }
            });
        });
    }

    public void saveAsync(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveToDb(data));
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            saveToDb(data);
        }
    }

    public void unload(UUID uuid) {
        saveAsync(uuid);
        cache.remove(uuid);
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerData get(Player player) {
        return cache.get(player.getUniqueId());
    }


    private PlayerData loadFromDb(UUID uuid, String name) {
        try (Connection con = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            PlayerData data = new PlayerData(uuid, name);
            if (rs.next()) {
                data.setCoins(rs.getLong("coins"));
                data.setGems(rs.getLong("gems"));
                data.setLevel(rs.getInt("level"));
                data.setPrestige(rs.getInt("prestige"));
                data.setExperience(rs.getLong("experience"));
                data.setRankId(rs.getString("rank_id"));
                data.setBanned(rs.getBoolean("banned"));
                data.setMuted(rs.getBoolean("muted"));
                data.setBanExpiry(rs.getLong("ban_expiry"));
                data.setMuteExpiry(rs.getLong("mute_expiry"));
                data.setBanReason(rs.getString("ban_reason"));
                data.setMuteReason(rs.getString("mute_reason"));
            }
            return data;
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load player " + name + ": " + e.getMessage());
            return new PlayerData(uuid, name);
        }
    }

    private void saveToDb(PlayerData data) {
        try (Connection con = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO players (uuid, name, coins, gems, level, prestige, experience, rank_id, " +
                     "banned, muted, ban_expiry, mute_expiry, ban_reason, mute_reason) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "name=VALUES(name), coins=VALUES(coins), gems=VALUES(gems), " +
                     "level=VALUES(level), prestige=VALUES(prestige), experience=VALUES(experience), " +
                     "rank_id=VALUES(rank_id), banned=VALUES(banned), muted=VALUES(muted), " +
                     "ban_expiry=VALUES(ban_expiry), mute_expiry=VALUES(mute_expiry), " +
                     "ban_reason=VALUES(ban_reason), mute_reason=VALUES(mute_reason)")) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getName());
            ps.setLong(3, data.getCoins());
            ps.setLong(4, data.getGems());
            ps.setInt(5, data.getLevel());
            ps.setInt(6, data.getPrestige());
            ps.setLong(7, data.getExperience());
            ps.setString(8, data.getRankId());
            ps.setBoolean(9, data.isBanned());
            ps.setBoolean(10, data.isMuted());
            ps.setLong(11, data.getBanExpiry());
            ps.setLong(12, data.getMuteExpiry());
            ps.setString(13, data.getBanReason());
            ps.setString(14, data.getMuteReason());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save player " + data.getName() + ": " + e.getMessage());
        }
    }

    public Collection<PlayerData> getAll() {
        return cache.values();
    }
}
