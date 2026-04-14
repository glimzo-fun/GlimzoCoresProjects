package me.pikashrey.glimzocore120.listeners;

import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore120.GlimzoCore120;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener120 implements Listener {

    private final GlimzoCore120 plugin;

    public PlayerListener120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();

        // Suppress default join message - we'll send our own after data loads
        e.joinMessage(null);

        // Load player data async - tab/scoreboard/chat applied inside after load
        plugin.getPlayerDataManager().loadAsync(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        var player = e.getPlayer();

        e.quitMessage(null);

        // Clean up cosmetics
        plugin.getCosmeticManager().removeAll(player);
        plugin.getTablistManager().remove(player);
        plugin.getScoreboardManager().remove(player);
        plugin.getChatManager().removeNameTag(player);

        // Save and unload data
        plugin.getPlayerDataManager().unload(player.getUniqueId());
    }
}
