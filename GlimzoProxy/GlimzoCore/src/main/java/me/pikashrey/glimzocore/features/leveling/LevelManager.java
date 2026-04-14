package me.pikashrey.glimzocore.features.leveling;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.events.impl.PlayerLevelUpEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelManager {

    protected final GlimzoCore plugin;

    public static int  MAX_LEVEL          = 100;
    public static long BASE_XP            = 1000L;
    public static long XP_INCREMENT        = 500L;
    private int  clanXpSharePercent;

    private Map<Integer, LevelReward> rewards = Collections.emptyMap();

    public LevelManager(GlimzoCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfigManager().getLeveling();

        MAX_LEVEL = cfg.getInt("leveling.max-level", 100);
        BASE_XP            = cfg.getLong("leveling.base-xp", 1000L);
        XP_INCREMENT       = cfg.getLong("leveling.xp-increment", 500L);
        clanXpSharePercent = cfg.getInt("leveling.clan-xp-share-percent", 10);
        rewards            = Collections.unmodifiableMap(buildRewardTable(cfg));
    }

    public int  getMaxLevel()           { return MAX_LEVEL; }
    public long getBaseXp()             { return BASE_XP; }
    public long getXpIncrement()        { return XP_INCREMENT; }
    public int  getClanXpSharePercent() { return clanXpSharePercent; }
    public LevelReward getReward(int level) { return rewards.get(level); }

    public static long xpForLevel(int level) {
        if (level <= 0) return BASE_XP;
        return BASE_XP + (long)(level - 1) * XP_INCREMENT;
    }

    public long totalXpForLevel(int level) {
        long total = 0;
        for (int i = 1; i < level; i++) total += LevelManager.xpForLevel(i);
        return total;
    }

    public long xpToNextLevel(PlayerData data) {
        if (data.getLevel() >= MAX_LEVEL) return 0;
        return xpForLevel(data.getLevel()) - data.getExperience();
    }

    public double levelProgress(PlayerData data) {
        if (data.getLevel() >= MAX_LEVEL) return 1.0;
        long needed = xpForLevel(data.getLevel());
        if (needed <= 0) return 1.0;
        return Math.min(1.0, (double) data.getExperience() / needed);
    }

    public void addXp(UUID uuid, long base) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null || base <= 0) return;

        long amount = applyBoost(uuid, base);
        data.addExperience(amount);
        data.getStats().addXP(amount);

        me.pikashrey.glimzocore.api.clan.ClanData clan = plugin.getClanManager().getClanByPlayer(uuid);
        if (clan != null && clanXpSharePercent > 0) {
            plugin.getClanManager().addXp(clan, uuid,
                    me.pikashrey.glimzocore.api.events.impl.ClanXpGainEvent.Source.MEMBER_ACTIVITY,
                    Math.max(1, amount * clanXpSharePercent / 100));
        }

        checkLevelUp(data);
    }

    private void checkLevelUp(PlayerData data) {
        while (data.getLevel() < MAX_LEVEL) {
            long needed = xpForLevel(data.getLevel());
            if (data.getExperience() < needed) break;

            data.addExperience(-needed);
            int prev = data.getLevel();
            data.setLevel(prev + 1);
            data.getStats().updateHighestLevel(data.getLevel());

            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(data, prev, data.getLevel()));

            LevelReward reward = rewards.get(data.getLevel());
            if (reward != null) deliverReward(data, reward);
        }
    }

    private long applyBoost(UUID uuid, long base) {
        me.pikashrey.glimzocore.api.clan.ClanData clan = plugin.getClanManager().getClanByPlayer(uuid);
        if (clan == null) return base;
        double boost = clan.getClanLevel().getPerkValue(me.pikashrey.glimzocore.api.clan.ClanPerk.XP_BOOST);
        return base + (long)(base * boost);
    }

    private void deliverReward(PlayerData data, LevelReward reward) {
        String reason = "Level " + data.getLevel() + " reward";
        switch (reward.getType()) {
            case COINS: plugin.getCoinManager().addCoins(data.getUuid(), reward.getAmount(), reason); break;
            case GEMS:  plugin.getGemManager().addGems(data.getUuid(), reward.getAmount(), reason);   break;
            default: break;
        }
        if (reward.getValue() != null && !reward.getValue().isEmpty()) {
            org.bukkit.entity.Player online = Bukkit.getPlayer(data.getUuid());
            if (online != null) online.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate(reward.getValue()));
        }
    }

    private Map<Integer, LevelReward> buildRewardTable(FileConfiguration cfg) {
        Map<Integer, LevelReward> table = new HashMap<>();

        for (int i = 1; i <= MAX_LEVEL; i++) {
            if      (i % 25 == 0) table.put(i, LevelReward.gems(i, 50L + (i / 25) * 25L));
            else if (i % 10 == 0) table.put(i, LevelReward.gems(i, 25));
            else if (i %  5 == 0) table.put(i, LevelReward.coins(i, 500L * (i / 5)));
            else                  table.put(i, LevelReward.none(i));
        }

        ConfigurationSection overrides = cfg.getConfigurationSection("rewards");
        if (overrides != null) {
            for (String key : overrides.getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    if (level < 1 || level > MAX_LEVEL) continue;
                    ConfigurationSection entry = overrides.getConfigurationSection(key);
                    if (entry == null) continue;
                    LevelReward.Type type;
                    try { type = LevelReward.Type.valueOf(entry.getString("type", "NONE").toUpperCase()); }
                    catch (IllegalArgumentException e) { type = LevelReward.Type.NONE; }
                    table.put(level, new LevelReward(level, type, entry.getLong("amount", 0), entry.getString("message")));
                } catch (NumberFormatException ignored) {}
            }
        }

        return table;
    }
}
