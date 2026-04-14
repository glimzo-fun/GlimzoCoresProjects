package me.pikashrey.glimzocore120.chat;

import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore120.GlimzoCore120;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager120 implements Listener {

    private final GlimzoCore120 plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ChatManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Rank rank     = plugin.getRankManager().getRank(player);
        String prefix = rank.getChatPrefix();

        // Format: [PREFIX] PlayerName: message
        // Using legacy color codes from rank prefix
        e.setFormat(prefix + " §f" + player.getName() + " §8» §f%2$s");
    }

    /** Apply nametag above player head using 1.20 scoreboard teams */
    public void applyNameTag(Player player) {
        Rank rank     = plugin.getRankManager().getRank(player);
        String prefix = rank.getChatPrefix();

        // Use scoreboard team for nametag prefix
        var scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        var teamName   = "rank_" + player.getUniqueId().toString().substring(0, 8);

        var team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);

        // Convert legacy color codes to Adventure component
        Component prefixComponent = LegacyComponentSerializer.legacyAmpersand()
                .deserialize(prefix + " ");

        team.prefix(prefixComponent);
        team.addEntry(player.getName());
        player.setScoreboard(scoreboard);
    }

    public void removeNameTag(Player player) {
        var scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        var teamName   = "rank_" + player.getUniqueId().toString().substring(0, 8);
        var team       = scoreboard.getTeam(teamName);
        if (team != null) team.unregister();
    }
}
