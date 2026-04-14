package me.pikashrey.glimzocore.features.cosmetics.ally.meal;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player meal inventories (how many of each meal they own),
 * purchasing via gems or coins, and feeding meals to allies.
 *
 * Data stored in glimzo_ally_meals (uuid, meal_id, quantity).
 */
public class AllyMealManager {

    private final GlimzoCore plugin;

    private final Map<UUID, Map<String, Integer>> inventory = new ConcurrentHashMap<>();

    public AllyMealManager(GlimzoCore plugin) {
        this.plugin = plugin;
        ensureTable();
    }


    private FileConfiguration cfg() {
        return plugin.getCosmeticManager().getCosmeticsConfig();
    }

    public int getGemCost(AllyMealType meal) {
        return cfg().getInt("ally-meals.gem-costs." + meal.getId(), 50);
    }

    public int getCoinCost(AllyMealType meal) {
        return cfg().getInt("ally-meals.coin-costs." + meal.getId(), 200);
    }

    /** How many PetMeal XP points this food grants when fed to an ally. */
    public int getMealXp(AllyMealType meal) {
        return cfg().getInt("ally-meals.xp-values." + meal.getId(), 1);
    }


    public int getQuantity(UUID uuid, AllyMealType meal) {
        return inventory.getOrDefault(uuid, new HashMap<>()).getOrDefault(meal.getId(), 0);
    }

    public int getTotalMeals(UUID uuid) {
        return inventory.getOrDefault(uuid, new HashMap<>()).values().stream().mapToInt(i -> i).sum();
    }

    private void setQuantity(UUID uuid, AllyMealType meal, int qty) {
        inventory.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                 .put(meal.getId(), Math.max(0, qty));
    }


    public enum PurchaseResult { SUCCESS, NOT_ENOUGH_GEMS, NOT_ENOUGH_COINS }

    /** Buy with gems. Returns result. */
    public PurchaseResult buyWithGems(Player player, AllyMealType meal, int quantity) {
        int cost = getGemCost(meal) * quantity;
        if (!plugin.getGemManager().hasGems(player.getUniqueId(), cost))
            return PurchaseResult.NOT_ENOUGH_GEMS;
        plugin.getGemManager().removeGems(player.getUniqueId(), cost, "meal_purchase:" + meal.getId());
        addMeals(player.getUniqueId(), meal, quantity);
        return PurchaseResult.SUCCESS;
    }

    /** Buy with coins. Returns result. */
    public PurchaseResult buyWithCoins(Player player, AllyMealType meal, int quantity) {
        int cost = getCoinCost(meal) * quantity;
        if (!plugin.getCoinManager().hasCoins(player.getUniqueId(), cost))
            return PurchaseResult.NOT_ENOUGH_COINS;
        plugin.getCoinManager().removeCoins(player.getUniqueId(), cost, "meal_purchase:" + meal.getId());
        addMeals(player.getUniqueId(), meal, quantity);
        return PurchaseResult.SUCCESS;
    }

    private void addMeals(UUID uuid, AllyMealType meal, int quantity) {
        int current = getQuantity(uuid, meal);
        setQuantity(uuid, meal, current + quantity);
        saveAsync(uuid, meal);
    }


    /**
     * Consume one meal from the player's inventory and feed it to their equipped ally.
     * Returns false if they don't own any of this meal or have no ally equipped.
     */
    public boolean feedMeal(Player player, AllyMealType meal) {
        UUID uuid = player.getUniqueId();
        int qty = getQuantity(uuid, meal);
        if (qty <= 0) {
            player.sendMessage("§cYou don't have any §f" + meal.getDisplayName() + "§c!");
            return false;
        }

        // Consume 1
        setQuantity(uuid, meal, qty - 1);
        saveAsync(uuid, meal);

        // Get XP value and feed to ally system
        int xp = getMealXp(meal);
        AllyPerkManager pm = plugin.getCosmeticManager().getAllyPerkManager();
        AllyType equipped = pm != null ? pm.getEquippedAlly(uuid) : null;
        if (equipped == null) {
            player.sendMessage("§cYou need an ally equipped to feed meals!");
            return false;
        }

        pm.feedAlly(player, equipped, xp);
        player.sendMessage("§a✓ Fed §f" + meal.getDisplayName() + " §ato your §d" +
                equipped.getDisplayName() + "§a! §7(+" + xp + " XP)");
        return true;
    }


    public void loadPlayer(UUID uuid) {
        inventory.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        String sql = "SELECT meal_id, quantity FROM glimzo_ally_meals WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id  = rs.getString("meal_id");
                    int    qty = rs.getInt("quantity");
                    if (qty > 0) inventory.get(uuid).put(id, qty);
                }
            }
        } catch (SQLException e) {
            plugin.log("§c[AllyMealManager] Load failed for " + uuid + ": " + e.getMessage());
        }
    }

    public void savePlayer(UUID uuid) {
        Map<String, Integer> meals = inventory.get(uuid);
        if (meals == null) return;
        for (AllyMealType meal : AllyMealType.values())
            saveSync(uuid, meal, meals.getOrDefault(meal.getId(), 0));
    }

    public void unloadPlayer(UUID uuid) {
        inventory.remove(uuid);
    }

    private void saveAsync(UUID uuid, AllyMealType meal) {
        int qty = getQuantity(uuid, meal);
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> saveSync(uuid, meal, qty));
    }

    private void saveSync(UUID uuid, AllyMealType meal, int qty) {
        String sql = "INSERT INTO glimzo_ally_meals (uuid, meal_id, quantity) VALUES (?,?,?) " +
                     "AS new_row ON DUPLICATE KEY UPDATE quantity = new_row.quantity";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, meal.getId());
            ps.setInt(3, qty);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("§c[AllyMealManager] Save failed: " + e.getMessage());
        }
    }

    private void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS glimzo_ally_meals (" +
                "uuid     VARCHAR(36)  NOT NULL," +
                "meal_id  VARCHAR(64)  NOT NULL," +
                "quantity INT          NOT NULL DEFAULT 0," +
                "PRIMARY KEY (uuid, meal_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (Connection con = plugin.getMysqlManager().getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.log("§c[AllyMealManager] Table creation failed: " + e.getMessage());
        }
    }
}
