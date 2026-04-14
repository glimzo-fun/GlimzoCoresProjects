package me.pikashrey.glimzocore.menus.punishments.alts;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AltsMenu extends GlimzoMenu {
    public AltsMenu(GlimzoCore plugin, Player player, String targetName) {
        super(plugin, player, "&cAlts &7» &f" + targetName, 27);
    }
    @Override protected void buildContent() {
        fillEmpty();
        set(new Slot(13) { @Override public ItemStack getItem() {
            return new ItemBuilder(Material.BARRIER)
                    .name("&7Alt detection requires IP logging.")
                    .lore("&7This feature will be available", "&7once IP tracking is implemented.").build();
        }});
    }
}
