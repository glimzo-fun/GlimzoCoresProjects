package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.general.DateUtils;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;

public class ProfileMenu extends GlimzoMenu {
    private final PlayerData target;
    public ProfileMenu(GlimzoCore plugin, Player viewer, PlayerData target) {
        super(plugin, viewer, "&6Profile &7» &f" + target.getName(), 27);
        this.target = target;
    }
    @Override
    protected void buildContent() {
        fillEmpty();
        RankRef rank = plugin.getRankManager().getActiveRankRef(target.getUuid());

        // Head slot 13 - rank
        set(new Slot(13) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.SKULL_ITEM, 1).durability((short) 3)
                        .name(rank.getColorCode() + target.getName())
                        .lore(
                            "&7Rank: " + rank.getColorCode() + rank.getDisplayName(),
                            "&7Level: &b" + target.getLevel(),
                            "&7Prestige: &6" + target.getPrestige(),
                            " ",
                            "&7Coins: &e" + target.getCoins(),
                            "&7Gems: &d" + target.getGems(),
                            " ",
                            "&7First joined: &f" + DateUtils.formatDate(target.getFirstJoinTime())
                        ).build();
            }
        });

        // Stats slot 11
        set(new Slot(11) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.BOOK)
                        .name("&bStats")
                        .lore(
                            "&7Friends: &a" + plugin.getFriendManager().getFriendCount(target.getUuid()),
                            "&7Logins: &f" + target.getStats().getTotalLogins()
                        ).build();
            }
        });

        // Achievements slot 15
        set(new Slot(15) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.NETHER_STAR)
                        .name("&eAchievements")
                        .lore("&7Unlocked: &f" + plugin.getAchievementManager().getUnlockCount(target.getUuid())
                              + " / " + plugin.getAchievementManager().getAllAchievements().size())
                        .build();
            }
        });
    }
}
