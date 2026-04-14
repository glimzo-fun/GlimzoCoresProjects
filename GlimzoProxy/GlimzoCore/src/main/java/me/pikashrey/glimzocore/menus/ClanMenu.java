package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.clan.ClanLevel;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClanMenu extends GlimzoMenu {
    public ClanMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&6Clan", 36);
    }
    @Override
    protected void buildContent() {
        fillEmpty();
        ClanData clan = plugin.getClanManager().getClanByPlayer(getPlayer().getUniqueId());
        if (clan == null) {
            set(new Slot(13) { @Override public ItemStack getItem() {
                return ItemBuilder.of(Material.BARRIER, "&cNot in a clan", "&7Use /clan create <name> <tag>");
            }});
            return;
        }
        ClanLevel cl = clan.getClanLevel();
        long nextXp = clan.getXpToNextLevel();

        set(new Slot(13) { @Override public ItemStack getItem() {
            return new ItemBuilder(Material.NETHER_STAR)
                    .name("&f[" + clan.getTag() + "] &6" + clan.getName())
                    .lore(
                        "&7Level: &6" + clan.getLevel() + " / " + ClanLevel.maxLevel(),
                        "&7XP: &f" + clan.getXp() + (nextXp > 0 ? " / " + (clan.getXp() + nextXp) : " &a(MAX)"),
                        "&7Leader: &f" + clan.getLeaderName(),
                        "&7Members: &f" + clan.getMemberCount(),
                        " ",
                        "&7XP Boost: &a+" + (int)(cl.getPerkValue(me.pikashrey.glimzocore.api.clan.ClanPerk.XP_BOOST)*100) + "%",
                        "&7Coin Boost: &e+" + (int)(cl.getPerkValue(me.pikashrey.glimzocore.api.clan.ClanPerk.COIN_BOOST)*100) + "%"
                    ).build();
        }});

        set(new Slot(11) { @Override public ItemStack getItem() {
            return ItemBuilder.of(Material.SKULL_ITEM, "&7Members (" + clan.getMemberCount() + ")");
        }});
        set(new Slot(15) { @Override public ItemStack getItem() {
            return ItemBuilder.of(Material.DIAMOND, "&bPerks");
        }});
    }
}
