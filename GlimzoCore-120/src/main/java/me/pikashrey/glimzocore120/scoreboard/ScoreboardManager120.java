package me.pikashrey.glimzocore120.scoreboard;

import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore120.GlimzoCore120;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager120 {

    private final GlimzoCore120 plugin;

    public ScoreboardManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
        // Refresh scoreboards every 2 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 40L, 40L);
    }

    public void setup(Player player) {
        update(player);
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            update(p);
        }
    }

    public void update(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return;

        Rank rank = plugin.getRankManager().getRank(player);

        Scoreboard sb     = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj     = sb.registerNewObjective(
                "glimzo", Criteria.DUMMY,
                Component.text("✦ GLIMZO ✦")
                        .color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD)
        );
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Lines - score order is reversed (higher score = higher position)
        setLine(obj, 10, " ");
        setLine(obj, 9,  "§7Server: §f" + getServerName());
        setLine(obj, 8,  " ");
        setLine(obj, 7,  "§7Rank: " + rank.getChatPrefix());
        setLine(obj, 6,  "§7Level: §f" + data.getLevel());
        setLine(obj, 5,  " ");
        setLine(obj, 4,  "§7Coins: §6" + data.getCoins());
        setLine(obj, 3,  "§7Gems: §b" + data.getGems());
        setLine(obj, 2,  " ");
        setLine(obj, 1,  "§bglimzo.net");

        player.setScoreboard(sb);
    }

    private void setLine(Objective obj, int score, String text) {
        // Use unique fake player names per line to avoid conflicts
        String entry = text;
        Score s = obj.getScore(entry);
        s.setScore(score);
    }

    private String getServerName() {
        return plugin.getServer().getName();
    }

    public void remove(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
