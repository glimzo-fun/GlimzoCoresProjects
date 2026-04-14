package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.leveling.LevelManager;
import me.pikashrey.glimzocore.features.leveling.LevelReward;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.general.StringUtils;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LevelMenu extends GlimzoMenu {
    public LevelMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&bLevel Progress", 27);
    }
    @Override
    protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        if (data == null) return;

        LevelManager lm = plugin.getLevelManager();
        int level = data.getLevel();
        long xp = data.getExperience();
        long needed = LevelManager.xpForLevel(level);
        double progress = lm.levelProgress(data);

        set(new Slot(13) { @Override public ItemStack getItem() {
            return new ItemBuilder(Material.EXP_BOTTLE)
                    .name("&bLevel &f" + level + " &7/ " + LevelManager.MAX_LEVEL)
                    .lore(
                        StringUtils.progressBar(progress, 20, '|', "&a", "&8"),
                        "&7XP: &b" + xp + " / " + needed,
                        level < LevelManager.MAX_LEVEL
                            ? "&7Next level in: &f" + lm.xpToNextLevel(data) + " XP"
                            : "&aMax level! Use /prestige",
                        " ",
                        "&7Prestige: &6" + data.getPrestige()
                    ).build();
        }});

        // Show next 3 level rewards
        int[] rewardSlots = {19, 22, 25};
        for (int i = 0; i < rewardSlots.length; i++) {
            int targetLevel = level + 1 + i;
            if (targetLevel > LevelManager.MAX_LEVEL) break;
            final LevelReward reward = plugin.getLevelManager().getReward(targetLevel);
            final int tl = targetLevel; final LevelReward r = reward;
            set(new Slot(rewardSlots[i]) { @Override public ItemStack getItem() {
                Material mat = (r == null || r.getType() == LevelReward.Type.NONE) ? Material.PAPER
                        : r.getType() == LevelReward.Type.COINS ? Material.GOLD_NUGGET
                        : r.getType() == LevelReward.Type.GEMS ? Material.EMERALD
                        : Material.NETHER_STAR;
                String desc = (r == null || r.getType() == LevelReward.Type.NONE) ? "&7No reward"
                        : "&7+" + r.getAmount() + " " + r.getType().name().toLowerCase();
                return ItemBuilder.of(mat, "&7Level &b" + tl + " &7reward", desc);
            }});
        }
    }
}
