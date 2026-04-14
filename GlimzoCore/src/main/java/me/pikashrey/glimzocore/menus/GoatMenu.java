package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.season.SeasonLeaderboard;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GoatMenu extends GlimzoMenu {

    public GoatMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&d&lG.O.A.T. Leaderboard", 45);
    }

    @Override
    protected void buildContent() {
        fillEmpty();
        
        List<SeasonLeaderboard.Entry> entries = plugin.getSeasonLeaderboard().getEntries();
        
        // Display top 10 in a grid format
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        
        // Add info item at top
        set(new Slot(4) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.PAPER)
                        .name("&d&lSeason Leaderboard")
                        .lore("&7Top players by season XP", "&7and season rank progression.").build();
            }
        });
        
        for (int i = 0; i < Math.min(entries.size(), slots.length); i++) {
            final SeasonLeaderboard.Entry e = entries.get(i);
            int position = i + 1;
            Material mat = position == 1 ? Material.GOLD_BLOCK : (position == 2 ? Material.IRON_BLOCK : (position == 3 ? Material.WOOD : Material.QUARTZ_BLOCK));
            
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(mat)
                            .name("&d#" + position + " &f" + e.name)
                            .lore("&7Season Rank: &d" + e.seasonRank,
                                  "&7Season XP: &f" + String.format("%,d", e.seasonXp))
                            .build();
                }
            });
        }
    }
}
