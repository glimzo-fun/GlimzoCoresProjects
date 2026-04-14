package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import me.pikashrey.glimzocore.utilities.general.ServerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class LobbySelectorMenu extends GlimzoMenu {
    public LobbySelectorMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&bLobby Selector", 27);
    }
    @Override protected void buildContent() {
        fillEmpty();
        for (int i = 0; i < 9; i++) {
            final int num = i + 1;
            set(new Slot(10 + (i < 5 ? i : i + 4)) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.COMPASS).name("&bLobby " + num)
                            .lore("&7Click to transfer").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.closeInventory();
                    ServerUtils.sendToServer(p, "lobby" + num);
                }
            });
        }
    }
}
