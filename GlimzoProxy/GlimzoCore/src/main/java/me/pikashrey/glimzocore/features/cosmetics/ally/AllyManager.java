package me.pikashrey.glimzocore.features.cosmetics.ally;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AllyManager {

    private final GlimzoCore         plugin;
    private final Map<UUID, Ally>    active = new ConcurrentHashMap<>();
    private BukkitTask task;

    public AllyManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // BUG #9 FIX: Iterating active.entrySet() and calling active.remove() inside the
            // loop caused ConcurrentModificationException with ConcurrentHashMap iterators.
            // Collect stale keys first, then remove after iteration.
            List<UUID> toRemove = new ArrayList<>();

            for (Map.Entry<UUID, Ally> entry : active.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    entry.getValue().remove();
                    toRemove.add(entry.getKey());
                    continue;
                }
                entry.getValue().tick();
            }

            for (UUID uuid : toRemove) {
                active.remove(uuid);
            }
        }, 0L, 2L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        removeAll();
    }

    /**
     * BUG #6 FIX: The old spawnAlly() had a permission check that silently
     * returned false for players who purchased an ally but had no permission node.
     * Purchased allies should spawn based on unlock ownership, not permissions.
     *
     * This method is used internally by the GUI (ownership already verified).
     * The permission-gated version is kept as spawnAllyWithPermCheck() for
     * any code that still needs the old behaviour.
     */
    public Ally createAndSpawn(Player player, AllyType type) {
        removeAlly(player); // despawn any currently active ally
        Ally ally = createAlly(type, player);
        // Apply the player's current level to allies that support scaling
        int level = 1;
        me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager pm =
                plugin.getCosmeticManager().getAllyPerkManager();
        if (pm != null) level = Math.max(1, pm.getAllyLevel(player, type));
        if (ally instanceof me.pikashrey.glimzocore.features.cosmetics.ally.impl.CharizardAlly)
            ((me.pikashrey.glimzocore.features.cosmetics.ally.impl.CharizardAlly) ally).setLevel(level);
        if (ally instanceof me.pikashrey.glimzocore.features.cosmetics.ally.impl.FalconAlly)
            ((me.pikashrey.glimzocore.features.cosmetics.ally.impl.FalconAlly) ally).setLevel(level);
        ally.spawn();
        active.put(player.getUniqueId(), ally);
        return ally;
    }

    /** Original permission-gated spawn - kept for backwards compatibility. */
    public boolean spawnAlly(Player player, AllyType type) {
        String perm = "glimzo.ally." + type.getId();
        if (!player.hasPermission(perm) && !player.hasPermission("glimzo.ally.*")) {
            // BUG #10 FIX: Previously returned false silently.
            // Now falls through to ownership check via CosmeticUnlockManager.
            // If the player owns the ally (purchased via gems), spawn it anyway.
            boolean owned = false;
            me.pikashrey.glimzocore.features.cosmetics.CosmeticUnlockManager um =
                    plugin.getCosmeticManager().getUnlockManager();
            if (um != null) owned = um.hasUnlock(player.getUniqueId(), type.getId());
            if (!owned) return false;
        }
        createAndSpawn(player, type);
        return true;
    }

    public void removeAlly(Player player) {
        Ally ally = active.remove(player.getUniqueId());
        if (ally != null) ally.remove();
    }

    public Ally getAlly(Player player) {
        return active.get(player.getUniqueId());
    }

    public boolean hasAlly(Player player) {
        return active.containsKey(player.getUniqueId());
    }

    public void removeAll() {
        for (Ally ally : active.values()) {
            ally.remove();
        }
        active.clear();
    }

    private Ally createAlly(AllyType type, Player player) {
        switch (type) {
            case MR_PANDA:  return new MrPandaAlly(player);
            case FALCON:    return new FalconAlly(player);
            case KITTY:     return new KittyAlly(player);
            case PUG:       return new PugAlly(player);
            case CHARIZARD: return new CharizardAlly(player);
            case DR_DUCKY:  return new DrDuckyAlly(player);
            default: throw new IllegalArgumentException("Unknown AllyType: " + type);
        }
    }
}