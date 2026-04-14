package me.pikashrey.glimzocore.features.cosmetics.ally;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.impl.*;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyItemManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Only the EQUIPPED ally's perks are active.
 * Owning multiple allies never stacks perks.
 */
public class AllyPerkManager {

    private final GlimzoCore plugin;
    private final AllyLevelManager allyLevelManager;

    private AllyItemManager allyItemManager;
    private BukkitTask tickTask;

    // playerUUID -> single active perk (only the equipped ally)
    private final Map<UUID, AllyLevel> activePerks  = new ConcurrentHashMap<>();
    // playerUUID -> which AllyType is currently equipped
    private final Map<UUID, AllyType>  equippedAlly = new ConcurrentHashMap<>();

    public AllyPerkManager(GlimzoCore plugin, AllyLevelManager allyLevelManager) {
        this.plugin = plugin;
        this.allyLevelManager = allyLevelManager;
        startTickTask();
    }


    /** onPlayerJoin is intentionally empty - CosmeticManager calls onAllyEquipped after loading state. */
    public void setAllyItemManager(AllyItemManager aim) { this.allyItemManager = aim; }

    /** onPlayerJoin is intentionally empty - CosmeticManager calls onAllyEquipped after loading state. */
    public void onPlayerJoin(Player player) {}

    public void onPlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        equippedAlly.remove(uuid);
        AllyLevel perk = activePerks.remove(uuid);
        if (perk != null) try { perk.onLevelDown(); } catch (Exception ignored) {}
    }


    /** Equips an ally: deactivates any previous perk, activates this one. */
    public void onAllyEquipped(Player player, AllyType ally) {
        UUID uuid = player.getUniqueId();
        int level = allyLevelManager.getAllyLevel(uuid, ally);
        if (level <= 0) return; // owned but no level yet

        deactivateCurrent(uuid);

        AllyLevel perk = createAllyLevel(ally, player, level);
        if (perk == null) return;
        activePerks.put(uuid, perk);
        equippedAlly.put(uuid, ally);
        perk.onLevelUp();
        if (allyItemManager != null) allyItemManager.giveItem(player, ally);
        // Sync ally skull to hotbar slot 2
        me.pikashrey.glimzocore.features.hotbar.HotbarManager hm =
                me.pikashrey.glimzocore.GlimzoCore.getInstance().getHotbarManager();
        if (hm != null) hm.updateAllySlot(player);
    }

    /** Called right after purchase - immediately equips the perk. */
    public void onAllyUnlocked(Player player, AllyType ally) {
        onAllyEquipped(player, ally);
    }

    /** Unequips: removes perks only if this ally is the one currently equipped. */
    public void onAllyUnequipped(Player player, AllyType ally) {
        UUID uuid = player.getUniqueId();
        if (ally.equals(equippedAlly.get(uuid))) {
            deactivateCurrent(uuid);
        }
    }


    public void upgradePerk(Player player, AllyType allyType) {
        UUID uuid = player.getUniqueId();
        int newLevel = allyLevelManager.getAllyLevel(uuid, allyType) + 1;
        if (newLevel > 3) { player.sendMessage("§cAlly is already at max level!"); return; }

        allyLevelManager.setAllyLevel(uuid, allyType, newLevel);

        // Refresh perk only if this is the equipped ally
        if (allyType.equals(equippedAlly.get(uuid))) {
            deactivateCurrent(uuid);
            AllyLevel perk = createAllyLevel(allyType, player, newLevel);
            if (perk != null) {
                activePerks.put(uuid, perk);
                equippedAlly.put(uuid, allyType);
                perk.onLevelUp();
            }
        }

        // Also update the visual level on the currently spawned ally (Charizard/Falcon scale)
        me.pikashrey.glimzocore.features.cosmetics.ally.AllyManager am =
                plugin.getCosmeticManager().getAllyManager();
        if (am != null) {
            me.pikashrey.glimzocore.features.cosmetics.ally.Ally spawned = am.getAlly(player);
            if (spawned instanceof me.pikashrey.glimzocore.features.cosmetics.ally.impl.CharizardAlly)
                ((me.pikashrey.glimzocore.features.cosmetics.ally.impl.CharizardAlly) spawned).setLevel(newLevel);
            if (spawned instanceof me.pikashrey.glimzocore.features.cosmetics.ally.impl.FalconAlly)
                ((me.pikashrey.glimzocore.features.cosmetics.ally.impl.FalconAlly) spawned).setLevel(newLevel);
        }
    }

    public boolean feedAlly(Player player, AllyType allyType, int mealCount) {
        UUID uuid = player.getUniqueId();
        int levelUp = allyLevelManager.addPetMeals(uuid, allyType, mealCount);
        if (levelUp > 0) {
            upgradePerk(player, allyType);
            player.sendMessage("§a✓ §f" + allyType.getDisplayName()
                    + " §aleveled up to §6Level " + levelUp + "§a!");
            return true;
        }
        int meals  = allyLevelManager.getAllyPetMeals(uuid, allyType);
        int needed = plugin.getConfig().getInt("allies.meals-per-level", 10);
        player.sendMessage("§7Fed §f" + mealCount + " PetMeals §7to §d"
                + allyType.getDisplayName() + "§7. Progress: §a" + meals + "§7/§a" + needed + "§7.");
        return false;
    }

    public int getAllyLevel(Player player, AllyType allyType) {
        return allyLevelManager.getAllyLevel(player.getUniqueId(), allyType);
    }

    public AllyType getEquippedAlly(UUID uuid) { return equippedAlly.get(uuid); }


    private void deactivateCurrent(UUID uuid) {
        equippedAlly.remove(uuid);
        AllyLevel old = activePerks.remove(uuid);
        if (old != null) try { old.onLevelDown(); } catch (Exception ignored) {}
        // Remove skull item from hotbar
        if (allyItemManager != null) {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(uuid);
            if (p != null) {
                allyItemManager.removeItem(p);
                // Clear ally skull from hotbar slot 2
                me.pikashrey.glimzocore.features.hotbar.HotbarManager hm =
                        me.pikashrey.glimzocore.GlimzoCore.getInstance().getHotbarManager();
                if (hm != null) hm.updateAllySlot(p);
            }
        }
    }

    private void startTickTask() {
        tickTask = new BukkitRunnable() {
            @Override public void run() {
                for (AllyLevel perk : activePerks.values()) {
                    try {
                        if (perk.getOwner() != null && perk.getOwner().isOnline())
                            perk.onTick();
                    } catch (Exception ignored) {}
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private AllyLevel createAllyLevel(AllyType type, Player player, int level) {
        switch (type) {
            case CHARIZARD: return new CharizardLevel(type, player, level);
            case DR_DUCKY:  return new DrDuckyLevel(type, player, level);
            case FALCON:    return new FalconLevel(type, player, level);
            case KITTY:     return new KittyLevel(type, player, level);
            case MR_PANDA:  return new MrPandaLevel(type, player, level);
            case PUG:       return new PugLevel(type, player, level);
            default:        return null;
        }
    }

    public void disable() {
        if (tickTask != null) { tickTask.cancel(); tickTask = null; }
        for (AllyLevel perk : activePerks.values())
            try { perk.onLevelDown(); } catch (Exception ignored) {}
        activePerks.clear();
        equippedAlly.clear();
    }
}