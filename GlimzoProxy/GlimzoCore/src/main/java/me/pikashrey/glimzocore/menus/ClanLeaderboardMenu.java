package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.clan.ClanLeaderboard;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class ClanLeaderboardMenu extends GlimzoMenu {
    public ClanLeaderboardMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&6Top Clans", 36);
    }
    @Override
    protected void buildContent() {
        fillEmpty();
        List<ClanLeaderboard.Entry> entries = plugin.getClanLeaderboard().getEntries();
        int[] slots = {10,11,12,13,14,15,16,19,20,21};
        for (int i = 0; i < Math.min(entries.size(), slots.length); i++) {
            final ClanLeaderboard.Entry e = entries.get(i);
            set(new Slot(slots[i]) { @Override public ItemStack getItem() {
                return new ItemBuilder(Material.NETHER_STAR)
                        .name("&6#" + e.rank + " &f[" + e.tag + "] " + e.name)
                        .lore("&7Level: &6" + e.level, "&7XP: &f" + e.xp,
                              "&7Leader: &f" + e.leaderName)
                        .build();
            }});
        }
    }
}
