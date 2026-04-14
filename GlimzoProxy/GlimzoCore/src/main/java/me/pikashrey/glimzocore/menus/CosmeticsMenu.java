package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CosmeticsMenu extends GlimzoMenu {

    public CosmeticsMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&bCosmetics", 54);
    }

    @Override
    protected void buildContent() {
        fillEmpty();

        // Auras
        set(new Slot(10) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.BLAZE_POWDER)
                        .name("&cAuras")
                        .lore("&7Browse and equip auras.", "&7Click to open.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticBrowserMenu(plugin, p, CosmeticBrowserMenu.Category.AURAS).open();
            }
        });

        // Wings
        set(new Slot(11) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.FEATHER)
                        .name("&bWings")
                        .lore("&7Browse and equip wings.", "&7Click to open.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticBrowserMenu(plugin, p, CosmeticBrowserMenu.Category.WINGS).open();
            }
        });

        // Morphs
        set(new Slot(12) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.BONE)
                        .name("&7Morphs")
                        .lore("&7Browse and equip morphs.", "&7Click to open.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticBrowserMenu(plugin, p, CosmeticBrowserMenu.Category.MORPHS).open();
            }
        });

        // Chat Tags
        set(new Slot(13) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.PAPER)
                        .name("&aChat Tags")
                        .lore("&7Browse and equip chat tags.", "&7Click to open.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticBrowserMenu(plugin, p, CosmeticBrowserMenu.Category.CHAT_TAGS).open();
            }
        });

        // Join Effects
        set(new Slot(14) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.FIREWORK)
                        .name("&6Join Effects")
                        .lore("&7Browse and equip join effects.", "&7Click to open.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticBrowserMenu(plugin, p, CosmeticBrowserMenu.Category.JOIN_EFFECTS).open();
            }
        });

        // Allies
        set(new Slot(15) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.MONSTER_EGG)
                        .name("&dAllies")
                        .lore("&7Browse and equip allies.", "&7Click to open.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new CosmeticBrowserMenu(plugin, p, CosmeticBrowserMenu.Category.ALLIES).open();
            }
        });

        // Ally Meals
        set(new Slot(16) {
            @Override public ItemStack getItem() {
                me.pikashrey.glimzocore.features.cosmetics.ally.meal.AllyMealManager mm =
                        plugin.getCosmeticManager().getAllyMealManager();
                int total = mm != null ? mm.getTotalMeals(getPlayer().getUniqueId()) : 0;
                return new ItemBuilder(Material.BREAD)
                        .name("&6Ally Meals")
                        .lore("&7Purchase meals for your ally.",
                              "&7Owned: &a" + total + " &7meals total",
                              "&7Click to open.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                new me.pikashrey.glimzocore.menus.AllyMealShopMenu(plugin, p, 0).open();
            }
        });
    }
}
