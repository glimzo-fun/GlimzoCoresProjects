package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.achievements.Achievement;
import me.pikashrey.glimzocore.menu.menu.SwitchableMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.menu.slots.pages.NextPageSlot;
import me.pikashrey.glimzocore.menu.slots.pages.PreviousPageSlot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class AchievementsMenu extends SwitchableMenu {
    private static final int PAGE_SIZE = 28;
    public AchievementsMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&6Achievements", 54);
    }
    @Override
    protected void buildContent() {
        fillEmpty();
        List<Achievement> all = new ArrayList<>(plugin.getAchievementManager().getAllAchievements());
        Set<String> unlocked = plugin.getAchievementManager().getUnlocked(getPlayer().getUniqueId());
        int start = currentPage * PAGE_SIZE;

        int[] slots = new int[PAGE_SIZE]; int si = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[si++] = row * 9 + col;

        for (int i = 0; i < PAGE_SIZE && (start + i) < all.size(); i++) {
            final Achievement ach = all.get(start + i);
            final boolean done = unlocked.contains(ach.getId());
            final int slot = slots[i];
            set(new Slot(slot) { @Override public ItemStack getItem() {
                return new ItemBuilder(done ? Material.NETHER_STAR : Material.COAL)
                        .name((done ? "&a" : "&7") + ach.getDisplayName())
                        .lore(ach.getCategory().getColor() + "[" + ach.getCategory().getDisplayName() + "]",
                              "&7" + ach.getDescription(),
                              " ",
                              done ? "&aCompleted!" : "&8Locked",
                              "&7Reward: &e" + ach.getCoinReward() + " coins, &b" + ach.getXpReward() + " XP")
                        .build();
            }});
        }
        if (currentPage > 0) set(new PreviousPageSlot(45, this::prevPage));
        if (start + PAGE_SIZE < all.size()) set(new NextPageSlot(53, this::nextPage));
    }
}
