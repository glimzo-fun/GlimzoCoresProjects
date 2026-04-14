package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class InventoryListener implements Listener {

    protected final GlimzoCore plugin;

    public InventoryListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(PlayerPickupItemEvent event) {
        if (!isLobbyWorld(event.getPlayer().getWorld())) return;
        event.setCancelled(true);
    }

    private boolean isLobbyWorld(org.bukkit.World world) {
        return world != null && world.getName().equals("world");
    }
}