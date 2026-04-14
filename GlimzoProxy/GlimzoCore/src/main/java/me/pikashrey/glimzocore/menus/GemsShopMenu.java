package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GemsShopMenu extends GlimzoMenu {
    private static final long[][] PACKAGES = {{100,500},{250,1200},{500,2500},{1000,5000}};
    public GemsShopMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&5Gems Shop", 27);
    }
    @Override protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        long coins = data != null ? data.getCoins() : 0;
        int[] slots = {10, 12, 14, 16};
        for (int i = 0; i < PACKAGES.length; i++) {
            final long price = PACKAGES[i][0]; final long gems = PACKAGES[i][1];
            final boolean canAfford = coins >= price;
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(canAfford ? Material.EMERALD : Material.COAL)
                            .name("&d" + gems + " Gems")
                            .lore("&7Cost: &e" + price + " coins", canAfford ? "&aClick to buy" : "&cNot enough coins").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    if (!canAfford) { p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate("&cNot enough coins.")); return; }
                    long removed = plugin.getCoinManager().removeCoins(p.getUniqueId(), price, "Gems shop purchase");
                    if (removed > 0) {
                        plugin.getGemManager().addGems(p.getUniqueId(), gems, "Gems shop purchase");
                        p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate("&aPurchased &d" + gems + " gems&a!"));
                        new GemsShopMenu(plugin, p).open();
                    }
                }
            });
        }
    }
}
