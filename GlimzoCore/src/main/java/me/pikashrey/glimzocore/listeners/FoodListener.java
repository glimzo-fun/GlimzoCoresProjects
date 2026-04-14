package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/** Lobby: prevent hunger loss. */
public class FoodListener implements Listener {

    protected final GlimzoCore plugin;

    public FoodListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if(!plugin.getHelpers().isLobbyWorld(event.getEntity().getWorld())) return;
        event.setCancelled(true);
        ((Player) event.getEntity()).setFoodLevel(20);
    }
}
