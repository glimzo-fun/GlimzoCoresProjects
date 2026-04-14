package me.pikashrey.glimzocore.data.staffmode;

import org.bukkit.inventory.ItemStack;

public class LastInventory {

    private final ItemStack[] contents;
    private final ItemStack[] armorContents;

    public LastInventory(ItemStack[] contents, ItemStack[] armorContents) {
        // Deep clone to avoid mutation
        this.contents      = contents      != null ? contents.clone()      : new ItemStack[36];
        this.armorContents = armorContents != null ? armorContents.clone() : new ItemStack[4];
    }

    public ItemStack[] getContents()      { return contents.clone(); }
    public ItemStack[] getArmorContents() { return armorContents.clone(); }
}

