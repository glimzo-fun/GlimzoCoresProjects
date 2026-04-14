package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.clan.ClanLevel;
import me.pikashrey.glimzocore.api.clan.ClanPerk;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClanPerksMenu extends GlimzoMenu {
    public ClanPerksMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&6Clan Perks", 36);
    }
    @Override protected void buildContent() {
        fillEmpty();
        ClanData clan = plugin.getClanManager().getClanByPlayer(getPlayer().getUniqueId());
        if (clan == null) return;
        ClanLevel level = clan.getClanLevel();

        ClanPerk[] perks = ClanPerk.values();
        int[] slots = {10,11,12,13,14,15,16};
        for (int i = 0; i < Math.min(perks.length, slots.length); i++) {
            final ClanPerk perk = perks[i];
            final boolean unlocked = level.hasPerk(perk);
            final double val = level.getPerkValue(perk);
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    Material mat = perk.isFunctional()
                            ? (unlocked ? Material.EMERALD : Material.COAL)
                            : (unlocked ? Material.NETHER_STAR : Material.COAL);
                    String valStr = perk == ClanPerk.INCREASED_CAPACITY
                            ? "+" + (int) val + " slots"
                            : "+" + (int)(val*100) + "%";
                    return new ItemBuilder(mat)
                            .name((unlocked ? "&a" : "&7") + perk.getDisplayName())
                            .lore(
                                perk.getCategory() == ClanPerk.Category.FUNCTIONAL ? "&bFunctional" : "&dCosmetic",
                                "&7" + perk.getDescription(),
                                unlocked ? "&a" + valStr : "&8Locked",
                                "&7Unlocks at: &6Level " + findUnlockLevel(perk)
                            ).build();
                }
            });
        }
    }
    private int findUnlockLevel(ClanPerk perk) {
        for (int l = 1; l <= ClanLevel.maxLevel(); l++) {
            if (ClanLevel.forLevel(l).hasPerk(perk)) return l;
        }
        return ClanLevel.maxLevel();
    }
}
