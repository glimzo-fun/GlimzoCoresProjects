package me.pikashrey.glimzocore.data.staffmode;

import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StaffModeItem {

    private final int       slot;
    private final ItemStack item;

    public StaffModeItem(int slot, ItemStack item) {
        this.slot = slot;
        this.item = item;
    }

    public int       getSlot() { return slot; }
    public ItemStack getItem() { return item.clone(); }

    /** Returns the standard staff mode item set. */
    public static List<StaffModeItem> getItems() {
        return Collections.unmodifiableList(Arrays.asList(
                new StaffModeItem(0, ItemBuilder.of(Material.COMPASS,       "&bInspect &7- Left click to freeze, Right click to check")),
                new StaffModeItem(1, ItemBuilder.of(Material.BOOK,          "&ePlayer List &7- View online players")),
                new StaffModeItem(2, ItemBuilder.of(Material.ENDER_PEARL,   "&dRandom Teleport &7- Teleport to a random player")),
                new StaffModeItem(4, ItemBuilder.of(Material.EYE_OF_ENDER,  "&aVanish Toggle &7- Right click to toggle vanish")),
                new StaffModeItem(7, ItemBuilder.of(Material.NETHER_STAR,   "&6Staff Settings")),
                new StaffModeItem(8, ItemBuilder.of(Material.BARRIER,       "&cExit Staff Mode"))
        ));
    }
}

