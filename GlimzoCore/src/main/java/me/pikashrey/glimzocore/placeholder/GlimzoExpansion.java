package me.pikashrey.glimzocore.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.api.season.SeasonRank;
import me.pikashrey.glimzocore.features.leveling.LevelManager;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;

public class GlimzoExpansion extends PlaceholderExpansion {

    private final GlimzoCore plugin;

    public GlimzoExpansion(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override public String  getIdentifier() { return "glimzo"; }
    @Override public String  getAuthor()     { return "Pikashrey"; }
    @Override public String  getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()       { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return "";

        RankRef rank = plugin.getRankManager().getActiveRankRef(player);

        switch (identifier) {
            //  Rank
            case "rank":
                return CC.translate(rank.getColorCode() + rank.getDisplayName());
            case "rank_id":
                return rank.getId();
            case "rank_prefix":
                return CC.translate(rank.getChatPrefix());

            // --- Level ---
            case "level":
                return String.valueOf(data.getLevel());
            case "prestige":
                return String.valueOf(data.getPrestige());
            case "prestige_display":
                return data.getPrestige() > 0
                        ? CC.translate(plugin.getPrestigeManager().getPrestigeDisplay(data.getPrestige()))
                        : "";
            case "xp":
                return String.valueOf(data.getExperience());
            case "xp_needed":
                return String.valueOf(LevelManager.xpForLevel(data.getLevel()));
            case "xp_percent": {
                double pct = plugin.getLevelManager().levelProgress(data) * 100;
                return String.format("%.1f", pct);
            }

            // --- Economy ---
            case "coins":
                return String.valueOf(data.getCoins());
            case "gems":
                return String.valueOf(data.getGems());

            // --- Clan ---
            case "clan": {
                ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                return clan != null ? "[" + clan.getTag() + "] " + clan.getName() : "None";
            }
            case "clan_tag": {
                ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                return clan != null ? clan.getTag() : "";
            }
            case "clan_level": {
                ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                return clan != null ? String.valueOf(clan.getLevel()) : "0";
            }

            // --- Season ---
            case "season_rank": {
                SeasonRank sRank = plugin.getSeasonManager().getPlayerRank(data);
                return sRank != null
                        ? CC.translate(sRank.getColorCode() + sRank.getDisplayName())
                        : "Unranked";
            }
            case "season_xp":
                return String.valueOf(data.getSeasonXp());

            // --- Social ---
            case "friends":
                return String.valueOf(plugin.getFriendManager().getFriendCount(player.getUniqueId()));

            // --- Server ---
            case "tps":
                return String.format("%.1f",
                        me.pikashrey.glimzocore.utilities.general.ServerUtils.getTps());

            default:
                return null; // let PAPI handle unknown identifiers
        }
    }
}

