package me.pikashrey.glimzocore.features.cosmetics;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevelManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.meal.AllyMealManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.features.cosmetics.aura.AuraManager;
import me.pikashrey.glimzocore.features.cosmetics.chattag.ChatTagManager;
import me.pikashrey.glimzocore.features.cosmetics.joineffect.JoinEffectManager;
import me.pikashrey.glimzocore.features.cosmetics.morph.MorphManager;
import me.pikashrey.glimzocore.features.cosmetics.wings.WingsManager;
import me.pikashrey.glimzocore.utilities.file.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticManager {

    protected final GlimzoCore plugin;
    private final Map<UUID, PlayerCosmeticState> stateCache = new ConcurrentHashMap<>();

    private AuraManager           auraManager;
    private WingsManager          wingsManager;
    private AllyManager           allyManager;
    private AllyLevelManager      allyLevelManager;
    private AllyPerkManager       allyPerkManager;
    private AllyMealManager       allyMealManager;
    private MorphManager          morphManager;
    private ChatTagManager        chatTagManager;
    private JoinEffectManager     joinEffectManager;
    private CosmeticUnlockManager unlockManager;
    private FileConfiguration     cosmeticsConfig;

    public CosmeticManager(GlimzoCore plugin) { this.plugin = plugin; }

    public void enable() {
        ensureCosmeticsTable();
        loadCosmeticsConfig();
        unlockManager     = new CosmeticUnlockManager(plugin);
        allyLevelManager  = new AllyLevelManager(plugin);
        allyPerkManager   = new AllyPerkManager(plugin, allyLevelManager);
        auraManager       = new AuraManager(plugin);
        wingsManager      = new WingsManager(plugin);
        allyManager       = new AllyManager(plugin);
        morphManager      = new MorphManager(plugin);
        allyMealManager   = new AllyMealManager(plugin);
        chatTagManager    = new ChatTagManager(plugin);
        joinEffectManager = new JoinEffectManager(plugin);
        auraManager.start();
        wingsManager.start();
        allyManager.start();
        morphManager.start();
        plugin.log("&a[CosmeticManager] All cosmetic subsystems started.");
    }

    public void disable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerCosmeticState state = stateCache.get(player.getUniqueId());
            if (state != null) saveToDatabase(state);
        }
        if (allyPerkManager != null) allyPerkManager.disable();
        if (morphManager    != null) morphManager.stop();
        if (wingsManager    != null) wingsManager.stop();
        if (allyManager     != null) allyManager.stop();
        if (auraManager     != null) auraManager.stop();
        stateCache.clear();
    }

    private void loadCosmeticsConfig() {
        try {
            ConfigFile cf = new ConfigFile(plugin, "cosmetics.yml");
            cf.load();
            cosmeticsConfig = cf.getConfig();
        } catch (Exception e) {
            cosmeticsConfig = plugin.getConfig();
            plugin.log("&e[CosmeticManager] Could not load cosmetics.yml, using main config.");
        }
    }

    public FileConfiguration getCosmeticsConfig() {
        return cosmeticsConfig != null ? cosmeticsConfig : plugin.getConfig();
    }

    /** Called from PlayerJoinListener with the batched join row. */
    public void loadPlayerFromData(UUID uuid, Map<String, String> row) {
        PlayerCosmeticState state = new PlayerCosmeticState(uuid);
        state.setActiveAuraId(row.get("active_aura"));
        state.setActiveWingsId(row.get("active_wings"));
        state.setActiveAllyId(row.get("active_ally"));
        state.setActiveMorphId(row.get("active_morph"));
        state.setActiveChatTagId(row.get("active_chat_tag"));
        state.setActiveJoinEffectId(row.get("active_join_effect"));
        state.setActiveJoinMessageId(row.get("active_join_message"));
        stateCache.put(uuid, state);

        // *** THE FIX: load the unlock list so isOwned() in menus returns true ***
        unlockManager.loadUnlocks(uuid);
        allyLevelManager.loadAllyLevels(uuid);
        if (allyMealManager != null) allyMealManager.loadPlayer(uuid);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            applyLoadedCosmetics(player, state);
            morphManager.onPlayerJoin(player);
            // Restore perk for the equipped ally (if any)
            if (state.hasAlly()) {
                me.pikashrey.glimzocore.features.cosmetics.ally.AllyType equippedType =
                        me.pikashrey.glimzocore.features.cosmetics.ally.AllyType.fromId(state.getActiveAllyId());
                if (equippedType != null) allyPerkManager.onAllyEquipped(player, equippedType);
            }
        });
    }

    public void loadPlayer(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerCosmeticState state = loadFromDatabase(uuid);
            stateCache.put(uuid, state);
            unlockManager.loadUnlocks(uuid);
            allyLevelManager.loadAllyLevels(uuid);
            if (allyMealManager != null) allyMealManager.loadPlayer(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) return;
                applyLoadedCosmetics(player, state);
                morphManager.onPlayerJoin(player);
                // Restore perk for the equipped ally (if any)
                if (state.hasAlly()) {
                    me.pikashrey.glimzocore.features.cosmetics.ally.AllyType equippedType =
                            me.pikashrey.glimzocore.features.cosmetics.ally.AllyType.fromId(state.getActiveAllyId());
                    if (equippedType != null) allyPerkManager.onAllyEquipped(player, equippedType);
                }
            });
        });
    }

    public void savePlayer(UUID uuid) {
        PlayerCosmeticState state = stateCache.get(uuid);
        if (state == null) return;
        allyLevelManager.saveAllyLevels(uuid);
        if (allyMealManager != null) allyMealManager.savePlayer(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveToDatabase(state));
    }

    public void unloadPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            if (allyPerkManager != null) allyPerkManager.onPlayerQuit(player);
            if (morphManager    != null) morphManager.unmorph(player);
            if (wingsManager    != null) wingsManager.unequip(player);
            if (allyManager     != null) allyManager.removeAlly(player);
            if (auraManager     != null) auraManager.unequip(player);
        }
        // *** FIX: free memory on quit ***
        unlockManager.unloadUnlocks(uuid);
        allyLevelManager.unloadPlayer(uuid);
        if (allyMealManager != null) allyMealManager.unloadPlayer(uuid);
        stateCache.remove(uuid);
    }

    private void applyLoadedCosmetics(Player player, PlayerCosmeticState state) {
        if (state.hasAura() && auraManager != null) {
            if (!auraManager.equip(player, state.getActiveAuraId()))
                state.setActiveAuraId(null);
        }
        if (state.hasWings() && wingsManager != null) {
            if (!wingsManager.equip(player, state.getActiveWingsId()))
                state.setActiveWingsId(null);
        }
        if (state.hasAlly() && allyManager != null) {
            AllyType ally = AllyType.fromId(state.getActiveAllyId());
            if (ally != null) {
                // *** FIX: createAndSpawn bypasses the permission gate ***
                allyManager.createAndSpawn(player, ally);
            } else {
                state.setActiveAllyId(null);
            }
        }
        // Morphs are Coming Soon - clear any stale saved morph ID so it never tries to restore
        if (state.hasMorph()) state.setActiveMorphId(null);
    }

    public PlayerCosmeticState getState(UUID uuid) { return stateCache.get(uuid); }
    public PlayerCosmeticState getState(Player p)  { return stateCache.get(p.getUniqueId()); }

    private void ensureCosmeticsTable() {
        try (Connection con = plugin.getMysqlManager().getConnection();
             java.sql.Statement stmt = con.createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS glimzo_player_cosmetics (" +
                            "uuid VARCHAR(36) NOT NULL," +
                            "active_aura VARCHAR(64) DEFAULT NULL," +
                            "active_wings VARCHAR(64) DEFAULT NULL," +
                            "active_ally VARCHAR(64) DEFAULT NULL," +
                            "active_morph VARCHAR(64) DEFAULT NULL," +
                            "active_chat_tag VARCHAR(64) DEFAULT NULL," +
                            "active_join_effect VARCHAR(64) DEFAULT NULL," +
                            "active_join_message VARCHAR(64) DEFAULT NULL," +
                            "PRIMARY KEY (uuid)," +
                            "CONSTRAINT fk_cosmetics_player FOREIGN KEY (uuid) " +
                            "REFERENCES glimzo_players(uuid) ON DELETE CASCADE" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
            );
        } catch (SQLException e) {
            plugin.log("&c[CosmeticManager] Failed to create cosmetics table: " + e.getMessage());
        }
    }

    private PlayerCosmeticState loadFromDatabase(UUID uuid) {
        PlayerCosmeticState state = new PlayerCosmeticState(uuid);
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM glimzo_player_cosmetics WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    state.setActiveAuraId(rs.getString("active_aura"));
                    state.setActiveWingsId(rs.getString("active_wings"));
                    state.setActiveAllyId(rs.getString("active_ally"));
                    state.setActiveMorphId(rs.getString("active_morph"));
                    state.setActiveChatTagId(rs.getString("active_chat_tag"));
                    state.setActiveJoinEffectId(rs.getString("active_join_effect"));
                    state.setActiveJoinMessageId(rs.getString("active_join_message"));
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[CosmeticManager] Failed to load cosmetics for " + uuid + ": " + e.getMessage());
        }
        return state;
    }

    private void saveToDatabase(PlayerCosmeticState state) {
        String sql =
                "INSERT INTO glimzo_player_cosmetics " +
                        "(uuid, active_aura, active_wings, active_ally, active_morph, " +
                        "active_chat_tag, active_join_effect, active_join_message) " +
                        "VALUES (?,?,?,?,?,?,?,?) AS new_row ON DUPLICATE KEY UPDATE " +
                        "active_aura=new_row.active_aura, active_wings=new_row.active_wings, " +
                        "active_ally=new_row.active_ally, active_morph=new_row.active_morph, " +
                        "active_chat_tag=new_row.active_chat_tag, " +
                        "active_join_effect=new_row.active_join_effect, " +
                        "active_join_message=new_row.active_join_message";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, state.getUuid().toString());
            ps.setString(2, state.getActiveAuraId());
            ps.setString(3, state.getActiveWingsId());
            ps.setString(4, state.getActiveAllyId());
            ps.setString(5, state.getActiveMorphId());
            ps.setString(6, state.getActiveChatTagId());
            ps.setString(7, state.getActiveJoinEffectId());
            ps.setString(8, state.getActiveJoinMessageId());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[CosmeticManager] Failed to save cosmetics for " + state.getUuid() + ": " + e.getMessage());
        }
    }

    public AllyMealManager       getAllyMealManager()     { return allyMealManager; }
    public AuraManager           getAuraManager()       { return auraManager; }
    public WingsManager          getWingsManager()       { return wingsManager; }
    public AllyManager           getAllyManager()         { return allyManager; }
    public AllyLevelManager      getAllyLevelManager()    { return allyLevelManager; }
    public AllyPerkManager       getAllyPerkManager()     { return allyPerkManager; }
    public MorphManager          getMorphManager()        { return morphManager; }
    public ChatTagManager        getChatTagManager()      { return chatTagManager; }
    public JoinEffectManager     getJoinEffectManager()  { return joinEffectManager; }
    public CosmeticUnlockManager getUnlockManager()      { return unlockManager; }
}