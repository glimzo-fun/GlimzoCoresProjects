package me.pikashrey.glimzocore.menu;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Routes inventory clicks to the correct GlimzoMenu.
 *
 * The original code used:
 *     event.getInventory().getHolder()
 *
 * event.getInventory() returns whichever inventory the player physically clicked
 * in - either the TOP inventory (the GUI) or the BOTTOM inventory (player inv).
 *
 * The Accept/Deny friend buttons are in slots 20 and 24.
 * The Party accept/decline buttons are in slots 11 and 15.
 * All of these are in the TOP inventory - but Bukkit's raw slot indices for a
 * 54-slot GUI put the bottom half of the GUI (rows 4-6) at raw slots 27-53.
 * For a 36-slot menu, slots 27-35 are in the top inventory but adjacent to the
 * player inventory boundary. If the menu size is wrong or any slot lookup
 * miscalculates, event.getInventory() can return the player inventory.
 *
 * The definitive fix: ALWAYS check event.getView().getTopInventory().getHolder().
 * This is always the custom GUI holder regardless of where the player clicked.
 */
public class MenuManager implements Listener {

    private final GlimzoCore plugin;

    public MenuManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Always use TOP inventory to get the GlimzoMenu holder
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder holder  = topInventory.getHolder();

        if (!(holder instanceof GlimzoMenu)) return;
        GlimzoMenu menu = (GlimzoMenu) holder;

        // Cancel ALL clicks while a GlimzoMenu is open (prevents item theft from bottom inv)
        event.setCancelled(true);

        // Only route to slot handlers for clicks in the TOP inventory (the GUI)
        // Bottom-inventory clicks are cancelled above but not routed
        if (event.getClickedInventory() == topInventory) {
            menu.handleClick(event);
        }
    }
}