package me.pikashrey.glimzocore.features.nick;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class NickManager {

    protected final GlimzoCore plugin;
    private final Pattern nickPattern;
    private final Random  random = new Random();

    public NickManager(GlimzoCore plugin) {
        this.plugin = plugin;
        String raw = plugin.getConfigManager().getSocial()
                .getString("nicks.allowed-pattern", "^[a-zA-Z0-9_]+$");
        this.nickPattern = Pattern.compile(raw);
    }


    private int weightOf(String rankId) {
        me.pikashrey.glimzocore.features.rank.RankLoader loader = plugin.getRankLoader();
        if (loader != null) {
            for (ConfiguredRank r : loader.getAll()) {
                if (rankId.equalsIgnoreCase(r.getId())) return r.getWeight();
            }
        }
        // Hard fallbacks matching ranks.yml
        switch (rankId.toLowerCase()) {
            case "warden":    return 200;
            case "legendary": return 400;
            case "cosmos":    return 700;
            default:          return 0;
        }
    }

    private int playerWeight(Player player) {
        return plugin.getRankManager().getActiveRankRef(player).getWeight();
    }

    /**
     * Tier logic (from spec):
     *   Warden (200) + above  → has nick access at all
     *   Legendary (400) + above → custom nick
     *   Cosmos (700) + above    → custom nick + optional rank spoofing
     */
    public boolean hasNickAccess(Player player) {
        return playerWeight(player) >= weightOf("warden");
    }

    /** Warden only (weight == warden) → random nick. */
    public boolean isRandomNickTier(Player player) {
        int w = playerWeight(player);
        return w >= weightOf("warden") && w < weightOf("legendary");
    }

    /** Legendary+ → custom nick. */
    public boolean isCustomNickTier(Player player) {
        return playerWeight(player) >= weightOf("legendary");
    }

    /** Cosmos+ → custom nick AND may pass a rank argument. */
    public boolean isRankNickTier(Player player) {
        return playerWeight(player) >= weightOf("cosmos");
    }


    public Result assignRandomNick(Player player) {
        List<String> pool = plugin.getConfigManager().getNicks()
                .getStringList("random-nicks");

        if (pool == null || pool.isEmpty()) return Result.NO_POOL;

        // Exclude nicks already in use
        pool.removeIf(nick -> {
            for (PlayerData pd : GlobalPlayer.getAll()) {
                if (pd.getUuid().equals(player.getUniqueId())) continue;
                if (nick.equalsIgnoreCase(pd.getName())) return true;
                if (nick.equalsIgnoreCase(pd.getNick()))  return true;
            }
            return false;
        });

        if (pool.isEmpty()) return Result.NO_POOL;
        return applyNick(player, pool.get(random.nextInt(pool.size())), null);
    }


    public Result setCustomNick(Player player, String rawNick) {
        return setCustomNick(player, rawNick, null);
    }

    /**
     * @param spoofedRankId  Rank to display in nametag/tab for this nick.
     *                       Null = keep real rank. Only allowed for Cosmos+.
     */
    public Result setCustomNick(Player player, String rawNick, String spoofedRankId) {
        String display = CC.strip(rawNick);

        int maxLen = plugin.getConfigManager().getSocial().getInt("nicks.max-length", 16);
        if (display.length() > maxLen) return Result.TOO_LONG;
        if (display.length() < 3)      return Result.TOO_SHORT;

        if (rawNick.contains("&")) {
            if (!player.hasPermission("glimzo.nick.color")) return Result.NO_COLOR_PERMISSION;
        }

        if (!nickPattern.matcher(rawNick).matches()) return Result.INVALID_CHARS;

        // Anti-impersonation
        for (PlayerData pd : GlobalPlayer.getAll()) {
            if (pd.getUuid().equals(player.getUniqueId())) continue;
            if (pd.getName().equalsIgnoreCase(display)) return Result.IMPERSONATION;
        }

        // Validate spoofed rank: must exist and be strictly below the player's own rank
        if (spoofedRankId != null) {
            if (!isRankNickTier(player))         return Result.NO_RANK_ACCESS;
            int spoofWeight = weightOf(spoofedRankId);
            if (spoofWeight <= 0)                return Result.INVALID_RANK;
            if (spoofWeight >= playerWeight(player)) return Result.RANK_TOO_HIGH;
        }

        return applyNick(player, rawNick, spoofedRankId);
    }


    public void clearNick(Player player) {
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return;
        data.setNick(null);
        data.setNickRank(null);
        // Restore real name in grey (default display color)
        player.setDisplayName(CC.translate("&7" + player.getName()));
        player.setPlayerListName(CC.translate("&7" + player.getName()));
        // Refresh nametag to restore real rank prefix
        if (plugin.getNameTagHandler() != null) plugin.getNameTagHandler().refresh(player);
    }


    private Result applyNick(Player player, String rawNick, String spoofedRankId) {
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return Result.NOT_LOADED;

        data.setNick(rawNick);
        data.setNickRank(spoofedRankId); // null = show real rank

        // Apply grey color if nick has no explicit color code
        String colored = rawNick.contains("&")
                ? CC.translate(rawNick)
                : CC.translate("&7" + rawNick);

        player.setDisplayName(colored);
        player.setPlayerListName(colored);

        // Refresh nametag so the spoofed rank prefix shows there too
        if (plugin.getNameTagHandler() != null) plugin.getNameTagHandler().refresh(player);

        if (plugin.getAchievementManager() != null) {
            plugin.getAchievementManager().checkNicknameSet(player.getUniqueId());
        }
        return Result.SUCCESS;
    }


    public boolean hasNick(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null && data.hasNick();
    }

    public String getNick(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null ? data.getNick() : null;
    }

    /** Returns the rank ID this player is spoofing while nicked, or null. */
    public String getNickRank(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null ? data.getNickRank() : null;
    }


    public enum Result {
        SUCCESS, NOT_LOADED, TOO_LONG, TOO_SHORT,
        INVALID_CHARS, NO_COLOR_PERMISSION, IMPERSONATION,
        NO_RANK_ACCESS, NO_POOL, INVALID_RANK, RANK_TOO_HIGH;

        public boolean isSuccess() { return this == SUCCESS; }
    }
}
