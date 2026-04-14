package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class NickMenu extends GlimzoMenu {

    public NickMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&bNickname", 27);
    }

    @Override
    protected void buildContent() {
        fillEmpty();
        boolean hasNick    = plugin.getNickManager().hasNick(getPlayer().getUniqueId());
        boolean isLegendary = plugin.getNickManager().isRandomNickTier(getPlayer());
        boolean hasAccess  = plugin.getNickManager().hasNickAccess(getPlayer());

        if (!hasAccess) {
            set(new Slot(13) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.BARRIER)
                            .name("&cNo Access")
                            .lore("&7You need &1[Warden]&7 or above", "&7to use nicknames.").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {}
            });
            return;
        }

        // Info item
        set(new Slot(11) {
            @Override public ItemStack getItem() {
                if (isLegendary) {
                    return new ItemBuilder(Material.NAME_TAG)
                            .name("&eAssign Random Nick")
                            .lore("&7As a &e[Legendary]&7 player,",
                                  "&7your nickname is randomly assigned.",
                                  " ",
                                  "&7Run &e/nick &7to get one.",
                                  hasNick ? ("&7Current: &f" + plugin.getNickManager().getNick(getPlayer().getUniqueId())) : "")
                            .build();
                } else {
                    return new ItemBuilder(Material.NAME_TAG)
                            .name("&bCustom Nickname")
                            .lore("&7Use &e/nick &b{YOUR_DESIRED_NICK}",
                                  "&7to set a custom nickname.",
                                  " ",
                                  hasNick ? ("&7Current: &f" + plugin.getNickManager().getNick(getPlayer().getUniqueId())) : "&7No nickname set.")
                            .build();
                }
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                p.closeInventory();
                if (isLegendary) {
                    p.performCommand("nick");
                } else {
                    p.sendMessage(CC.translate("&7Custom nick usage: &e/nick &b{YOUR_DESIRED_NICK}"));
                }
            }
        });

        // Clear button - only shown if they have a nick
        if (hasNick) {
            String current = plugin.getNickManager().getNick(getPlayer().getUniqueId());
            set(new Slot(15) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.BARRIER)
                            .name("&cRemove Nickname")
                            .lore("&7Current: &f" + current, " ", "&cClick to remove.").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.performCommand("unnick");
                    new NickMenu(plugin, p).open();
                }
            });
        }
    }
}
