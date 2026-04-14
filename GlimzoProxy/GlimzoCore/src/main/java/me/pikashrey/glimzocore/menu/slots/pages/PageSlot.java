package me.pikashrey.glimzocore.menu.slots.pages;
import me.pikashrey.glimzocore.menu.slots.Slot;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
/** Abstract base for pagination slots. */
public abstract class PageSlot extends Slot {
    public PageSlot(int slot) { super(slot); }
    public abstract ItemStack getItem();
    public abstract void onClick(Player player, InventoryClickEvent event);
}
