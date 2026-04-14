package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.impl.essential.staff.FreezeCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    protected final GlimzoCore plugin;

    public PlayerMoveListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Only cancel if the player actually changed block position
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        // Check frozen state via the static registry on FreezeCommand
        if (FreezeCommand.isFrozenStatic(event.getPlayer().getUniqueId())) {
            event.setTo(event.getFrom());
        }
    }
}
