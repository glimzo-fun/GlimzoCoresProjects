package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.menu.menu.SwitchableMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.menu.slots.pages.NextPageSlot;
import me.pikashrey.glimzocore.menu.slots.pages.PreviousPageSlot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class QuestsMenu extends SwitchableMenu {
    private static final int PAGE_SIZE = 28;
    
    public QuestsMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&aQuests", 54);
    }
    
    @Override 
    protected void buildContent() {
        fillEmpty();
        
        // Placeholder quest system - shows available quests
        Set<String> completedQuests = new HashSet<>();
        // QuestManager not available, use empty set
        
        List<String> allQuests = new ArrayList<>(Arrays.asList(
                "mining_master", "farmer", "fisherman", "explorer", "warrior",
                "builder", "collector", "traveler", "lucky", "lucky_supreme"
        ));
        
        int start = currentPage * PAGE_SIZE;
        int[] slots = new int[PAGE_SIZE];
        int si = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[si++] = row * 9 + col;
        
        for (int i = 0; i < PAGE_SIZE && (start + i) < allQuests.size(); i++) {
            final String questId = allQuests.get(start + i);
            final boolean completed = completedQuests.contains(questId);
            final int slot = slots[i];
            set(new Slot(slot) {
                @Override public ItemStack getItem() {
                    String displayName = questId.substring(0, 1).toUpperCase() + questId.substring(1).replace("_", " ");
                    return new ItemBuilder(completed ? Material.NETHER_STAR : Material.COAL)
                            .name((completed ? "&a" : "&7") + displayName)
                            .lore("&7Quest ID: &f" + questId,
                                  " ",
                                  completed ? "&aCompleted!" : "&7Not yet completed",
                                  "&7Reward: &e100 coins, &b50 XP")
                            .build();
                }
            });
        }
        if (currentPage > 0) set(new PreviousPageSlot(45, this::prevPage));
        if (start + PAGE_SIZE < allQuests.size()) set(new NextPageSlot(53, this::nextPage));
    }
}
