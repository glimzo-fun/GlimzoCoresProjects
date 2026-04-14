package me.pikashrey.glimzocore.features.cosmetics.aura;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.aura.impl.*;

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

public class AuraManager implements Listener {

    private final GlimzoCore plugin;
    private final Map<String, BaseAura>  registry = new LinkedHashMap<>();
    private final Map<UUID, BaseAura>    active   = new java.util.concurrent.ConcurrentHashMap<>();
    private BukkitTask task;

    public AuraManager(GlimzoCore plugin) {
        this.plugin = plugin;
        register(new InfernoAura());
        register(new MagicAura());
        register(new RainbowAura());
        register(new IceAura());
        register(new NatureAura());
        register(new LightningAura());

        // Register disconnect listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Copy to avoid modification during iteration
            java.util.List<UUID> toRemove = new java.util.ArrayList<>();
            for (Map.Entry<UUID, BaseAura> entry : active.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    entry.getValue().tick(player);
                } else {
                    toRemove.add(entry.getKey());
                }
            }
            for (UUID uuid : toRemove) {
                BaseAura aura = active.remove(uuid);
                if (aura != null) aura.unequip(null);
            }
        }, 0L, 2L); // tick every 2 server ticks (~100ms)
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // Use unequip() so cleanup logic (particles off, etc.) runs properly
        for (Map.Entry<UUID, BaseAura> entry : active.entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                entry.getValue().unequip(player);
            }
        }
        active.clear();
    }

    /**
     * Called when a player disconnects - clean up their aura
     */

    private void register(BaseAura aura) {
        registry.put(aura.getId(), aura);
    }

    /**
     * Equip an aura for a player with permission validation
     */
    public boolean equip(Player player, String auraId) {
        BaseAura aura = registry.get(auraId);
        if (aura == null) {
            return false;  // Aura not found
        }

        // Remove existing aura first
        unequip(player);

        // Equip new aura
        aura.equip(player);
        active.put(player.getUniqueId(), aura);
        return true;
    }

    /**
     * Unequip the player's current aura
     */
    public void unequip(Player player) {
        BaseAura aura = active.remove(player.getUniqueId());
        if (aura != null) {
            aura.unequip(player);
        }
    }

    public boolean hasActive(Player player) {
        return active.containsKey(player.getUniqueId());
    }

    public BaseAura getActive(Player player) {
        return active.get(player.getUniqueId());
    }

    public Map<String, BaseAura> getRegistry() {
        return new HashMap<>(registry);
    }
}