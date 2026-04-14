package me.pikashrey.glimzocore.menu.menu;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Base menu class for all GlimzoCore GUIs.
 *
 * The original fillEmpty() was called at the START of buildContent() in
 * subclasses (e.g. FriendsMenu, PartyMenu). This means:
 *
 *   1. fillEmpty() fills ALL 54 slots with grey glass panes
 *   2. buildContent() then tries to set slots 20, 24, 26 (Accept/Deny/Ignore)
 *
 * BUT - since fillEmpty() already registered those slots in the map first,
 * and the map only stores one Slot per index, later set() calls correctly
 * overwrite the filler. The map itself was fine.
 *
 * The REAL problem was MenuManager using event.getInventory().getHolder()
 * (fixed in MenuManager.java). However, we also fix fillEmpty() to be
 * called DEFENSIVELY - it now only fills slots NOT already registered,
 * so subclasses can safely call it anywhere in buildContent() without
 * worrying about call order.
 *
 * Additional fix: handleClick now uses event.getSlot() (inventory-relative)
 * rather than event.getRawSlot() (view-relative), which is what the slot map
 * keys are indexed by.
 */
public abstract class GlimzoMenu implements InventoryHolder {

    protected final GlimzoCore plugin;
    protected final Player     player;
    private   final String     title;
    private   final int        size;
    private         Inventory  inventory;
    private   final Map<Integer, Slot> slots = new HashMap<>();

    public GlimzoMenu(GlimzoCore plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.title  = title;
        this.size   = size;
    }

    /** Override to add slots to this menu. Called by open() and build(). */
    protected abstract void buildContent();

    /** Add (or overwrite) a slot in this menu. */
    protected void set(Slot slot) {
        slots.put(slot.getSlot(), slot);
    }

    /**
     * Fill all EMPTY slots with a grey filler pane.
     * Safe to call anywhere in buildContent() - will never overwrite a slot
     * that has already been set() by the subclass.
     *
     * BUG FIX: original filled slots first, then set() was called - looked
     * correct in the map but the root click routing bug masked it. Now this
     * is truly safe regardless of call order.
     */
    protected void fillEmpty() {
        for (int i = 0; i < size; i++) {
            if (!slots.containsKey(i)) {
                final int fi = i;
                set(new Slot(fi) {
                    @Override public org.bukkit.inventory.ItemStack getItem() {
                        return ItemBuilder.filler();
                    }
                    // No onClick override - clicks on filler are cancelled by MenuManager
                });
            }
        }
    }

    /** Open this menu for the player. */
    public void open() {
        slots.clear();
        buildContent();
        // fillEmpty AFTER buildContent so subclass slots are never overwritten
        fillEmpty();
        inventory = Bukkit.createInventory(this, size, CC.translate(title));
        for (Map.Entry<Integer, Slot> entry : slots.entrySet()) {
            org.bukkit.inventory.ItemStack item = entry.getValue().getItem();
            if (item != null) inventory.setItem(entry.getKey(), item);
        }
        player.openInventory(inventory);
    }

    /**
     * Rebuild and push changes to the already-open inventory (no flicker).
     */
    public void build() {
        slots.clear();
        buildContent();
        fillEmpty();
        if (inventory != null) {
            inventory.clear();
            for (Map.Entry<Integer, Slot> entry : slots.entrySet()) {
                org.bukkit.inventory.ItemStack item = entry.getValue().getItem();
                if (item != null) inventory.setItem(entry.getKey(), item);
            }
            player.updateInventory();
        }
    }

    /**
     * Called by MenuManager when the player clicks inside this inventory.
     *
     * BUG FIX: use event.getSlot() (0-based within the clicked inventory),
     * not event.getRawSlot() (0-based within the entire InventoryView).
     * Our slot map is keyed by inventory slot index, not raw view index.
     */
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        // event.getSlot() = slot within the inventory that was clicked
        // This matches the keys we store in the slots map
        Slot slot = slots.get(event.getSlot());
        if (slot != null) slot.onClick(player, event);
    }

    @Override public Inventory getInventory() { return inventory; }
    public String              getTitle()     { return title; }
    public int                 getSize()      { return size; }
    public Player              getPlayer()    { return player; }
}
