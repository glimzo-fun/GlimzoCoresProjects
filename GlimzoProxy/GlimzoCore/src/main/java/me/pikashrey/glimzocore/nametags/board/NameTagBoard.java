package me.pikashrey.glimzocore.nametags.board;

import me.pikashrey.glimzocore.nametags.NameTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class NameTagBoard {

    private final Player     owner;
    private final Scoreboard board;

    public NameTagBoard(Player owner) {
        this.owner = owner;
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();
        owner.setScoreboard(board);
    }

    public void applyTag(Player target, NameTag tag) {
        String teamName = NameTag.teamName(target.getUniqueId());
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        team.setPrefix(truncate(tag.getPrefix(), 16));
        team.setSuffix(truncate(tag.getSuffix(), 16));

        // Ensure target is in their team
        String entry = target.getName();
        if (!team.hasEntry(entry)) {
            team.addEntry(entry);
        }
    }

    /**
     * Remove a player's team from this board (called when they disconnect).
     */
    public void removeTag(UUID targetUuid) {
        String teamName = NameTag.teamName(targetUuid);
        Team team = board.getTeam(teamName);
        if (team != null) team.unregister();
    }

    public void initAllPlayers(java.util.Collection<? extends Player> online,
                                java.util.function.Function<Player, NameTag> tagProvider) {
        for (Player p : online) {
            applyTag(p, tagProvider.apply(p));
        }
    }

    public Player     getOwner() { return owner; }
    public Scoreboard getBoard()  { return board; }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
}

