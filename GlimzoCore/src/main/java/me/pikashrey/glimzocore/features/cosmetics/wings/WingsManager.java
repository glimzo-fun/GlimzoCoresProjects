package me.pikashrey.glimzocore.features.cosmetics.wings;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.wings.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class WingsManager implements Listener {

    private final GlimzoCore plugin;
    private final Map<String, BaseWings> registry = new LinkedHashMap<>();
    private final Map<UUID, BaseWings>   active   = new java.util.concurrent.ConcurrentHashMap<>();
    private BukkitTask task;

    public WingsManager(GlimzoCore plugin) {
        this.plugin = plugin;
        register(new AngelWings());
        register(new DragonWings());
        register(new PhoenixWings());
        register(new DemonWings());
        register(new ButterflyWings());
        register(new EnergyWings());

        // Register disconnect listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        // Every 2 ticks (10 updates/sec) - smooth but not spammy
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Safe iteration with copy to avoid ConcurrentModificationException
            java.util.List<UUID> toRemove = new java.util.ArrayList<>();
            for (Map.Entry<UUID, BaseWings> entry : active.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    entry.getValue().tick(player);
                } else {
                    toRemove.add(entry.getKey());
                }
            }
            for (UUID uuid : toRemove) {
                BaseWings wings = active.remove(uuid);
                if (wings != null) wings.cleanup();
            }
        }, 0L, 2L);  // Changed from 1L to 2L for better performance
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // Use unequip() so destroy packets are sent to online players,
        // preventing ghost armor stands after /reload or shutdown
        for (Map.Entry<UUID, BaseWings> entry : active.entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                entry.getValue().unequip(player);
            } else {
                entry.getValue().cleanup();
            }
        }
        active.clear();
    }

    /**
     * Called when a player disconnects - clean up their wings
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unequip(event.getPlayer());
    }

    /** When a player joins, show them all active wings around them. */
    public void onPlayerJoin(Player joiner) {
        for (Map.Entry<UUID, BaseWings> entry : active.entrySet()) {
            Player wearer = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (wearer == null || !wearer.isOnline()) continue;
            if (!wearer.getWorld().equals(joiner.getWorld())) continue;
            entry.getValue().spawnForNewViewer(wearer, joiner);
        }
    }

    private void register(BaseWings wings) {
        registry.put(wings.getId(), wings);
    }

    /**
     * Equip wings for a player with permission validation
     */
    public boolean equip(Player player, String wingsId) {
        BaseWings wings = registry.get(wingsId);
        if (wings == null) {
            return false;  // Wings not found
        }

        // Remove existing wings first
        unequip(player);

        // Equip new wings
        wings.equip(player);
        active.put(player.getUniqueId(), wings);
        return true;
    }

    /**
     * Unequip the player's current wings
     */
    public void unequip(Player player) {
        BaseWings wings = active.remove(player.getUniqueId());
        if (wings != null) {
            wings.unequip(player);
        }
    }

    public boolean hasActive(Player player)  { return active.containsKey(player.getUniqueId()); }
    public BaseWings getActive(Player player)  { return active.get(player.getUniqueId()); }
    public Map<String, BaseWings> getRegistry(){ return new HashMap<>(registry); }
}