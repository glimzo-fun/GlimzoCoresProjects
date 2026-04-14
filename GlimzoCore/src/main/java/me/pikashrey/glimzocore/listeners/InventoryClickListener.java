package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles inventory interaction in the lobby.
 *
 * Basic idea:
 * - Staff mode: can't move items (their layout is fixed)
 * - Build mode: no restrictions
 * - Creative: can only move items they placed themselves
 * - Everyone else: no inventory interaction at all
 */
public class InventoryClickListener implements Listener {

    // Hotbar (0–8) is always treated as protected
    private static final int PROTECTED_SLOTS = 9;

    private final GlimzoCore plugin;

    // Tracks slots that a creative player filled themselves
    private final java.util.Map<UUID, Set<Integer>> creativeAdded = new ConcurrentHashMap<>();

    public InventoryClickListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!isLobbyWorld(player.getWorld())) return;

        // Builders can do anything
        if (plugin.getBuildModeManager().isBuilder(player.getUniqueId())) return;

        // Only care about the player's own inventory
        InventoryType top = event.getView().getTopInventory().getType();
        boolean ownInventory = top == InventoryType.CRAFTING || top == InventoryType.PLAYER;

        if (!ownInventory) return;

        // Staff mode: block movement, allow clicks
        if (plugin.getStaffManager().isInStaffMode(player.getUniqueId())) {
            InventoryAction action = event.getAction();

            boolean moving =
                    action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                            action == InventoryAction.DROP_ALL_SLOT ||
                            action == InventoryAction.DROP_ONE_SLOT ||
                            action == InventoryAction.HOTBAR_SWAP ||
                            action == InventoryAction.HOTBAR_MOVE_AND_READD;

            if (moving) event.setCancelled(true);
            return;
        }

        // Creative handling
        if (player.getGameMode() == GameMode.CREATIVE) {
            handleCreativeClick(event, player);
            return;
        }

        // Everyone else: block everything
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!isLobbyWorld(player.getWorld())) return;

        if (plugin.getBuildModeManager().isBuilder(player.getUniqueId())) return;

        InventoryType top = event.getView().getTopInventory().getType();
        boolean ownInventory = top == InventoryType.CRAFTING || top == InventoryType.PLAYER;

        if (!ownInventory) return;

        if (player.getGameMode() == GameMode.CREATIVE) {
            for (int slot : event.getRawSlots()) {
                if (isProtectedSlot(player, slot)) {
                    event.setCancelled(true);
                    return;
                }
            }

            // mark slots as player-added
            Set<Integer> added = playerAddedSlots(player.getUniqueId());
            for (int slot : event.getRawSlots()) {
                if (slot < player.getInventory().getSize()) {
                    added.add(slot);
                }
            }
            return;
        }

        event.setCancelled(true);
    }

    private void handleCreativeClick(InventoryClickEvent event, Player player) {
        int rawSlot = event.getRawSlot();
        InventoryAction action = event.getAction();

        // Can't touch protected slots
        if (isProtectedSlot(player, rawSlot)) {
            event.setCancelled(true);
            return;
        }

        // Track player-added items
        if (action == InventoryAction.PLACE_ONE ||
                action == InventoryAction.PLACE_SOME ||
                action == InventoryAction.PLACE_ALL ||
                action == InventoryAction.SWAP_WITH_CURSOR) {

            if (rawSlot < player.getInventory().getSize()) {
                playerAddedSlots(player.getUniqueId()).add(rawSlot);
            }

        } else if (action == InventoryAction.PICKUP_ONE ||
                action == InventoryAction.PICKUP_SOME ||
                action == InventoryAction.PICKUP_ALL ||
                action == InventoryAction.PICKUP_HALF) {

            playerAddedSlots(player.getUniqueId()).remove(rawSlot);
        }
    }

    private boolean isProtectedSlot(Player player, int rawSlot) {
        // Hotbar protection
        if (rawSlot >= 36 && rawSlot <= 44) return true;
        if (rawSlot >= 0 && rawSlot < PROTECTED_SLOTS) return true;

        // If player didn't place it, treat as server item
        Set<Integer> added = creativeAdded.get(player.getUniqueId());

        if (added == null || !added.contains(rawSlot)) {
            ItemStack item = getItem(player, rawSlot);

            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                return true;
            }
        }

        return false;
    }

    private ItemStack getItem(Player player, int slot) {
        try {
            return player.getInventory().getItem(slot);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Set<Integer> playerAddedSlots(UUID uuid) {
        return creativeAdded.computeIfAbsent(
                uuid,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>())
        );
    }

    public void onQuit(UUID uuid) {
        creativeAdded.remove(uuid);
    }

    private boolean isLobbyWorld(org.bukkit.World world) {
        return world != null && world.getName().equals("world");
    }
}