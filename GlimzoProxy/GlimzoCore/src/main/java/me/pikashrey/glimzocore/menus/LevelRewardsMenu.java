package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.leveling.LevelManager;
import me.pikashrey.glimzocore.features.leveling.LevelReward;
import me.pikashrey.glimzocore.menu.menu.SwitchableMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.menu.slots.pages.NextPageSlot;
import me.pikashrey.glimzocore.menu.slots.pages.PreviousPageSlot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LevelRewardsMenu extends SwitchableMenu {
    private static final int PAGE_SIZE = 28;
    public LevelRewardsMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&bLevel Rewards", 54);
    }
    @Override protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        int playerLevel = data != null ? data.getLevel() : 0;
        int start = currentPage * PAGE_SIZE + 1; // level 1-based

        int[] slots = new int[PAGE_SIZE]; int si = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[si++] = row * 9 + col;

        for (int i = 0; i < PAGE_SIZE; i++) {
            int level = start + i;
            if (level > LevelManager.MAX_LEVEL) break;
            final LevelReward reward = plugin.getLevelManager().getReward(level);
            final boolean reached = playerLevel >= level;
            final int lvl = level;
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    Material mat = reached ? Material.EMERALD : Material.COAL;
                    if (reward != null && reward.getType() == LevelReward.Type.GEMS) mat = reached ? Material.DIAMOND : Material.COAL;
                    else if (reward != null && reward.getType() == LevelReward.Type.COINS) mat = reached ? Material.GOLD_NUGGET : Material.COAL;
                    String desc = (reward == null || reward.getType() == LevelReward.Type.NONE)
                            ? "&8No reward" : "&7+" + reward.getAmount() + " " + reward.getType().name().toLowerCase();
                    return new ItemBuilder(mat)
                            .name((reached ? "&a" : "&7") + "Level " + lvl)
                            .lore(desc, reached ? "&a✔ Reached" : "&8Not yet reached").build();
                }
            });
        }
        if (currentPage > 0) set(new PreviousPageSlot(45, this::prevPage));
        if (start + PAGE_SIZE - 1 < LevelManager.MAX_LEVEL) set(new NextPageSlot(53, this::nextPage));
    }
}
