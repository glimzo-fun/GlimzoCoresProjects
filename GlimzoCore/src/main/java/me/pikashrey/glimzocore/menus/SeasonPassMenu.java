package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.season.Season;
import me.pikashrey.glimzocore.api.season.SeasonPassTier;
import me.pikashrey.glimzocore.menu.menu.SwitchableMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.menu.slots.pages.NextPageSlot;
import me.pikashrey.glimzocore.menu.slots.pages.PreviousPageSlot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class SeasonPassMenu extends SwitchableMenu {
    private static final int PAGE_SIZE = 7;
    public SeasonPassMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&6Season Pass Tiers", 54);
    }
    @Override protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        Season season = plugin.getSeasonManager().getCurrentSeason();
        if (data == null || season == null) return;
        List<SeasonPassTier> tiers = season.getPassTiers();
        boolean hasPass = data.hasSeasonPass();
        int playerRank = data.getSeasonRank();
        int start = currentPage * PAGE_SIZE;
        int[] freeRow = {10,11,12,13,14,15,16};
        int[] premRow = {19,20,21,22,23,24,25};

        for (int i = 0; i < PAGE_SIZE && (start + i) < tiers.size(); i++) {
            final SeasonPassTier tier = tiers.get(start + i);
            final boolean reached = playerRank >= (start + i);
            final int col = i;
            set(new Slot(freeRow[col]) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(reached ? Material.EMERALD : Material.COAL)
                            .name("&aTier " + tier.getTier() + " Free")
                            .lore(tier.getFreeRewards().toString(), reached ? "&aClaimed" : "&8Locked").build();
                }
            });
            set(new Slot(premRow[col]) {
                @Override public ItemStack getItem() {
                    Material mat = hasPass && reached ? Material.DIAMOND : Material.COAL;
                    return new ItemBuilder(mat)
                            .name((hasPass ? "&6" : "&7") + "Tier " + tier.getTier() + " Premium")
                            .lore(tier.getPremiumRewards().toString(),
                                    hasPass && reached ? "&aClaimed" : hasPass ? "&8Locked" : "&cRequires pass").build();
                }
            });
        }
        if (currentPage > 0) set(new PreviousPageSlot(45, this::prevPage));
        if (start + PAGE_SIZE < tiers.size()) set(new NextPageSlot(53, this::nextPage));
    }
}
