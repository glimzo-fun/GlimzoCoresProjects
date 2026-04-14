package me.pikashrey.glimzocore120.tablist;

import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore120.GlimzoCore120;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TablistManager120 {

    private final GlimzoCore120 plugin;

    public TablistManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    /** Update tab list header/footer for all players */
    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            update(p);
        }
    }

    /** Update tab list header/footer + nametag for a single player */
    public void update(Player player) {
        // Header / footer
        Component header = Component.newline()
                .append(Component.text("✦ Glimzo Network ✦", NamedTextColor.AQUA))
                .append(Component.newline());

        Component footer = Component.newline()
                .append(Component.text("Online: ", NamedTextColor.GRAY))
                .append(Component.text(Bukkit.getOnlinePlayers().size() + "", NamedTextColor.WHITE))
                .append(Component.newline());

        player.sendPlayerListHeaderAndFooter(header, footer);

        // Rank prefix in tab via scoreboard team
        Rank rank        = plugin.getRankManager().getRank(player);
        Scoreboard sb    = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName  = "tab_" + String.format("%02d", 99 - rank.getWeight()) + "_" +
                           player.getName().substring(0, Math.min(player.getName().length(), 10));

        // Remove from any existing team first
        for (Team t : sb.getTeams()) {
            t.removeEntry(player.getName());
        }

        Team team = sb.getTeam(teamName);
        if (team == null) team = sb.registerNewTeam(teamName);

        Component prefix = LegacyComponentSerializer.legacyAmpersand()
                .deserialize(rank.getChatPrefix() + " ");
        team.prefix(prefix);
        team.addEntry(player.getName());
        player.setScoreboard(sb);
    }

    public void remove(Player player) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team t : sb.getTeams()) {
            if (t.hasEntry(player.getName())) {
                t.removeEntry(player.getName());
                break;
            }
        }
    }
}
