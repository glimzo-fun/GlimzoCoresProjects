package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.joineffect.JoinMessage;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class JoinMessagesMenu extends GlimzoMenu {
    public JoinMessagesMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&aJoin Messages", 54);
    }
    @Override protected void buildContent() {
        fillEmpty();
        List<String> messages = JoinMessage.ALL;
        int[] slots = new int[messages.size()];
        int s = 0;
        for (int row = 1; row <= 5; row++)
            for (int col = 1; col <= 7 && s < messages.size(); col++)
                slots[s++] = row * 9 + col;

        for (int i = 0; i < messages.size(); i++) {
            final int idx = i;
            final String msg = messages.get(i);
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.PAPER)
                            .name("&f" + msg)
                            .lore("&7Click to pin this join message.").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState state =
                            plugin.getCosmeticManager().getState(p);
                    if (state != null) state.setActiveJoinMessageId(String.valueOf(idx));
                    p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate("&aJoin message set: &f" + msg));
                    p.closeInventory();
                }
            });
        }
    }
}
