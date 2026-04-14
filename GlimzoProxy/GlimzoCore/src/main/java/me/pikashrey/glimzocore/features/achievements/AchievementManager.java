package me.pikashrey.glimzocore.features.achievements;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Config-driven achievement system.
 * All achievements are defined in achievements.yml - no code changes needed to add new ones.
 *
 * Supported triggers (checked by AchievementListener):
 *   LOGIN, LOGIN_STREAK, LEVEL_REACHED, FRIEND_ADDED,
 *   CLAN_CREATED, CLAN_JOINED, COINS_EARNED, PRESTIGE, NICKNAME_SET
 */
public class AchievementManager {

    protected final GlimzoCore plugin;

    private final Map<String, Achievement>  definitions   = new LinkedHashMap<>();
    private final Map<UUID, Set<String>>    playerUnlocks = new ConcurrentHashMap<>();

    public AchievementManager(GlimzoCore plugin) {
        this.plugin = plugin;
        loadDefinitions();
    }


    public void loadDefinitions() {
        definitions.clear();

        File file = new File(plugin.getDataFolder(), "achievements.yml");
        if (!file.exists()) {
            plugin.saveResource("achievements.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection root = cfg.getConfigurationSection("achievements");
        if (root == null) {
            plugin.log("&c[Achievements] achievements.yml has no 'achievements' section!");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;

            String catStr = s.getString("category", "GENERAL").toUpperCase();
            AchievementCategory cat;
            try { cat = AchievementCategory.valueOf(catStr); }
            catch (IllegalArgumentException e) { cat = AchievementCategory.GENERAL; }

            String trigStr = s.getString("trigger", "").toUpperCase();
            AchievementTrigger trigger;
            try { trigger = AchievementTrigger.valueOf(trigStr); }
            catch (IllegalArgumentException e) {
                plugin.log("&e[Achievements] Unknown trigger '" + trigStr + "' for achievement '" + id + "', skipping.");
                continue;
            }

            Achievement ach = new Achievement(
                    id,
                    s.getString("display-name", id),
                    s.getString("description", ""),
                    cat,
                    trigger,
                    s.getInt("xp-reward", 0),
                    s.getLong("coin-reward", 0),
                    s.getInt("target-level", 0),
                    s.getInt("target-count", 0),
                    s.getLong("target-amount", 0),
                    s.getInt("login-streak-days", 0)
            );
            definitions.put(id, ach);
        }

        plugin.log("&a[Achievements] Loaded " + definitions.size() + " achievements from achievements.yml.");
    }

    public void reload() {
        loadDefinitions();
    }


    public void loadAchievements(UUID uuid) {
        playerUnlocks.put(uuid, fetchFromDb(uuid));
    }

    public void loadAchievementsFromData(UUID uuid, Set<String> ids) {
        Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        set.addAll(ids);
        playerUnlocks.put(uuid, set);
    }

    public void unloadAchievements(UUID uuid) {
        playerUnlocks.remove(uuid);
    }


    public boolean hasAchievement(UUID uuid, String id) {
        Set<String> s = playerUnlocks.get(uuid);
        return s != null && s.contains(id);
    }

    public void grantAchievement(UUID uuid, String achievementId) {
        if (hasAchievement(uuid, achievementId)) return;
        Achievement ach = definitions.get(achievementId);
        if (ach == null) return;

        playerUnlocks.computeIfAbsent(uuid,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(achievementId);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveToDb(uuid, achievementId));

        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) {
            data.getStats().incrementAchievementsUnlocked();
            final long coinReward = ach.getCoinReward();
            final long xpReward  = ach.getXpReward();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (coinReward > 0)
                    plugin.getCoinManager().addCoins(uuid, coinReward, "Achievement: " + achievementId);
                if (xpReward > 0)
                    plugin.getLevelManager().addXp(uuid, xpReward);
            });
        }

        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            online.sendMessage(CC.translate(
                    "&6&l✦ Achievement Unlocked! &r"
                    + ach.getCategory().getColor() + ach.getDisplayName()
                    + " &7- " + ach.getDescription()));
        }
    }


    /** Called on every login. */
    public void checkLogin(UUID uuid) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.LOGIN) continue;
            if (hasAchievement(uuid, ach.getId())) continue;
            grantAchievement(uuid, ach.getId());
        }
    }

    /** Called when login streak is updated. Pass current streak days. */
    public void checkLoginStreak(UUID uuid, int streakDays) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.LOGIN_STREAK) continue;
            if (hasAchievement(uuid, ach.getId())) continue;
            if (streakDays >= ach.getLoginStreakDays()) grantAchievement(uuid, ach.getId());
        }
    }

    /** Called when a player reaches a new level. */
    public void checkLevelReached(UUID uuid, int newLevel) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.LEVEL_REACHED) continue;
            if (hasAchievement(uuid, ach.getId())) continue;
            if (newLevel >= ach.getTargetLevel()) grantAchievement(uuid, ach.getId());
        }
    }

    /** Called when a friend is added. Pass current friend count. */
    public void checkFriendAdded(UUID uuid, int friendCount) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.FRIEND_ADDED) continue;
            if (hasAchievement(uuid, ach.getId())) continue;
            if (friendCount >= ach.getTargetCount()) grantAchievement(uuid, ach.getId());
        }
    }

    /** Called when a clan is created. */
    public void checkClanCreated(UUID uuid) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.CLAN_CREATED) continue;
            if (!hasAchievement(uuid, ach.getId())) grantAchievement(uuid, ach.getId());
        }
    }

    /** Called when a player joins a clan. */
    public void checkClanJoined(UUID uuid) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.CLAN_JOINED) continue;
            if (!hasAchievement(uuid, ach.getId())) grantAchievement(uuid, ach.getId());
        }
    }

    /** Called when lifetime coins earned changes. */
    public void checkCoinsEarned(UUID uuid, long lifetimeCoins) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.COINS_EARNED) continue;
            if (hasAchievement(uuid, ach.getId())) continue;
            if (lifetimeCoins >= ach.getTargetAmount()) grantAchievement(uuid, ach.getId());
        }
    }

    /** Called on prestige. */
    public void checkPrestige(UUID uuid) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.PRESTIGE) continue;
            if (!hasAchievement(uuid, ach.getId())) grantAchievement(uuid, ach.getId());
        }
    }

    /** Called when a nickname is set. */
    public void checkNicknameSet(UUID uuid) {
        for (Achievement ach : definitions.values()) {
            if (ach.getTrigger() != AchievementTrigger.NICKNAME_SET) continue;
            if (!hasAchievement(uuid, ach.getId())) grantAchievement(uuid, ach.getId());
        }
    }


    public int getUnlockCount(UUID uuid) {
        return playerUnlocks.getOrDefault(uuid, Collections.emptySet()).size();
    }

    public Set<String> getUnlocked(UUID uuid) {
        return Collections.unmodifiableSet(playerUnlocks.getOrDefault(uuid, Collections.emptySet()));
    }

    public Collection<Achievement> getAllAchievements() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public Achievement getAchievement(String id) { return definitions.get(id); }


    private Set<String> fetchFromDb(UUID uuid) {
        Set<String> set = new HashSet<>();
        String sql = "SELECT achievement_id FROM glimzo_achievements WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getString("achievement_id"));
            }
        } catch (SQLException e) {
            plugin.log("&c[AchievementManager] Load failed: " + e.getMessage());
        }
        return set;
    }

    private void saveToDb(UUID uuid, String id) {
        String sql = "INSERT IGNORE INTO glimzo_achievements (uuid, achievement_id, unlocked_at) VALUES (?,?,?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, id);
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[AchievementManager] Save failed: " + e.getMessage());
        }
    }
}
