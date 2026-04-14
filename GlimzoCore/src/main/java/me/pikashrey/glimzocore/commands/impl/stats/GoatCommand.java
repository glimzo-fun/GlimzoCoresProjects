package me.pikashrey.glimzocore.commands.impl.stats;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.season.SeasonLeaderboard;
import me.pikashrey.glimzocore.menus.GoatMenu;
import org.bukkit.entity.Player;

import java.util.List;

public class GoatCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public GoatCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (a.isPlayer()) {
            new GoatMenu(plugin, a.getPlayer()).open();
            return;
        }
        List<SeasonLeaderboard.Entry> entries = plugin.getSeasonLeaderboard().getEntries();
        if (entries.isEmpty()) { a.send("&7No G.O.A.T. data yet."); return; }
        a.send("&d&lG.O.A.T. Leaderboard (Season):");
        for (SeasonLeaderboard.Entry e : entries) {
            a.send("  &7#" + e.position + " &f" + e.name
                    + " &7- Rank &d" + e.seasonRank + " (" + e.seasonXp + " XP)");
        }
    }
}
