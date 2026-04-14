package me.pikashrey.glimzocore.features.cosmetics;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.cosmetic.CosmeticUnlock;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticUnlockManager {

    protected final GlimzoCore plugin;

    // UUID -> set of unlocked cosmetic ids
    private final Map<UUID, Set<String>> unlocks = new ConcurrentHashMap<>();

    public CosmeticUnlockManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void loadUnlocks(UUID uuid) {
        unlocks.put(uuid, fetchFromDb(uuid));
    }

    public void unloadUnlocks(UUID uuid) {
        unlocks.remove(uuid);
    }

    // Unlock management

    public boolean hasUnlock(UUID uuid, String cosmeticId) {
        Set<String> set = unlocks.get(uuid);
        return set != null && set.contains(cosmeticId);
    }

    public Set<String> getUnlocks(UUID uuid) {
        return Collections.unmodifiableSet(unlocks.getOrDefault(uuid, Collections.emptySet()));
    }

    public void grantUnlock(UUID uuid, String cosmeticId, String source) {
        if (hasUnlock(uuid, cosmeticId)) return;
        unlocks.computeIfAbsent(uuid, k -> new HashSet<>()).add(cosmeticId);

        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) data.getStats().incrementCosmeticsUnlocked();

        long now = System.currentTimeMillis();
        CosmeticUnlock unlock = new CosmeticUnlock(uuid, cosmeticId, now, source, false);
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveToDb(unlock));
    }

    public void revokeUnlock(UUID uuid, String cosmeticId) {
        Set<String> set = unlocks.get(uuid);
        if (set != null) set.remove(cosmeticId);
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> removeFromDb(uuid, cosmeticId));
    }

    // Database

    private Set<String> fetchFromDb(UUID uuid) {
        Set<String> set = new HashSet<>();
        String sql = "SELECT cosmetic_id FROM glimzo_cosmetic_unlocks WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getString("cosmetic_id"));
            }
        } catch (SQLException e) {
            plugin.log("&c[CosmeticUnlockManager] Failed to load unlocks: " + e.getMessage());
        }
        return set;
    }

    private void saveToDb(CosmeticUnlock unlock) {
        String sql =
            "INSERT IGNORE INTO glimzo_cosmetic_unlocks " +
            "(uuid, cosmetic_id, unlocked_at, source) VALUES (?,?,?,?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, unlock.getPlayerUuid().toString());
            ps.setString(2, unlock.getCosmeticId());
            ps.setLong(3, unlock.getUnlockedAt());
            ps.setString(4, unlock.getSource());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[CosmeticUnlockManager] Failed to save unlock: " + e.getMessage());
        }
    }

    private void removeFromDb(UUID uuid, String cosmeticId) {
        String sql = "DELETE FROM glimzo_cosmetic_unlocks WHERE uuid = ? AND cosmetic_id = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, cosmeticId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[CosmeticUnlockManager] Failed to remove unlock: " + e.getMessage());
        }
    }
}

