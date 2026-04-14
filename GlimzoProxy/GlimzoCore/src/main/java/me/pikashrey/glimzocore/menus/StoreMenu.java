package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoreMenu extends GlimzoMenu {

    // Custom skull textures (hardcoded - visual only)
    private static final String SKULL_CROWN =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2ZlMjQ0OWQyMjdmYTk5MWYyYTcxMDI3MDlkOTk4MmRlMWY3OGY0MTE4MjYyOGJkYWUzOTQ2MjI1NGM2ODFhMyJ9fX0=";
    private static final String SKULL_GEM =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdkYjhkNTY0ZTgyZDc2NDgzNzA5NDk3NWMxMGUyN2NiZWUzMTZhZWRjOWY1MWI4ZDkxOTEzMWM3ZmIifX19";
    private static final String SKULL_STAR =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU4NjVlMjI3YjNkOTIxMzUxYzQyODIzODEyZTU4MjE1NWRhNzY5NjdmMjM5YzAzN2U5NTczYzc2YTZhNiJ9fX0=";
    private static final String SKULL_COIN =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxOGE0NWQ2ZWQyMzc2ZGQ5NzI5MzZjOWMwNzU0NTE4MTk4MDJlNCJ9fX0=";

    public StoreMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, cfg(plugin).getString("store.title", "&6&lGlimzo &e&lStore"), 54);
    }

    private static FileConfiguration cfg(GlimzoCore plugin) {
        return plugin.getConfigManager().getStore();
    }

    private FileConfiguration cfg() { return cfg(plugin); }

    @Override
    protected void buildContent() {
        int[] border = {0,1,2,3,4,5,6,7,8,
                9,13,17,18,22,26,27,31,35,36,40,44,
                45,46,47,48,50,51,52,53};
        for (int s : border) {
            final int fs = s;
            set(new Slot(fs) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.STAINED_GLASS_PANE)
                            .durability((short) 15).name("&r").build();
                }
            });
        }

        set(new Slot(4) {
            @Override public ItemStack getItem() {
                String link = cfg().getString("store.link", "store.glimzo.fun");
                return new ItemBuilder(Material.BOOK)
                        .name("&e&lGlimzo Network Store")
                        .lore("&7All purchases support the server!",
                                "&7Web store: &6" + link,
                                "&8Prices shown are approximate.").build();
            }
        });

        buildSectionHeader(10, SKULL_CROWN,  "&6&lDonor Ranks",     "&7Unlock exclusive ranks",   "&7and permanent perks!");
        buildSectionHeader(14, SKULL_GEM,    "&b&lGem Packages",    "&7Gems power cosmetics",     "&7and the ally system!");
        buildSectionHeader(28, SKULL_STAR,   "&d&lCosmetics Bundles","&7Pre-built packs",         "&7at a discounted price!");

        int[] rankSlots = {11, 12, 19, 20, 21, 25};
        ConfigurationSection ranksSection = cfg().getConfigurationSection("store.ranks");
        if (ranksSection != null) {
            int ri = 0;
            for (String key : ranksSection.getKeys(false)) {
                if (ri >= rankSlots.length) break;
                ConfigurationSection rs = ranksSection.getConfigurationSection(key);
                if (rs == null) continue;
                final String displayName = rs.getString("display-name", "&7" + key);
                final String rankId      = rs.getString("rank-id", key);
                final String price       = rs.getString("price", "N/A");
                final List<String> perks = rs.getStringList("perks");
                buildRank(rankSlots[ri++], displayName, rankId, price, perks);
            }
        }

        int[] gemSlots = {15, 16, 23, 24, 33, 34};
        ConfigurationSection gemSection = cfg().getConfigurationSection("store.gem-packages");
        if (gemSection != null) {
            int gi = 0;
            for (String key : gemSection.getKeys(false)) {
                if (gi >= gemSlots.length) break;
                ConfigurationSection gs = gemSection.getConfigurationSection(key);
                if (gs == null) continue;
                final String label   = gs.getString("label", "&7Pack");
                final int    amount  = gs.getInt("amount", 500);
                final String price   = gs.getString("price", "N/A");
                final boolean pop    = gs.getBoolean("popular", false);
                buildGemPackage(gemSlots[gi++], amount, label, price, pop);
            }
        }

        int[] bundleSlots = {29, 30, 37, 38};
        Material[] bundleIcons = {Material.BLAZE_POWDER, Material.MONSTER_EGG, Material.NETHER_STAR, Material.CHEST};
        ConfigurationSection bundleSection = cfg().getConfigurationSection("store.bundles");
        if (bundleSection != null) {
            int bi = 0;
            for (String key : bundleSection.getKeys(false)) {
                if (bi >= bundleSlots.length) break;
                ConfigurationSection bs = bundleSection.getConfigurationSection(key);
                if (bs == null) continue;
                final String name     = bs.getString("name", "&7Bundle");
                final String desc     = bs.getString("description", "");
                final String price    = bs.getString("price", "N/A");
                final List<String> contents = bs.getStringList("contents");
                final Material icon  = bundleIcons[bi];
                buildBundle(bundleSlots[bi++], name, desc, price, icon, contents);
            }
        }

        set(new Slot(41) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.GOLD_NUGGET)
                        .name("&e&lCoins")
                              "&7Coins are &aearned &7by playing -",
                              "&7leveling up, completing quests,",
                              "&7and participating in events.",
                              "",
                              "&cCoins cannot be purchased.",
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                p.sendMessage(CC.translate("&eCoins are earned in-game - level up, complete quests, and join events!"));
            }
        });

        set(new Slot(49) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.ARROW)
                        .name("&7Back").lore("&8Close the store").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                p.closeInventory();
            }
        });
    }


    private void buildSectionHeader(int slot, String texture, String title,
                                    String lore1, String lore2) {
        set(new Slot(slot) {
            @Override public ItemStack getItem() {
                ItemStack skull = SkullBuilder.fromBase64(texture);
                ItemMeta meta = skull.getItemMeta();
                meta.setDisplayName(CC.translate(title));
                meta.setLore(Arrays.asList(
                        CC.translate(lore1),
                        CC.translate(lore2)
                ));
                skull.setItemMeta(meta);
                return skull;
            }
        });
    }

    private void buildRank(int slot, String displayName, String rankId,
                           String price, List<String> perks) {
        set(new Slot(slot) {
            @Override public ItemStack getItem() {
                Rank targetRank = Rank.fromId(rankId);
                Rank playerRank = plugin.getRankManager().getActiveRank(getPlayer());

                boolean blocked = targetRank != null && playerRank != null
                        && targetRank.getWeight() <= playerRank.getWeight();

                // Extract colour from display name e.g. "&6BARON" → "&6"
                String color = (displayName.length() >= 2 && displayName.charAt(0) == '&')
                        ? displayName.substring(0, 2) : "&7";

                if (blocked) {
                    boolean same = targetRank != null && playerRank != null
                            && targetRank.getWeight() == playerRank.getWeight();
                    return new ItemBuilder(Material.INK_SACK).durability((short) 8)
                            .name("&7" + displayName.replaceAll("&[0-9a-fk-or]", "").trim())
                                    same ? "&cYou already have this rank!"
                                            : "&cYou have a higher rank!",
                                    "&8Price: &7" + price,
                                    "&8Not available for you.").build();
                }

                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Perks:"));
                for (String perk : perks) {
                    lore.add(CC.translate("  " + color + "• " + perk));
                }
                lore.add(CC.translate("&aPrice: &2" + price));
                lore.add(CC.translate("&7Click to purchase!"));

                return new ItemBuilder(Material.DIAMOND)
                        .name(CC.translate(color + "&l" + displayName.replaceAll("&[0-9a-fk-or]", "").trim()))
                        .lore(lore.toArray(new String[0])).build();
            }

            @Override public void onClick(Player p, InventoryClickEvent e) {
                Rank targetRank = Rank.fromId(rankId);
                Rank playerRank = plugin.getRankManager().getActiveRank(p);
                if (targetRank != null && playerRank != null
                        && targetRank.getWeight() <= playerRank.getWeight()) {
                    boolean same = targetRank.getWeight() == playerRank.getWeight();
                    p.sendMessage(CC.translate(same
                            ? "&cYou already have the " + displayName + " &crank!"
                            : "&cYou already have a rank higher than " + displayName + "&c!"));
                    return;
                }
                String link = cfg().getString("store.link", "store.glimzo.fun");
                p.sendMessage(CC.translate("&7Purchase " + displayName + " &7at &6" + link));
                p.closeInventory();
            }
        });
    }

    private void buildGemPackage(int slot, int amount, String label,
                                 String price, boolean popular) {
        set(new Slot(slot) {
            @Override public ItemStack getItem() {
                List<String> lore = new ArrayList<>();
                if (popular) lore.add(CC.translate("&6★ MOST POPULAR ★"));
                else         lore.add(CC.translate("&7Gem Package"));
                lore.add(CC.translate("&7Amount: &b" + formatNumber(amount) + " Gems"));
                lore.add(CC.translate("&aPrice: &2" + price));
                lore.add(CC.translate("&7Click to purchase!"));
                return new ItemBuilder(Material.EMERALD)
                        .name(CC.translate(label + " &b" + formatNumber(amount) + " &7Gems"))
                        .lore(lore.toArray(new String[0])).build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                String link = cfg().getString("store.link", "store.glimzo.fun");
                p.sendMessage(CC.translate("&7Purchase &b" + formatNumber(amount)
                        + " Gems &7at &6" + link));
                p.closeInventory();
            }
        });
    }

    private void buildBundle(int slot, String name, String desc,
                             String price, Material icon, List<String> contents) {
        set(new Slot(slot) {
            @Override public ItemStack getItem() {
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7" + desc));
                lore.add(CC.translate("&7Contains:"));
                for (String line : contents) {
                    lore.add(CC.translate("  &8• &f" + line));
                }
                lore.add(CC.translate("&aPrice: &2" + price));
                lore.add(CC.translate("&7Click to purchase!"));
                return new ItemBuilder(icon)
                        .name(CC.translate(name))
                        .lore(lore.toArray(new String[0])).build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                String link = cfg().getString("store.link", "store.glimzo.fun");
                p.sendMessage(CC.translate("&7Purchase " + name + " &7at &6" + link));
                p.closeInventory();
            }
        });
    }


    private String formatNumber(int n) {
        if (n >= 1000) return (n / 1000) + "," + String.format("%03d", n % 1000);
        return String.valueOf(n);
    }
}