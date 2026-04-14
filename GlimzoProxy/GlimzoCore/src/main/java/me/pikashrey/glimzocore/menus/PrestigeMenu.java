package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.leveling.LevelManager;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PrestigeMenu extends GlimzoMenu {
    public PrestigeMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&6Prestige", 27);
    }
    @Override protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        if (data == null) return;
        boolean canPrestige = data.getLevel() >= LevelManager.MAX_LEVEL;

        set(new Slot(13) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(canPrestige ? Material.NETHER_STAR : Material.COAL)
                        .name(canPrestige ? "&6Prestige Now!" : "&cNot Ready")
                        .lore(
                            "&7Current prestige: &6" + data.getPrestige(),
                            "&7Level: &b" + data.getLevel() + " / " + LevelManager.MAX_LEVEL,
                            " ",
                            canPrestige
                                ? "&7Reward: &d" + (100 + data.getPrestige() * 50) + " gems\n&aClick to prestige!"
                                : "&cYou need level " + LevelManager.MAX_LEVEL + " to prestige."
                        ).build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                if (!canPrestige) return;
                p.closeInventory();
                if (plugin.getPrestigeManager().prestige(p.getUniqueId())) {
                    p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate("&aPrestiged! Your level has been reset."));
                }
            }
        });
    }
}
