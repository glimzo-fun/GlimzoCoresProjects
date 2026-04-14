package me.pikashrey.glimzocore.features.scoreboard;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager implements org.bukkit.event.Listener {

    protected final GlimzoCore plugin;

    private final Map<UUID, Scoreboard> boards     = new ConcurrentHashMap<>();
    private final Map<UUID, Objective>  objectives = new ConcurrentHashMap<>();

    // Safety fallback task   fires every 5 minutes to catch anything events missed
    private BukkitTask fallbackTask;

    public ScoreboardManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        fallbackTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getSettingsManager().hasScoreboardEnabled(p.getUniqueId())) {
                    update(p);
                } else {
                    resetBoard(p);
                }
            }
        }, 20L, 6000L); // every 5 minutes
    }

    public void disable() {
        if (fallbackTask != null) fallbackTask.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) resetBoard(p);
        boards.clear();
        objectives.clear();
    }

    public void onJoin(Player player) {
        if (!plugin.getSettingsManager().hasScoreboardEnabled(player.getUniqueId())) return;
        update(player);
    }

    public void onQuit(UUID uuid) {
        boards.remove(uuid);
        objectives.remove(uuid);
    }

    //Board rendering(triggers come from SyncManager)

    private void buildBoard(Player player) {
        // Use the player's existing scoreboard (set by NameTagBoard) so nametag
        // team prefixes are not wiped. Only create a new one if none is assigned.
        Scoreboard board = player.getScoreboard();
        if (board == null || board.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        // Guard against duplicate objective if this scoreboard was already used
        Objective obj = board.getObjective("glimzo");
        if (obj == null) {
            obj = board.registerNewObjective("glimzo", "dummy");
        }
        obj.setDisplayName(CC.translate("&2&l🌿 GLIMZO"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        boards.put(player.getUniqueId(), board);
        objectives.put(player.getUniqueId(), obj);
        player.setScoreboard(board);
    }

    public void update(Player player) {
        UUID uuid = player.getUniqueId();
        if (!boards.containsKey(uuid)) buildBoard(player);

        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return;

        RankRef rank = plugin.getRankManager().getActiveRankRef(player);
        me.pikashrey.glimzocore.api.clan.ClanData clan = plugin.getClanManager().getClanByPlayer(uuid);

        Scoreboard board = boards.get(uuid);
        Objective  obj   = objectives.get(uuid);

        for (String entry : board.getEntries()) board.resetScores(entry);

        String clanLine = clan != null ? " &7Clan  &8» &a[" + clan.getTag() + "]" : " &7Clan  &8» &8None";

        setLine(board, obj, CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━"), 10);
        setLine(board, obj, CC.translate(" &7Rank  &8» " + rank.getColorCode() + rank.getDisplayName()), 9);
        setLine(board, obj, CC.translate(" &7Level &8» &a" + data.getLevel()
                + (data.getPrestige() > 0 ? " &6✦" + data.getPrestige() : "")), 8);
        setLine(board, obj, CC.translate(" "), 7);
        setLine(board, obj, CC.translate(" &7Coins &8» &e" + data.getCoins()), 6);
        setLine(board, obj, CC.translate(" &7Gems  &8» &d" + data.getGems()), 5);
        setLine(board, obj, CC.translate("  "), 4);
        setLine(board, obj, CC.translate(clanLine), 3);
        setLine(board, obj, CC.translate("   "), 2);
        setLine(board, obj, CC.translate("&2&lplay.glimzo.fun"), 1);
        setLine(board, obj, CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━"), 0);
    }

    private void setLine(Scoreboard board, Objective obj, String text, int score) {
        Score s = obj.getScore(text);
        s.setScore(score);
    }

    private void resetBoard(Player player) {
        boards.remove(player.getUniqueId());
        objectives.remove(player.getUniqueId());
        // Don't reset to main scoreboard on quit - just let it be garbage collected
    }
}