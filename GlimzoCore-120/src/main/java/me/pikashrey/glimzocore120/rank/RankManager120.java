package me.pikashrey.glimzocore120.rank;

import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore120.GlimzoCore120;
import org.bukkit.entity.Player;

public class RankManager120 {

    private final GlimzoCore120 plugin;

    public RankManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    public Rank getRank(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return Rank.DEFAULT;
        Rank rank = Rank.fromId(data.getRankId());
        return rank != null ? rank : Rank.DEFAULT;
    }

    public String getPrefix(Player player) {
        return getRank(player).getChatPrefix();
    }

    public void setRank(Player player, Rank rank) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return;
        data.setRankId(rank.getId());
        plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
        // Refresh visuals
        plugin.getTablistManager().update(player);
        plugin.getChatManager().applyNameTag(player);
    }

    public boolean isAtLeast(Player player, Rank rank) {
        return getRank(player).isAtLeast(rank);
    }
}
