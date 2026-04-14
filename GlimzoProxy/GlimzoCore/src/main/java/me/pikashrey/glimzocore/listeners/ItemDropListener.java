package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

/** Lobby: prevent players from dropping items. */
public class ItemDropListener implements Listener {

    protected final GlimzoCore plugin;

    public ItemDropListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        // Allow staff in staff mode to drop items (so they can manage their hotbar)
        if (plugin.getStaffManager().isInStaffMode(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
    }
}
