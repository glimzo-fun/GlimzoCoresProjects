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

public class GameSelectorMenu extends GlimzoMenu {
    public GameSelectorMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&9Game Selector", 27);
    }
    @Override protected void buildContent() {
        fillEmpty();
        String[] servers = {"lobby", "bedwars", "skywars", "practice"};
        String[] colors  = {"&7", "&e", "&b", "&a"};
        Material[] mats  = {Material.EMERALD, Material.BED, Material.FEATHER, Material.IRON_SWORD};
        int[] slots = {10, 12, 14, 16};
        for (int i = 0; i < servers.length; i++) {
            final String srv = servers[i]; final String clr = colors[i]; final Material mat = mats[i];
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(mat).name(clr + srv.substring(0,1).toUpperCase() + srv.substring(1))
                            .lore("&7Click to connect").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.closeInventory();
                    ServerUtils.sendToServer(p, srv);
                }
            });
        }
    }
}
