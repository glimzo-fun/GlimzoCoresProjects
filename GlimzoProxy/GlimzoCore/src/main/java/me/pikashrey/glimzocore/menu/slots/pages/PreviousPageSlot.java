package me.pikashrey.glimzocore.menu.slots.pages;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
/** Standard "Previous Page" slot. */
public class PreviousPageSlot extends PageSlot {
    private final Runnable onPrev;
    public PreviousPageSlot(int slot, Runnable onPrev) { super(slot); this.onPrev = onPrev; }
    @Override public ItemStack getItem() {
        return new ItemBuilder(Material.ARROW).name("&7« &ePrevious Page").build();
    }
    @Override public void onClick(Player p, InventoryClickEvent e) { if (onPrev != null) onPrev.run(); }
}
