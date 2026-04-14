package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.meal.AllyMealManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.meal.AllyMealType;
import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop GUI for purchasing and using Ally Meals.
 * Shows all 32 meals with gem cost, coin cost, owned count, XP value.
 * Left-click = buy with gems | Right-click = buy with coins | Shift-click = feed to ally
 */
public class AllyMealShopMenu extends GlimzoMenu {

    // Slots 10-16, 19-25, 28-34, 37-43 - 4 rows of 7 = 28 slots for 32 meals (needs pagination)
    // Use pages: page 0 = meals 0-27, page 1 = meals 28-31
    private final int page;
    private static final int PAGE_SIZE = 28;
    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };

    public AllyMealShopMenu(GlimzoCore plugin, Player player, int page) {
        super(plugin, player, "&6&lAlly Meal Shop", 54);
        this.page = page;
    }

    @Override
    protected void buildContent() {
        fillEmpty();

        AllyMealManager mm = plugin.getCosmeticManager().getAllyMealManager();
        AllyMealType[] meals = AllyMealType.values();
        int start = page * PAGE_SIZE;
        int end   = Math.min(start + PAGE_SIZE, meals.length);

        for (int i = start; i < end; i++) {
            final AllyMealType meal = meals[i];
            final int slotIndex = i - start;
            final int slot      = ITEM_SLOTS[slotIndex];

            set(new Slot(slot) {
                @Override
                public ItemStack getItem() {
                    int owned    = mm.getQuantity(getPlayer().getUniqueId(), meal);
                    int gemCost  = mm.getGemCost(meal);
                    int coinCost = mm.getCoinCost(meal);
                    int xp       = mm.getMealXp(meal);

                    long playerGems  = plugin.getGemManager().getGems(getPlayer().getUniqueId());
                    long playerCoins = plugin.getCoinManager().getCoins(getPlayer().getUniqueId());

                    boolean canAffordGems  = playerGems  >= gemCost;
                    boolean canAffordCoins = playerCoins >= coinCost;

                    ItemStack skull = SkullBuilder.fromBase64(meal.getTexture());
                    ItemMeta meta = skull.getItemMeta();
                    meta.setDisplayName(CC.translate("&e" + meal.getDisplayName()));

                    List<String> lore = new ArrayList<>();
                    lore.add(CC.translate("&8&m                          "));
                    lore.add(CC.translate("&7Owned: &a" + owned + " &7meals"));
                    lore.add(CC.translate("&7Ally XP: &b+" + xp + " XP &7per meal"));
                    lore.add("");
                    lore.add(CC.translate("&6Cost (Gems): &b" + gemCost +
                            (canAffordGems ? " &a✔" : " &c✘")));
                    lore.add(CC.translate("&6Cost (Coins): &e" + coinCost +
                            (canAffordCoins ? " &a✔" : " &c✘")));
                    lore.add("");
                    lore.add(CC.translate("&eLeft-click &7→ Buy 1 with Gems"));
                    lore.add(CC.translate("&eRight-click &7→ Buy 1 with Coins"));
                    if (owned > 0) {
                        lore.add(CC.translate("&eShift+Click &7→ Feed 1 to your ally"));
                    }
                    meta.setLore(lore);
                    skull.setItemMeta(meta);
                    return skull;
                }

                @Override
                public void onClick(Player p, InventoryClickEvent e) {
                    AllyMealManager mm = plugin.getCosmeticManager().getAllyMealManager();

                    if (e.isShiftClick()) {
                        // Feed meal to ally
                        mm.feedMeal(p, meal);
                        build();
                        return;
                    }

                    if (e.isRightClick()) {
                        // Buy with coins
                        AllyMealManager.PurchaseResult result = mm.buyWithCoins(p, meal, 1);
                        switch (result) {
                            case SUCCESS:
                                p.sendMessage(CC.translate("&a✓ Purchased &f" + meal.getDisplayName() +
                                        " &afor &e" + mm.getCoinCost(meal) + " Coins&a!"));
                                break;
                            case NOT_ENOUGH_COINS:
                                p.sendMessage(CC.translate("&cNot enough Coins! Need &e" +
                                        mm.getCoinCost(meal) + "&c, have &e" +
                                        plugin.getCoinManager().getCoins(p.getUniqueId()) + "&c."));
                                break;
                            default: break;
                        }
                    } else {
                        // Buy with gems (left click)
                        AllyMealManager.PurchaseResult result = mm.buyWithGems(p, meal, 1);
                        switch (result) {
                            case SUCCESS:
                                p.sendMessage(CC.translate("&a✓ Purchased &f" + meal.getDisplayName() +
                                        " &afor &b" + mm.getGemCost(meal) + " Gems&a!"));
                                break;
                            case NOT_ENOUGH_GEMS:
                                p.sendMessage(CC.translate("&cNot enough Gems! Need &b" +
                                        mm.getGemCost(meal) + "&c, have &b" +
                                        plugin.getGemManager().getGems(p.getUniqueId()) + "&c."));
                                break;
                            default: break;
                        }
                    }
                    build();
                }
            });
        }

        int totalPages = (int) Math.ceil(meals.length / (double) PAGE_SIZE);

        if (page > 0) {
            set(new Slot(45) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW).name("&7« Previous Page").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new AllyMealShopMenu(plugin, p, page - 1).open();
                }
            });
        }

        if (page < totalPages - 1) {
            set(new Slot(53) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW).name("&7Next Page »").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new AllyMealShopMenu(plugin, p, page + 1).open();
                }
            });
        }

        // Page indicator
        set(new Slot(49) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.ARROW)
                        .name("&7Back to Cosmetics")
                        .lore("&8Page " + (page + 1) + "/" + totalPages).build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticsMenu(plugin, p).open();
            }
        });
    }
}
