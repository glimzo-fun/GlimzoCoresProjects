package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/** Lobby: suppress death messages, award XP to clan, respawn at spawn. */
public class PlayerDeathListener implements Listener {

    protected final GlimzoCore plugin;

    public PlayerDeathListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location spawn = player.getWorld().getSpawnLocation();
        event.setRespawnLocation(spawn);
        // Restore hotbar items 1 tick after respawn (inventory resets on respawn)
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (plugin.getHotbarManager() != null) plugin.getHotbarManager().giveHotbar(player);
        }, 1L);
    }
}
