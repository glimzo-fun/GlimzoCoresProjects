package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/** Lobby: cancel all damage so players cannot be hurt. */
public class EntityDamageListener implements Listener {

    protected final GlimzoCore plugin;

    public EntityDamageListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
}
