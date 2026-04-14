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

public class AchievementManager {

    protected final GlimzoCore plugin;

    private final Map<String, Achievement> definitions   = new LinkedHashMap<>();
    private final Map<UUID, Set<String>>   playerUnlocks = new ConcurrentHashMap<>();

    public AchievementManager(GlimzoCore plugin) {
        this.plugin = plugin;
        loadDefinitions();
    }

    public void loadDefinitions() {
        definitions.clear();

        File file = new File(plugin.getDataFolder(), "achievements.yml");
        if (!file.exists()) plugin.saveResource("achievements.yml", false);
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection root = cfg.getConfigurationSection("achievements");
        if (root == null) {
            plugin.log("&c[Achievements] achievements.yml has no 'achievements' section!");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;

            AchievementCategory cat;
            try { cat = AchievementCategory.valueOf(s.getString("category", "GENERAL").toUpperCase()); }
            catch (IllegalArgumentException e) { cat = AchievementCategory.GENERAL; }

            AchievementTrigger trigger;
            try { trigger = AchievementTrigger.valueOf(s.getString("trigger", "").toUpperCase()); }
            catch (IllegalArgumentException e) {
                plugin.log("&e[Achievements] Unknown trigger for '" + id + "', skipping.");
                continue;
            }

            definitions.put(id, new Achievement(
                    id, s.getString("display-name", id), s.getString("description", ""),
                    cat, trigger, s.getInt("xp-reward", 0), s.getLong("coin-reward", 0),
                    s.getInt("target-level", 0), s.getInt("target-count", 0),
                    s.getLong("target-amount", 0), s.getInt("login-streak-days", 0)
            ));
        }

        plugin.log("&a[Achievements] Loaded " + definitions.size() + " achievements.");
    }

    public void reload() { loadDefinitions(); }

    public void loadAchievements(UUID uuid) {
        playerUnlocks.put(uuid, fetchFromDb(uuid));
    }

    public void loadAchievementsFromData(UUID uuid, Set<String> ids) {
        Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        set.addAll(ids);
        playerUnlocks.put(uuid, set);
    }

    public void unloadAchievements(UUID uuid) { playerUnlocks.remove(uuid); }

    public boolean hasAchievement(UUID uuid, String id) {
        Set<String> s = playerUnlocks.get(uuid);
        return s != null && s.contains(id);
    }

    public void grantAchievement(UUID uuid, String achievementId) {
        if (hasAchievement(uuid, achievementId)) return;
        Achievement ach = definitions.get(achievementId);
        if (ach == null) return;

        playerUnlocks.computeIfAbsent(uuid, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(achievementId);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveToDb(uuid, achievementId));

        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) {
            data.getStats().incrementAchievementsUnlocked();
            final long coinReward = ach.getCoinReward();
            final long xpReward  = ach.getXpReward();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (coinReward > 0) plugin.getCoinManager().addCoins(uuid, coinReward, "Achievement: " + achievementId);
                if (xpReward  > 0) plugin.getLevelManager().addXp(uuid, xpReward);
            });
        }

        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            online.sendMessage(CC.translate("&6&l\u2746 Achievement Unlocked! &r"
                    + ach.getCategory().getColor() + ach.getDisplayName()
                    + " &7- " + ach.getDescription()));
        }
    }

    public void checkLogin(UUID uuid) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() == AchievementTrigger.LOGIN && !hasAchievement(uuid, a.getId()))
                grantAchievement(uuid, a.getId());
        }
    }

    public void checkLoginStreak(UUID uuid, int streakDays) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() != AchievementTrigger.LOGIN_STREAK || hasAchievement(uuid, a.getId())) continue;
            if (streakDays >= a.getLoginStreakDays()) grantAchievement(uuid, a.getId());
        }
    }

    public void checkLevelReached(UUID uuid, int newLevel) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() != AchievementTrigger.LEVEL_REACHED || hasAchievement(uuid, a.getId())) continue;
            if (newLevel >= a.getTargetLevel()) grantAchievement(uuid, a.getId());
        }
    }

    public void checkFriendAdded(UUID uuid, int friendCount) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() != AchievementTrigger.FRIEND_ADDED || hasAchievement(uuid, a.getId())) continue;
            if (friendCount >= a.getTargetCount()) grantAchievement(uuid, a.getId());
        }
    }

    public void checkClanCreated(UUID uuid) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() == AchievementTrigger.CLAN_CREATED && !hasAchievement(uuid, a.getId()))
                grantAchievement(uuid, a.getId());
        }
    }

    public void checkClanJoined(UUID uuid) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() == AchievementTrigger.CLAN_JOINED && !hasAchievement(uuid, a.getId()))
                grantAchievement(uuid, a.getId());
        }
    }

    public void checkCoinsEarned(UUID uuid, long lifetimeCoins) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() != AchievementTrigger.COINS_EARNED || hasAchievement(uuid, a.getId())) continue;
            if (lifetimeCoins >= a.getTargetAmount()) grantAchievement(uuid, a.getId());
        }
    }

    public void checkPrestige(UUID uuid) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() == AchievementTrigger.PRESTIGE && !hasAchievement(uuid, a.getId()))
                grantAchievement(uuid, a.getId());
        }
    }

    public void checkNicknameSet(UUID uuid) {
        for (Achievement a : definitions.values()) {
            if (a.getTrigger() == AchievementTrigger.NICKNAME_SET && !hasAchievement(uuid, a.getId()))
                grantAchievement(uuid, a.getId());
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
