package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/** Lobby world protection: no explosions, no mob/animal spawning, no weather. */
public class WorldListener implements Listener {

    protected final GlimzoCore plugin;

    public WorldListener(GlimzoCore plugin) {
        this.plugin = plugin;
        // Kill mobs that were already in the world before the plugin loaded.
        // Delay 40 ticks so worlds are fully loaded when we iterate.
        Bukkit.getScheduler().runTaskLater(plugin, this::clearExistingMobs, 40L);
    }

    /**
     * Remove all non-player living entities that exist in the world at startup.
     * CreatureSpawnEvent only prevents NEW spawns - anything already loaded
     * from the region files (saved mobs, lingering slimes, etc.) stays alive
     * until explicitly removed. Ally-managed entities are exempt because they
     * haven't been spawned yet at this point (cosmetics load after players join).
     */
    private void clearExistingMobs() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;
                if (entity instanceof LivingEntity) {
                    entity.remove();
                    removed++;
                }
            }
        }
        if (removed > 0) {
            plugin.log("&7[WorldListener] Cleared " + removed + " existing mob(s) from world.");
        }
    }

    /** Cancel all block damage from explosions. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().clear();
        event.setCancelled(true);
    }

    /**
     * Cancel ALL creature/animal spawns regardless of reason.
     * The old code only blocked NATURAL and SPAWNER - this missed breeding,
     * eggs, jockeys, village invasions, plugin-spawned mobs, etc.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Allow plugin-spawned entities (CUSTOM reason) - used by ally system (KittyAlly ocelot)
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        event.setCancelled(true);
    }

    /**
     * Block players from interacting with ally ocelots (KittyAlly).
     * Without this, players can try to tame, leash, or feed the ocelot.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof org.bukkit.entity.Ocelot) {
            org.bukkit.entity.Ocelot ocelot = (org.bukkit.entity.Ocelot) event.getRightClicked();
            if (!ocelot.isCustomNameVisible() && !ocelot.getRemoveWhenFarAway()) {
                event.setCancelled(true);
            }
        }
    }

    /** Keep lobby sky permanently clear - cancel any transition to rainy/stormy. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeather(WeatherChangeEvent event) {
        if (event.toWeatherState()) event.setCancelled(true);
    }

    /** Belt-and-suspenders: also block thunder explicitly. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThunder(ThunderChangeEvent event) {
        if (event.toThunderState()) event.setCancelled(true);
    }

    /** Prevent random item entities from littering the lobby floor. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity().getEntityId() < 0) return;
        if (!event.getEntity().hasMetadata("player_dropped")) {
            event.setCancelled(true);
        }
    }

    /** Prevent slimes from multiplying in any corner-case where one slips through. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSlimeSplit(SlimeSplitEvent event) {
        event.setCancelled(true);
    }
}
