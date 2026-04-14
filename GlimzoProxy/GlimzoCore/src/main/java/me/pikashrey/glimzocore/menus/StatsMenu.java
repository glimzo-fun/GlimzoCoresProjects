package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.player.PlayerStats;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StatsMenu extends GlimzoMenu {
    public StatsMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&bPlayer Stats", 27);
    }
    @Override
    protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        if (data == null) return;
        PlayerStats s = data.getStats();

        set(new Slot(10) { @Override public ItemStack getItem() {
            return new ItemBuilder(Material.GOLD_NUGGET).name("&eEconomy")
                    .lore("&7Coins Earned: &e" + s.getTotalCoinsEarned(),
                          "&7Coins Spent: &e" + s.getTotalCoinsSpent(),
                          "&7Gems Earned: &d" + s.getTotalGemsEarned(),
                          "&7Gems Spent: &d" + s.getTotalGemsSpent()).build();
        }});
        set(new Slot(12) { @Override public ItemStack getItem() {
            return new ItemBuilder(Material.EXP_BOTTLE).name("&bLeveling")
                    .lore("&7Peak Level: &b" + s.getHighestLevel(),
                          "&7Total Prestiges: &6" + s.getTotalPrestiges(),
                          "&7Total XP Earned: &b" + s.getTotalXPEarned()).build();
        }});
        set(new Slot(14) { @Override public ItemStack getItem() {
            return new ItemBuilder(Material.COMPASS).name("&aGeneral")
                    .lore("&7Logins: &f" + s.getTotalLogins(),
                          "&7Playtime: &f" + TimeFormatUtils.format(s.getTotalPlaytimeMinutes() * 60_000L),
                          "&7Friends Added: &a" + s.getTotalFriendsAdded(),
                          "&7Clans Joined: &e" + s.getTotalClansJoined()).build();
        }});
        set(new Slot(16) { @Override public ItemStack getItem() {
            return new ItemBuilder(Material.NETHER_STAR).name("&6Achievements")
                    .lore("&7Unlocked: &f" + s.getTotalAchievementsUnlocked(),
                          "&7Cosmetics: &f" + s.getTotalCosmeticsUnlocked()).build();
        }});
    }
}
