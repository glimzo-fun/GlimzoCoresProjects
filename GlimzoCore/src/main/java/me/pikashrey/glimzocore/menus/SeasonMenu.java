package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.season.Season;
import me.pikashrey.glimzocore.api.season.SeasonRank;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SeasonMenu extends GlimzoMenu {
    public SeasonMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&dSeason", 36);
    }
    @Override protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        Season season = plugin.getSeasonManager().getCurrentSeason();
        if (data == null || season == null) return;
        SeasonRank rank = plugin.getSeasonManager().getPlayerRank(data);

        set(new Slot(13) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.DIAMOND)
                        .name("&d" + season.getDisplayName())
                        .lore(
                            "&7Your rank: " + (rank != null ? rank.getColoredName() : "&8Unranked"),
                            "&7Season XP: &f" + data.getSeasonXp(),
                            "&7Pass active: " + (data.hasSeasonPass() ? "&aYes" : "&cNo"),
                            " ",
                            "&7Season ends: &f" + me.pikashrey.glimzocore.utilities.general.TimeFormatUtils.formatExpiry(season.getEndTime())
                        ).build();
            }
        });
        set(new Slot(11) {
            @Override public ItemStack getItem() {
                return ItemBuilder.of(Material.GOLD_BLOCK, "&6Season Pass",
                        data.hasSeasonPass() ? "&aActive" : "&7Not active - purchase at store.glimzo.net");
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new SeasonPassMenu(plugin, p).open();
            }
        });
    }
}
