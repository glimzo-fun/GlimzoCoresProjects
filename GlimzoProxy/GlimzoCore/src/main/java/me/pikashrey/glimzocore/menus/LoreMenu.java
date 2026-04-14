package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.lore.LoreChapter;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class LoreMenu extends GlimzoMenu {
    public LoreMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&5Server Lore", 27);
    }
    @Override protected void buildContent() {
        fillEmpty();
        List<LoreChapter> chapters = plugin.getLoreManager().getChapters();
        if (chapters.isEmpty()) {
            set(new Slot(13) { @Override public ItemStack getItem() {
                return ItemBuilder.of(Material.BOOK, "&7No lore chapters configured yet.");
            }});
            return;
        }
        int[] slots = {10,12,14,16};
        for (int i = 0; i < Math.min(chapters.size(), slots.length); i++) {
            final LoreChapter ch = chapters.get(i);
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.WRITTEN_BOOK)
                            .name("&5Chapter " + (ch.getIndex() + 1) + ": &f" + ch.getTitle())
                            .lore("&7" + ch.getEntries().size() + " entries", "&7Click to read").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.closeInventory();
                    for (me.pikashrey.glimzocore.features.lore.LoreEntry entry : ch.getEntries()) {
                        p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate("&5--- " + entry.getTitle() + " ---"));
                        entry.getContent().forEach(line -> p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate("&7" + line)));
                    }
                }
            });
        }
    }
}
