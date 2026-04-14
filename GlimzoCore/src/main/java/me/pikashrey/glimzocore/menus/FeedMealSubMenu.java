package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
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
 * Submenu shown when clicking "Feed PetMeal" in the ally browser.
 * Displays ONLY meals the player currently owns (qty > 0).
 * Clicking a meal feeds 1 of it to the equipped ally.
 */
public class FeedMealSubMenu extends GlimzoMenu {

    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };
    private static final int PAGE_SIZE = ITEM_SLOTS.length;

    private final AllyType ally;
    private final int page;
    private final List<AllyMealType> ownedMeals;

    public FeedMealSubMenu(GlimzoCore plugin, Player player, AllyType ally, int page) {
        super(plugin, player, "&d&lFeed Meal » &f" + ally.getDisplayName(), 54);
        this.ally = ally;
        this.page = page;
        this.ownedMeals = buildOwnedList(player);
    }

    private List<AllyMealType> buildOwnedList(Player player) {
        AllyMealManager mm = plugin.getCosmeticManager().getAllyMealManager();
        List<AllyMealType> list = new ArrayList<>();
        for (AllyMealType meal : AllyMealType.values()) {
            if (mm.getQuantity(player.getUniqueId(), meal) > 0) {
                list.add(meal);
            }
        }
        return list;
    }

    @Override
    protected void buildContent() {
        fillEmpty();

        AllyMealManager mm = plugin.getCosmeticManager().getAllyMealManager();
        AllyPerkManager pm = plugin.getCosmeticManager().getAllyPerkManager();

        if (ownedMeals.isEmpty()) {
            // No meals owned - show info item in centre
            set(new Slot(22) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.BARRIER)
                            .name("&cNo Meals Owned")
                            .lore("&7You don't own any PetMeals yet.",
                                  "&7Buy some from the &6Ally Meal Shop&7!")
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {}
            });
        } else {
            int start = page * PAGE_SIZE;
            int end   = Math.min(start + PAGE_SIZE, ownedMeals.size());

            for (int i = start; i < end; i++) {
                final AllyMealType meal = ownedMeals.get(i);
                final int slot = ITEM_SLOTS[i - start];

                set(new Slot(slot) {
                    @Override public ItemStack getItem() {
                        int owned = mm.getQuantity(getPlayer().getUniqueId(), meal);
                        int xp    = mm.getMealXp(meal);

                        ItemStack skull = SkullBuilder.fromBase64(meal.getTexture());
                        ItemMeta meta = skull.getItemMeta();
                        meta.setDisplayName(CC.translate("&e" + meal.getDisplayName()));

                        List<String> lore = new ArrayList<>();
                        lore.add(CC.translate("&8&m                          "));
                        lore.add(CC.translate("&7Owned: &a" + owned + " &7meals"));
                        lore.add(CC.translate("&7Ally XP: &b+" + xp + " XP &7per meal"));
                        lore.add("");
                        lore.add(CC.translate("&eClick &7→ Feed 1 to &d" + ally.getDisplayName()));
                        meta.setLore(lore);
                        skull.setItemMeta(meta);
                        return skull;
                    }

                    @Override public void onClick(Player p, InventoryClickEvent e) {
                        int owned = mm.getQuantity(p.getUniqueId(), meal);
                        if (owned <= 0) {
                            p.sendMessage(CC.translate("&cYou no longer have any &f" + meal.getDisplayName() + "&c!"));
                            // Reopen to refresh; remove empty slots
                            new FeedMealSubMenu(plugin, p, ally, page).open();
                            return;
                        }
                        mm.feedMeal(p, meal);
                        // Refresh view
                        new FeedMealSubMenu(plugin, p, ally, page).open();
                    }
                });
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(ownedMeals.size() / (double) PAGE_SIZE));

        if (page > 0) {
            set(new Slot(45) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW).name("&7« Previous Page").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new FeedMealSubMenu(plugin, p, ally, page - 1).open();
                }
            });
        }

        if (page < totalPages - 1) {
            set(new Slot(53) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW).name("&7Next Page »").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new FeedMealSubMenu(plugin, p, ally, page + 1).open();
                }
            });
        }

        // Back button
        set(new Slot(49) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.ARROW)
                        .name("&7« Back")
                        .lore("&8Page " + (page + 1) + "/" + totalPages)
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticBrowserMenu(plugin, p,
                        CosmeticBrowserMenu.Category.ALLIES, ally).open();
            }
        });
    }
}
