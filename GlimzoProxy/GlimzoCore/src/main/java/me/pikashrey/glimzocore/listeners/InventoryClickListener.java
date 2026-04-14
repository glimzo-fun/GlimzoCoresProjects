package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevents all inventory manipulation in the lobby player inventory.
 *
 * Rules:
 *   - Staff in staff mode: fully blocked (staff items are fixed).
 *   - Build-mode admins: fully allowed (they manage their own inventory).
 *   - Creative players: may only move items they themselves added.
 *     Items pre-placed by the server (slot < PROTECTED_SLOTS) are locked.
 *   - All other players: every click/drag in their own inventory is cancelled.
 */
public class InventoryClickListener implements Listener {

    /**
     * Slots 0-8 (hotbar) are considered "server-placed" for creative players.
     * Creative admins cannot remove these, but CAN move items they added
     * to slots 9-35 (main inventory area).
     */
    private static final int PROTECTED_SLOTS = 9;

    /**
     * UUIDs of players currently in build mode - we allow them full
     * inventory access so they can manage their tools.
     */
    protected final GlimzoCore plugin;

    /**
     * Track which inventory slots a creative player filled themselves
     * (vs items the server placed).  Keyed by UUID.
     * We store a Set<Integer> of slot indices that the player added.
     */
    private final java.util.Map<UUID, Set<Integer>> creativeAdded =
            new ConcurrentHashMap<>();

    public InventoryClickListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Build mode admins: no restrictions
        if (plugin.getBuildModeManager().isBuilder(player.getUniqueId())) return;

        // Only restrict the player's own inventory (not custom GUIs like menus)
        InventoryType topType = event.getView().getTopInventory().getType();
        boolean isOwnInventory = topType == InventoryType.CRAFTING
                              || topType == InventoryType.PLAYER;

        if (!isOwnInventory) return; // custom GUIs handled by MenuManager

        // Staff mode: allow clicking but block item movement (staff items are fixed slots)
        // Actual staff item actions are handled via PlayerInteractEvent, not inventory clicks.
        if (plugin.getStaffManager().isInStaffMode(player.getUniqueId())) {
            // Only block moves/drops - allow clicks that open menus (handled elsewhere)
            org.bukkit.event.inventory.InventoryAction act = event.getAction();
            boolean isMoving = act == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || act == org.bukkit.event.inventory.InventoryAction.DROP_ALL_SLOT
                    || act == org.bukkit.event.inventory.InventoryAction.DROP_ONE_SLOT
                    || act == org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP
                    || act == org.bukkit.event.inventory.InventoryAction.HOTBAR_MOVE_AND_READD;
            if (isMoving) event.setCancelled(true);
            return;
        }

        // Creative mode: allow moving items the player added, lock server items
        if (player.getGameMode() == GameMode.CREATIVE) {
            handleCreativeClick(event, player);
            return;
        }

        // Everyone else: block all inventory interaction
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (plugin.getBuildModeManager().isBuilder(player.getUniqueId())) return;

        InventoryType topType = event.getView().getTopInventory().getType();
        boolean isOwnInventory = topType == InventoryType.CRAFTING
                              || topType == InventoryType.PLAYER;
        if (!isOwnInventory) return;

        if (player.getGameMode() == GameMode.CREATIVE) {
            // Allow drag only into non-protected slots
            for (int slot : event.getRawSlots()) {
                if (isProtectedSlot(player, slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
            // Mark the dragged-into slots as player-added
            Set<Integer> added = playerAddedSlots(player.getUniqueId());
            for (int slot : event.getRawSlots()) {
                if (slot < player.getInventory().getSize()) added.add(slot);
            }
            return;
        }

        event.setCancelled(true);
    }


    private void handleCreativeClick(InventoryClickEvent event, Player player) {
        int rawSlot    = event.getRawSlot();
        int slot       = event.getSlot();
        InventoryAction action = event.getAction();

        // Moving items OUT of a protected slot - block it
        if (isProtectedSlot(player, rawSlot)) {
            // The only allowed action is placing something INTO the slot from cursor
            // (which would replace it - also block).
            event.setCancelled(true);
            return;
        }

        // Moving items from a non-protected slot - allowed
        // Track which slots they fill so we don't accidentally re-protect them
        if (action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME
                || action == InventoryAction.PLACE_ALL
                || action == InventoryAction.SWAP_WITH_CURSOR) {
            if (rawSlot < player.getInventory().getSize()) {
                playerAddedSlots(player.getUniqueId()).add(rawSlot);
            }
        } else if (action == InventoryAction.PICKUP_ONE
                || action == InventoryAction.PICKUP_SOME
                || action == InventoryAction.PICKUP_ALL
                || action == InventoryAction.PICKUP_HALF) {
            // Un-track when they pick up
            playerAddedSlots(player.getUniqueId()).remove(rawSlot);
        }
        // Allow the action to proceed
    }

    /**
     * A slot is "protected" (server-placed) if:
     *   - It's in the hotbar (slots 0-8, raw 36-44 in CRAFTING view), OR
     *   - The player never added anything there themselves.
     */
    private boolean isProtectedSlot(Player player, int rawSlot) {
        // Convert raw slot to inventory slot
        int invSlot = rawSlot;

        // In CRAFTING view raw 36-44 = hotbar slots 0-8
        // Raw 9-35 = main inv slots 9-35
        // Raw 45 = offhand

        // Hotbar is always protected
        if (invSlot >= 36 && invSlot <= 44) return true; // hotbar in CRAFTING view
        if (invSlot >= 0 && invSlot < PROTECTED_SLOTS) return true; // direct hotbar

        // If the player hasn't added anything to this slot, it's server-placed
        Set<Integer> added = creativeAdded.get(player.getUniqueId());
        if (added == null || !added.contains(rawSlot)) {
            ItemStack current = event_item_at(player, rawSlot);
            if (current != null && current.getType() != org.bukkit.Material.AIR) {
                return true; // server put something here and player hasn't touched it
            }
        }
        return false;
    }

    private ItemStack event_item_at(Player player, int rawSlot) {
        try { return player.getInventory().getItem(rawSlot); } catch (Exception e) { return null; }
    }

    private Set<Integer> playerAddedSlots(UUID uuid) {
        return creativeAdded.computeIfAbsent(uuid,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    /** Call this on player quit to clean up the tracking map. */
    public void onQuit(UUID uuid) {
        creativeAdded.remove(uuid);
    }
}
