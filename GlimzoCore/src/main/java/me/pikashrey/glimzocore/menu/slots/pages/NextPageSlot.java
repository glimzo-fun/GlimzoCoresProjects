package me.pikashrey.glimzocore.menu.slots.pages;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
/** Standard "Next Page" slot - extend and override onClick to advance the page. */
public class NextPageSlot extends PageSlot {
    private final Runnable onNext;
    public NextPageSlot(int slot, Runnable onNext) { super(slot); this.onNext = onNext; }
    @Override public ItemStack getItem() {
        return new ItemBuilder(Material.ARROW).name("&aNext Page &7»").build();
    }
    @Override public void onClick(Player p, InventoryClickEvent e) { if (onNext != null) onNext.run(); }
}
