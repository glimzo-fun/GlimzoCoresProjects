package me.pikashrey.glimzocore.menu.slots;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class Slot {

    private final int slot;

    public Slot(int slot) {
        this.slot = slot;
    }

    /** The item to display in this slot. */
    public abstract ItemStack getItem();

    /** Called when a player clicks this slot. */
    public void onClick(Player player, InventoryClickEvent event) {}

    public int getSlot() { return slot; }
}

