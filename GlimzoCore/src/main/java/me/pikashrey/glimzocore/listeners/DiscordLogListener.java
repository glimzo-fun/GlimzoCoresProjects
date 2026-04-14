package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class DiscordLogListener implements Listener {

    protected final GlimzoCore plugin;

    public DiscordLogListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getDiscordManager().isEnabled()) return;
        if (!event.getPlayer().hasPermission("glimzo.staff.mode")) return;

        String msg = event.getMessage();
        // Only log staff-relevant commands
        if (msg.startsWith("/ban") || msg.startsWith("/mute") || msg.startsWith("/kick")
                || msg.startsWith("/warn") || msg.startsWith("/freeze")
                || msg.startsWith("/rank") || msg.startsWith("/unban")
                || msg.startsWith("/unmute")) {
            plugin.getDiscordManager().sendStaffAlert(
                    "Staff Command",
                    "**" + event.getPlayer().getName() + "** executed: `" + msg + "`");
        }
    }
}
