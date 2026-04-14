package me.pikashrey.glimzocore.features.season;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.season.Season;
import me.pikashrey.glimzocore.api.season.SeasonPassTier;

import java.util.UUID;

public class SeasonPassManager {

    protected final GlimzoCore plugin;

    public SeasonPassManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    /** Activate the season pass for a player (called after purchase). */
    public void activatePass(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null) return;
        if (data.hasSeasonPass()) return; // already active
        data.setSeasonPassActive(true);
        deliverBacklogRewards(uuid, data);
    }

    public void onRankUp(UUID uuid, int newRankIndex) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null) return;

        Season season = plugin.getSeasonManager().getCurrentSeason();
        if (season == null || newRankIndex >= season.getPassTiers().size()) return;

        SeasonPassTier tier = season.getPassTiers().get(newRankIndex);
        deliverTierRewards(uuid, tier, data.hasSeasonPass());
    }

    private void deliverBacklogRewards(UUID uuid, PlayerData data) {
        Season season = plugin.getSeasonManager().getCurrentSeason();
        if (season == null) return;

        int currentRank = data.getSeasonRank();
        for (int i = 0; i <= currentRank && i < season.getPassTiers().size(); i++) {
            deliverTierRewards(uuid, season.getPassTiers().get(i), true);
        }
    }

    private void deliverTierRewards(UUID uuid, SeasonPassTier tier, boolean includePremium) {
        // Parse reward strings: "coins:500", "gems:25"
        for (String reward : tier.getFreeRewards()) {
            applyReward(uuid, reward);
        }
        if (includePremium) {
            for (String reward : tier.getPremiumRewards()) {
                applyReward(uuid, reward);
            }
        }
    }

    private void applyReward(UUID uuid, String reward) {
        String[] parts = reward.split(":");
        if (parts.length < 2) return;
        try {
            final long amount = Long.parseLong(parts[1]);
            // addCoins/addGems/addXp fire Bukkit events - must run on main thread
            switch (parts[0]) {
                case "coins":
                    org.bukkit.Bukkit.getScheduler().runTask(plugin,
                            () -> plugin.getCoinManager().addCoins(uuid, amount, "Season pass reward"));
                    break;
                case "gems":
                    org.bukkit.Bukkit.getScheduler().runTask(plugin,
                            () -> plugin.getGemManager().addGems(uuid, amount, "Season pass reward"));
                    break;
                case "xp":
                    org.bukkit.Bukkit.getScheduler().runTask(plugin,
                            () -> plugin.getLevelManager().addXp(uuid, amount));
                    break;
            }
        } catch (NumberFormatException ignored) {}
    }

    public boolean hasPass(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null && data.hasSeasonPass();
    }
}

