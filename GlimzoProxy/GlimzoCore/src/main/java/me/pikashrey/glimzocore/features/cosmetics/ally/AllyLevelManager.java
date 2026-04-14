package me.pikashrey.glimzocore.features.cosmetics.ally;

import me.pikashrey.glimzocore.GlimzoCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ally levels and PetMeal progress for all players.
 * Data is persisted to glimzo_ally_levels in MySQL.
 */
public class AllyLevelManager {

    private final GlimzoCore plugin;

    // playerUUID -> (AllyType -> currentLevel  [1-3, 0 = not levelled yet])
    private final Map<UUID, Map<AllyType, Integer>> allyLevels   = new ConcurrentHashMap<>();
    // playerUUID -> (AllyType -> petMeals accumulated towards next level)
    private final Map<UUID, Map<AllyType, Integer>> allyPetMeals = new ConcurrentHashMap<>();

    public AllyLevelManager(GlimzoCore plugin) {
        this.plugin = plugin;
        ensureTable();
    }


    /** Returns 0 if not yet levelled, 1-3 if levelled. */
    public int getAllyLevel(UUID playerUuid, AllyType allyType) {
        return allyLevels
                .getOrDefault(playerUuid, new HashMap<>())
                .getOrDefault(allyType, 0);
    }

    /** Returns meals accumulated towards the NEXT level after the current one. */
    public int getAllyPetMeals(UUID playerUuid, AllyType allyType) {
        return allyPetMeals
                .getOrDefault(playerUuid, new HashMap<>())
                .getOrDefault(allyType, 0);
    }

    public Map<AllyType, Integer> getAllyLevelsForPlayer(UUID playerUuid) {
        return allyLevels.getOrDefault(playerUuid, new HashMap<>());
    }


    public void setAllyLevel(UUID playerUuid, AllyType allyType, int level) {
        allyLevels
                .computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .put(allyType, Math.max(0, Math.min(3, level)));
    }

    /**
     * Add PetMeals to an ally.
     * Returns the new level if a level-up occurred, or 0 if not.
     */
    public int addPetMeals(UUID playerUuid, AllyType allyType, int count) {
        int currentLevel = getAllyLevel(playerUuid, allyType);
        if (currentLevel >= 3) return 0; // already maxed

        int currentMeals = getAllyPetMeals(playerUuid, allyType);
        int newMeals     = currentMeals + count;
        int mealsPerLevel = plugin.getCosmeticManager().getCosmeticsConfig().getInt("allies.meals-per-level", 10);

        // Level up ONE step at a time so level-2 milestones/rewards are never skipped.
        // Excess meals beyond the threshold carry over to the next level.
        if (newMeals >= mealsPerLevel && currentLevel < 3) {
            int nextLevel = currentLevel + 1;
            int remainder = newMeals - mealsPerLevel;
            setAllyLevel(playerUuid, allyType, nextLevel);
            allyPetMeals
                    .computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                    .put(allyType, remainder);
            return nextLevel;
        }

        allyPetMeals
                .computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .put(allyType, newMeals);
        return 0;
    }


    public void loadAllyLevels(UUID playerUuid) {
        allyLevels  .computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());
        allyPetMeals.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());

        String sql = "SELECT ally_type, ally_level, pet_meals " +
                "FROM glimzo_ally_levels WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AllyType type = AllyType.fromId(rs.getString("ally_type"));
                    if (type == null) continue;
                    int level = rs.getInt("ally_level");
                    int meals = rs.getInt("pet_meals");
                    allyLevels.get(playerUuid).put(type, level);
                    allyPetMeals.get(playerUuid).put(type, meals);
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[AllyLevelManager] Failed to load levels for " + playerUuid + ": " + e.getMessage());
        }
    }

    public void saveAllyLevels(UUID playerUuid) {
        Map<AllyType, Integer> levels = allyLevels.get(playerUuid);
        if (levels == null || levels.isEmpty()) return;

        String sql =
                "INSERT INTO glimzo_ally_levels (uuid, ally_type, ally_level, pet_meals) " +
                        "VALUES (?, ?, ?, ?) AS new_row " +
                        "ON DUPLICATE KEY UPDATE " +
                        "ally_level = new_row.ally_level, pet_meals = new_row.pet_meals";

        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map.Entry<AllyType, Integer> entry : levels.entrySet()) {
                int meals = getAllyPetMeals(playerUuid, entry.getKey());
                ps.setString(1, playerUuid.toString());
                ps.setString(2, entry.getKey().getId());
                ps.setInt(3, entry.getValue());
                ps.setInt(4, meals);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            plugin.log("&c[AllyLevelManager] Failed to save levels for " + playerUuid + ": " + e.getMessage());
        }
    }

    public void unloadPlayer(UUID playerUuid) {
        allyLevels  .remove(playerUuid);
        allyPetMeals.remove(playerUuid);
    }


    private void ensureTable() {
        String sql =
                "CREATE TABLE IF NOT EXISTS glimzo_ally_levels (" +
                        "uuid       VARCHAR(36) NOT NULL," +
                        "ally_type  VARCHAR(32) NOT NULL," +
                        "ally_level INT         NOT NULL DEFAULT 1," +
                        "pet_meals  INT         NOT NULL DEFAULT 0," +
                        "PRIMARY KEY (uuid, ally_type)," +
                        "INDEX idx_uuid (uuid)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection con = plugin.getMysqlManager().getConnection();
             java.sql.Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.log("&c[AllyLevelManager] Failed to create table: " + e.getMessage());
        }
    }
}