package me.pikashrey.glimzocore.features.season;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.events.impl.PlayerSeasonRankUpEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.season.Season;
import me.pikashrey.glimzocore.api.season.SeasonPassTier;
import me.pikashrey.glimzocore.api.season.SeasonRank;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SeasonManager {

    protected final GlimzoCore plugin;
    private Season currentSeason;

    public SeasonManager(GlimzoCore plugin) {
        this.plugin = plugin;
        loadCurrentSeason();
    }

    public void loadCurrentSeason() {
        FileConfiguration cfg = plugin.getConfigManager().getSeasons();
        List<SeasonRank>     ranks = loadRanks(cfg);
        List<SeasonPassTier> tiers = loadPassTiers(cfg, ranks);

        long now   = System.currentTimeMillis();
        long start = plugin.getConfig().getLong("season.start", now);
        long end   = plugin.getConfig().getLong("season.end",   now + 30L * 24 * 3600 * 1000);
        int  num   = plugin.getConfig().getInt("season.number", 1);

        currentSeason = new Season(num, "Season " + num, start, end, ranks, tiers);
    }

    private List<SeasonRank> loadRanks(FileConfiguration cfg) {
        List<SeasonRank> ranks = new ArrayList<>();
        List<?> raw = cfg.getList("season.ranks");
        if (raw == null || raw.isEmpty()) return ranks;

        for (int i = 0; i < raw.size(); i++) {
            Object obj = raw.get(i);
            if (!(obj instanceof java.util.Map)) continue;
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) obj;
            ranks.add(new SeasonRank(
                    i,
                    str(map, "id",      "rank_" + i),
                    str(map, "display", "Rank " + i),
                    str(map, "color",   "&7"),
                    toLong(map.get("xp-required"), 0L)
            ));
        }
        return ranks;
    }

    private List<SeasonPassTier> loadPassTiers(FileConfiguration cfg, List<SeasonRank> ranks) {
        List<SeasonPassTier> tiers = new ArrayList<>();
        List<?> raw = cfg.getList("season.pass-tiers");
        int count = raw != null ? Math.min(raw.size(), ranks.size()) : 0;

        for (int i = 0; i < count; i++) {
            Object obj = raw.get(i);
            List<String> free    = Collections.emptyList();
            List<String> premium = Collections.emptyList();
            if (obj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) obj;
                free    = toStringList(map.get("free"));
                premium = toStringList(map.get("premium"));
            }
            tiers.add(new SeasonPassTier(i + 1, ranks.get(i).getXpRequired(), free, premium));
        }

        while (tiers.size() < ranks.size()) {
            tiers.add(new SeasonPassTier(tiers.size() + 1, ranks.get(tiers.size()).getXpRequired(),
                    Collections.emptyList(), Collections.emptyList()));
        }
        return tiers;
    }

    public void addSeasonXp(UUID uuid, long amount) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null || currentSeason == null || !currentSeason.isActive()) return;
        data.addSeasonXp(amount);
        data.getStats().addSeasonXP(amount);
        checkRankUp(data);
    }

    private void checkRankUp(PlayerData data) {
        if (currentSeason == null) return;
        List<SeasonRank> ranks = currentSeason.getRanks();
        if (ranks == null || ranks.isEmpty()) return;

        int current = data.getSeasonRank();
        while (current + 1 < ranks.size()) {
            if (data.getSeasonXp() < ranks.get(current + 1).getXpRequired()) break;
            current++;
            data.setSeasonRank(current);
            data.getStats().updateHighestSeasonRank(current);

            Bukkit.getPluginManager().callEvent(new PlayerSeasonRankUpEvent(data, current - 1, current));

            SeasonPassTier tier = currentSeason.getPassTierForRank(current);
            if (tier != null) deliverTierRewards(data, tier);

            org.bukkit.entity.Player online = Bukkit.getPlayer(data.getUuid());
            if (online != null) {
                SeasonRank rank = ranks.get(current);
                online.sendMessage(CC.translate("&aYou reached " + rank.getColorCode() + rank.getDisplayName() + "&a!"));
            }
        }
    }

    private void deliverTierRewards(PlayerData data, SeasonPassTier tier) {
        applyRewards(data, tier.getFreeRewards());
        if (data.hasSeasonPass()) applyRewards(data, tier.getPremiumRewards());
    }

    private void applyRewards(PlayerData data, List<String> rewards) {
        if (rewards == null) return;
        for (String entry : rewards) {
            String[] parts = entry.split(":", 2);
            if (parts.length != 2) continue;
            try {
                long amount = Long.parseLong(parts[1].trim());
                final long fAmount = amount;
                final UUID uuid = data.getUuid();
                switch (parts[0].trim().toLowerCase()) {
                    case "coins":
                        // addCoins fires a Bukkit event - must run on main thread
                        org.bukkit.Bukkit.getScheduler().runTask(plugin,
                                () -> plugin.getCoinManager().addCoins(uuid, fAmount, "Season pass reward"));
                        break;
                    case "gems":
                        org.bukkit.Bukkit.getScheduler().runTask(plugin,
                                () -> plugin.getGemManager().addGems(uuid, fAmount, "Season pass reward"));
                        break;
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    public Season     getCurrentSeason() { return currentSeason; }
    public void tick() {
        if (currentSeason == null) return;
        if (!currentSeason.isActive()) {
            // Season has ended - log once and stop awarding XP.
            // addSeasonXp() already guards on isActive() so no XP can slip through.
            // Full end-of-season reward/reset logic can be wired here when ready.
            plugin.log("&e[Season] Season " + currentSeason.getNumber()
                    + " has ended. Awaiting reset.");
            // Prevent this log from spamming every tick by nulling currentSeason.
            // GlimzoCore admin can reload a new season via config + /glimzo reload config.
            currentSeason = null;
        }
    }

    public SeasonRank getPlayerRank(PlayerData data) {
        if (currentSeason == null) return null;
        List<SeasonRank> ranks = currentSeason.getRanks();
        if (ranks == null || ranks.isEmpty()) return null;
        return ranks.get(Math.max(0, Math.min(data.getSeasonRank(), ranks.size() - 1)));
    }

    public SeasonRank getRankByIndex(int index) {
        if (currentSeason == null) return null;
        List<SeasonRank> ranks = currentSeason.getRanks();
        if (ranks == null || index < 0 || index >= ranks.size()) return null;
        return ranks.get(index);
    }

    private static String str(java.util.Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v != null ? v.toString() : def;
    }

    private static long toLong(Object v, long def) {
        if (v == null) return def;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return def; }
    }

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object v) {
        if (!(v instanceof List)) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (Object item : (List<?>) v) result.add(item.toString());
        return result;
    }
}
