package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.cosmetics.CosmeticUnlockManager;
import me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevelManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.features.cosmetics.aura.AuraManager;
import me.pikashrey.glimzocore.features.cosmetics.aura.BaseAura;
import me.pikashrey.glimzocore.features.cosmetics.chattag.ChatTag;
import me.pikashrey.glimzocore.features.cosmetics.chattag.ChatTagManager;
import me.pikashrey.glimzocore.features.cosmetics.joineffect.JoinEffectType;
import me.pikashrey.glimzocore.features.cosmetics.morph.BaseMorph;
import me.pikashrey.glimzocore.features.cosmetics.morph.MorphManager;
import me.pikashrey.glimzocore.features.cosmetics.wings.BaseWings;
import me.pikashrey.glimzocore.features.cosmetics.wings.WingsManager;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CosmeticBrowserMenu extends GlimzoMenu {

    public enum Category { AURAS, WINGS, MORPHS, CHAT_TAGS, JOIN_EFFECTS, ALLIES }

    private final Category category;
    /** When set, show the detail submenu for this specific ally instead of the list. */
    private final AllyType detailAlly;

    public CosmeticBrowserMenu(GlimzoCore plugin, Player player, Category category) {
        this(plugin, player, category, null);
    }

    public CosmeticBrowserMenu(GlimzoCore plugin, Player player, Category category, AllyType detailAlly) {
        super(plugin, player, getCategoryTitle(category, detailAlly), 54);
        this.category   = category;
        this.detailAlly = detailAlly;
    }

    private static String getCategoryTitle(Category cat, AllyType detail) {
        if (detail != null) return "&d&l" + detail.getDisplayName();
        switch (cat) {
            case AURAS:        return "&c&lAuras";
            case WINGS:        return "&b&lWings";
            case MORPHS:       return "&7&lMorphs";
            case CHAT_TAGS:    return "&a&lChat Tags";
            case JOIN_EFFECTS: return "&6&lJoin Effects";
            case ALLIES:       return "&d&lAllies";
            default:           return "&7Cosmetics";
        }
    }

    @Override
    protected void buildContent() {
        fillEmpty();
        PlayerCosmeticState state = plugin.getCosmeticManager().getState(getPlayer());

        if (category == Category.ALLIES && detailAlly != null) {
            buildAllyDetail(state, detailAlly);
        } else {
            switch (category) {
                case AURAS:        buildAuras(state);       break;
                case WINGS:        buildWings(state);       break;
                case MORPHS:       buildMorphs(state);      break;
                case CHAT_TAGS:    buildChatTags(state);    break;
                case JOIN_EFFECTS: buildJoinEffects(state); break;
                case ALLIES:       buildAllies(state);      break;
            }
        }

        // Back button
        set(new Slot(49) {
            @Override public ItemStack getItem() {
                String backLabel = detailAlly != null ? "&7Back to Allies" : "&7Back";
                return new ItemBuilder(Material.ARROW).name(backLabel).lore("&8Return").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                if (detailAlly != null) {
                    new CosmeticBrowserMenu(plugin, p, Category.ALLIES).open();
                } else {
                    new CosmeticsMenu(plugin, p).open();
                }
            }
        });
    }


    private void buildAuras(PlayerCosmeticState state) {
        AuraManager am = plugin.getCosmeticManager().getAuraManager();
        if (am == null) return;
        String active = state != null ? state.getActiveAuraId() : null;

        int slot = 10;
        for (BaseAura aura : new ArrayList<>(am.getRegistry().values())) {
            if (slot >= 45) break;
            final String id        = aura.getId();
            final boolean owned    = isOwned(id);
            final boolean equipped = id.equals(active);
            final int cost         = getCosmeticCost("auras", id, 200);
            set(new Slot(slot++) {
                @Override public ItemStack getItem() {
                    if (!owned) {
                        boolean canAfford = plugin.getGemManager().hasGems(getPlayer().getUniqueId(), cost);
                        return new ItemBuilder(Material.BLAZE_POWDER)
                                .name("&c" + aura.getDisplayName())
                                .lore("&7Status: &cLocked",
                                      "&7Cost: &b" + cost + " Gems",
                                      canAfford ? "&eClick to purchase!" : "&cNot enough gems.").build();
                    }
                    Material icon = equipped ? Material.EMERALD : Material.BLAZE_POWDER;
                    return new ItemBuilder(icon)
                            .name((equipped ? "&a" : "&e") + aura.getDisplayName())
                            .lore(equipped ? "&a&lEQUIPPED &7- Click to unequip" : "&7Click to equip").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    if (!owned) {
                        long gems = plugin.getGemManager().getGems(p.getUniqueId());
                        if (gems < cost) { p.sendMessage(CC.translate("&cNeed &b" + cost + " Gems&c. You have &b" + gems + "&c.")); return; }
                        plugin.getGemManager().removeGems(p.getUniqueId(), cost, "aura_unlock:" + id);
                        plugin.getCosmeticManager().getUnlockManager().grantUnlock(p.getUniqueId(), id, "gem_purchase");
                        p.sendMessage(CC.translate("&a✓ Unlocked &e" + aura.getDisplayName() + " &afor &b" + cost + " Gems&a!"));
                        build(); return;
                    }
                    PlayerCosmeticState s = plugin.getCosmeticManager().getState(p);
                    if (equipped) {
                        plugin.getCosmeticManager().getAuraManager().unequip(p);
                        if (s != null) s.setActiveAuraId(null);
                    } else {
                        plugin.getCosmeticManager().getAuraManager().equip(p, id);
                        if (s != null) s.setActiveAuraId(id);
                    }
                    build();
                }
            });
        }
    }


    private void buildWings(PlayerCosmeticState state) {
        WingsManager wm = plugin.getCosmeticManager().getWingsManager();
        if (wm == null) return;

        int slot = 10;
        for (BaseWings wings : new ArrayList<>(wm.getRegistry().values())) {
            if (slot >= 45) break;
            set(new Slot(slot++) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.FEATHER)
                            .name("&b" + wings.getDisplayName())
                            .lore("&6&l⚠ Coming Soon!",
                                  "&7Wings are not yet available",
                                  "&7on this server version.",
                                  "&8Stay tuned for updates!")
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.sendMessage(CC.translate("&6Wings are coming soon! Stay tuned."));
                }
            });
        }
    }


    private void buildMorphs(PlayerCosmeticState state) {
        MorphManager mm = plugin.getCosmeticManager().getMorphManager();
        if (mm == null) return;

        int slot = 10;
        for (BaseMorph morph : new ArrayList<>(mm.getRegistry().values())) {
            if (slot >= 45) break;
            set(new Slot(slot++) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.BONE)
                            .name("&7" + morph.getDisplayName())
                            .lore("&6&l⚠ Coming Soon!",
                                  "&7Morphs require ProtocolLib",
                                  "&7which is not yet installed.",
                                  "&8Stay tuned for updates!")
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.sendMessage(CC.translate("&6Morphs are coming soon! Stay tuned."));
                }
            });
        }
    }


    private void buildChatTags(PlayerCosmeticState state) {
        ChatTagManager ctm = plugin.getCosmeticManager().getChatTagManager();
        if (ctm == null) return;
        String active = state != null ? state.getActiveChatTagId() : null;

        int slot = 10;
        for (ChatTag tag : new ArrayList<>(ctm.getRegistry().values())) {
            if (slot >= 45) break;
            final String id        = tag.getId();
            final boolean owned    = isOwned(id) || (tag.hasPermission() && getPlayer().hasPermission(tag.getPermission()));
            final boolean equipped = id.equals(active);
            final int cost         = getCosmeticCost("chat-tags", id, 150);
            set(new Slot(slot++) {
                @Override public ItemStack getItem() {
                    if (!owned) {
                        boolean canAfford = plugin.getGemManager().hasGems(getPlayer().getUniqueId(), cost);
                        return new ItemBuilder(Material.PAPER)
                                .name("&c" + CC.translate(tag.getTag()))
                                .lore("&8" + tag.getDescription(),
                                      "&7Status: &cLocked",
                                      "&7Cost: &b" + cost + " Gems",
                                      canAfford ? "&eClick to purchase!" : "&cNot enough gems.").build();
                    }
                    Material icon = equipped ? Material.EMERALD : Material.PAPER;
                    return new ItemBuilder(icon)
                            .name((equipped ? "&a" : "&f") + CC.translate(tag.getTag()))
                            .lore("&8" + tag.getDescription(),
                                  equipped ? "&a&lEQUIPPED &7- Click to unequip" : "&7Click to equip").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    if (!owned) {
                        long gems = plugin.getGemManager().getGems(p.getUniqueId());
                        if (gems < cost) { p.sendMessage(CC.translate("&cNeed &b" + cost + " Gems&c. You have &b" + gems + "&c.")); return; }
                        plugin.getGemManager().removeGems(p.getUniqueId(), cost, "chattag_unlock:" + id);
                        plugin.getCosmeticManager().getUnlockManager().grantUnlock(p.getUniqueId(), id, "gem_purchase");
                        p.sendMessage(CC.translate("&a✓ Unlocked &f" + CC.translate(tag.getTag()) + " &afor &b" + cost + " Gems&a!"));
                        build(); return;
                    }
                    PlayerCosmeticState s = plugin.getCosmeticManager().getState(p);
                    if (s == null) return;
                    if (equipped) s.setActiveChatTagId(null);
                    else s.setActiveChatTagId(id);
                    build();
                }
            });
        }
    }


    private void buildJoinEffects(PlayerCosmeticState state) {
        String active = state != null ? state.getActiveJoinEffectId() : null;

        int slot = 10;
        for (JoinEffectType effect : JoinEffectType.values()) {
            if (slot >= 45) break;
            final String id        = effect.getId();
            final boolean owned    = isOwned(id) || getPlayer().hasPermission(effect.getPermission())
                                     || getPlayer().hasPermission("glimzo.joineffect.*");
            final boolean equipped = id.equals(active);
            final int cost         = getCosmeticCost("join-effects", id, 300);
            set(new Slot(slot++) {
                @Override public ItemStack getItem() {
                    if (!owned) {
                        boolean canAfford = plugin.getGemManager().hasGems(getPlayer().getUniqueId(), cost);
                        return new ItemBuilder(Material.FIREWORK)
                                .name("&c" + effect.getDisplayName())
                                .lore("&7Status: &cLocked",
                                      "&7Cost: &b" + cost + " Gems",
                                      canAfford ? "&eClick to purchase!" : "&cNot enough gems.").build();
                    }
                    Material icon = equipped ? Material.EMERALD : Material.FIREWORK;
                    return new ItemBuilder(icon)
                            .name((equipped ? "&a" : "&6") + effect.getDisplayName())
                            .lore(equipped ? "&a&lEQUIPPED &7- Click to unequip" : "&7Click to equip").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    if (!owned) {
                        long gems = plugin.getGemManager().getGems(p.getUniqueId());
                        if (gems < cost) { p.sendMessage(CC.translate("&cNeed &b" + cost + " Gems&c. You have &b" + gems + "&c.")); return; }
                        plugin.getGemManager().removeGems(p.getUniqueId(), cost, "joineffect_unlock:" + id);
                        plugin.getCosmeticManager().getUnlockManager().grantUnlock(p.getUniqueId(), id, "gem_purchase");
                        p.sendMessage(CC.translate("&a✓ Unlocked &6" + effect.getDisplayName() + " &afor &b" + cost + " Gems&a!"));
                        build(); return;
                    }
                    PlayerCosmeticState s = plugin.getCosmeticManager().getState(p);
                    if (s == null) return;
                    if (equipped) s.setActiveJoinEffectId(null);
                    else s.setActiveJoinEffectId(id);
                    build();
                }
            });
        }
    }


    private void buildAllies(PlayerCosmeticState state) {
        int slot = 10;
        for (AllyType ally : AllyType.values()) {
            if (slot >= 45) break;
            final AllyType finalAlly = ally;
            final boolean owned = isOwned(ally.getId());
            final int gemCost   = getAllyCost(ally);

            set(new Slot(slot++) {
                @Override public ItemStack getItem() {
                    if (!owned) {
                        // BUG #1 FIX: Show purchasable item with cost, not just "locked"
                        boolean canAfford = plugin.getGemManager().hasGems(getPlayer().getUniqueId(), gemCost);
                        return new ItemBuilder(Material.INK_SACK).durability((short) 8)
                                .name("&c" + finalAlly.getDisplayName())
                                .lore("&7Status: &cLocked",
                                      "&7Cost: &b" + gemCost + " Gems",
                                      canAfford ? "&eClick to purchase!" : "&cNot enough gems.",
                                      "",
                                      "&8Preview: &7" + getPerkDesc(finalAlly, 1))
                                .build();
                    }
                    // Owned - show summary, click opens detail
                    AllyPerkManager pm = plugin.getCosmeticManager().getAllyPerkManager();
                    int level = pm != null ? pm.getAllyLevel(getPlayer(), finalAlly) : 0;
                    AllyManager am = plugin.getCosmeticManager().getAllyManager();
                    boolean spawned = am != null && am.hasAlly(getPlayer())
                            && finalAlly.getId().equals(state != null ? state.getActiveAllyId() : null);

                    return new ItemBuilder(Material.MONSTER_EGG)
                            .name("&d" + finalAlly.getDisplayName())
                            .lore("&7Level: &b" + level + "&7/&b3",
                                  spawned ? "&aSpawned &7- click to manage" : "&7Click to manage")
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    if (!owned) {
                        // BUG #2 FIX: Actually purchase on click
                        long gems = plugin.getGemManager().getGems(p.getUniqueId());
                        if (gems < gemCost) {
                            p.sendMessage(CC.translate("&cYou need &b" + gemCost + " Gems &cto unlock &d"
                                    + finalAlly.getDisplayName() + "&c. You have &b" + gems + " Gems&c."));
                            return;
                        }
                        plugin.getGemManager().removeGems(p.getUniqueId(), gemCost, "ally_unlock:" + finalAlly.getId());
                        plugin.getCosmeticManager().getUnlockManager()
                                .grantUnlock(p.getUniqueId(), finalAlly.getId(), "gem_purchase");
                        // BUG #3 FIX: Set level to 1 on first purchase
                        AllyLevelManager lm = plugin.getCosmeticManager().getAllyLevelManager();
                        if (lm != null) lm.setAllyLevel(p.getUniqueId(), finalAlly, 1);
                        AllyPerkManager pm = plugin.getCosmeticManager().getAllyPerkManager();
                        if (pm != null) pm.onAllyUnlocked(p, finalAlly);
                        p.sendMessage(CC.translate("&a✓ Unlocked &d" + finalAlly.getDisplayName()
                                + " &afor &b" + gemCost + " Gems&a!"));
                        build();
                        return;
                    }
                    // Open detail submenu
                    new CosmeticBrowserMenu(plugin, p, Category.ALLIES, finalAlly).open();
                }
            });
        }
    }


    /**
     * BUG #4 FIX: Brand-new detail view per ally.
     * Shows: spawn toggle, level/meals progress, each perk, feed button.
     */
    private void buildAllyDetail(PlayerCosmeticState state, AllyType ally) {
        AllyPerkManager  pm = plugin.getCosmeticManager().getAllyPerkManager();
        AllyLevelManager lm = plugin.getCosmeticManager().getAllyLevelManager();
        AllyManager      am = plugin.getCosmeticManager().getAllyManager();

        int level      = (pm != null) ? pm.getAllyLevel(getPlayer(), ally) : 1;
        int mealsNow   = (lm != null) ? lm.getAllyPetMeals(getPlayer().getUniqueId(), ally) : 0;
        int mealsNeeded = plugin.getCosmeticManager().getCosmeticsConfig().getInt("allies.meals-per-level", 10);

        boolean spawned  = am != null && am.hasAlly(getPlayer())
                && ally.getId().equals(state != null ? state.getActiveAllyId() : null);

        set(new Slot(13) {
            @Override public ItemStack getItem() {
                List<String> lore = new ArrayList<>();
                lore.add("&8&m                              ");
                lore.add("&7Level: &b" + level + "&7/&b3");
                if (level < 3) {
                    lore.add(buildMealBar(mealsNow, mealsNeeded));
                } else {
                    lore.add("&a&lMAX LEVEL");
                }
                lore.add("");
                lore.add("&7&nPerks:");
                lore.add(buildPerkLine(ally, 1, level));
                lore.add(buildPerkLine(ally, 2, level));
                lore.add(buildPerkLine(ally, 3, level));
                return new ItemBuilder(Material.MONSTER_EGG)
                        .name("&d&l" + ally.getDisplayName())
                        .lore(lore.toArray(new String[0])).build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {}
        });

        // BUG #5 FIX: The spawn toggle - this is what was completely missing
        set(new Slot(11) {
            @Override public ItemStack getItem() {
                if (spawned) {
                    return new ItemBuilder(Material.EMERALD)
                            .name("&a&lDespawn Ally")
                            .lore("&7Your &d" + ally.getDisplayName() + " &7is currently &aspawned&7.",
                                  "&7Click to &cdespawn &7it.")
                            .build();
                } else {
                    return new ItemBuilder(Material.MONSTER_EGG)
                            .name("&e&lSpawn Ally")
                            .lore("&7Click to spawn your &d" + ally.getDisplayName() + "&7.",
                                  "&7It will follow you around!")
                            .build();
                }
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                PlayerCosmeticState s = plugin.getCosmeticManager().getState(p);
                if (spawned) {
                    // Despawn
                    if (am != null) am.removeAlly(p);
                    if (pm != null) pm.onAllyUnequipped(p, ally);
                    if (s != null) s.setActiveAllyId(null);
                    p.sendMessage(CC.translate("&7&d" + ally.getDisplayName() + " &7despawned."));
                } else {
                    // BUG #6 FIX: spawnAlly() had a permission gate that blocked purchased allies.
                    // We bypass it here since ownership is already verified by isOwned().
                    if (am != null) {
                        am.removeAlly(p); // remove any currently active ally first
                        me.pikashrey.glimzocore.features.cosmetics.ally.Ally newAlly =
                                am.createAndSpawn(p, ally);
                        if (newAlly != null) {
                            if (s != null) s.setActiveAllyId(ally.getId());
                            if (pm != null) pm.onAllyEquipped(p, ally);
                            p.sendMessage(CC.translate("&d" + ally.getDisplayName() + " &aspawned!"));
                        }
                    }
                }
                build();
            }
        });

        set(new Slot(15) {
            @Override public ItemStack getItem() {
                if (level >= 3) {
                    return new ItemBuilder(Material.GOLD_NUGGET)
                            .name("&6&lFeed Ally")
                            .lore("&7Your ally is already at &amax level&7!")
                            .build();
                }
                // Count how many distinct meal types the player owns
                me.pikashrey.glimzocore.features.cosmetics.ally.meal.AllyMealManager mm =
                        plugin.getCosmeticManager().getAllyMealManager();
                int ownedTypes = 0;
                for (me.pikashrey.glimzocore.features.cosmetics.ally.meal.AllyMealType mt :
                        me.pikashrey.glimzocore.features.cosmetics.ally.meal.AllyMealType.values()) {
                    if (mm.getQuantity(getPlayer().getUniqueId(), mt) > 0) ownedTypes++;
                }
                return new ItemBuilder(Material.BREAD)
                        .name("&6&lFeed PetMeal")
                        .lore("&7Current meals: &a" + mealsNow + "&7/&a" + mealsNeeded,
                              "&7You own &a" + ownedTypes + " &7meal type(s).",
                              "",
                              "&eClick &7to select which meal to feed.")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                if (level >= 3) {
                    p.sendMessage(CC.translate("&cYour ally is already at max level!"));
                    return;
                }
                // Open the meal-select submenu showing only meals the player owns
                new me.pikashrey.glimzocore.menus.FeedMealSubMenu(plugin, p, ally, 0).open();
            }
        });

        set(new Slot(29) {
            @Override public ItemStack getItem() {
                boolean unlocked = level >= 1;
                return new ItemBuilder(unlocked ? Material.EMERALD : Material.COAL)
                        .name((unlocked ? "&a" : "&7") + "Level 1 Perk")
                        .lore(getPerkDesc(ally, 1), unlocked ? "&aUnlocked!" : "&7Locked").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {}
        });

        set(new Slot(31) {
            @Override public ItemStack getItem() {
                boolean unlocked = level >= 2;
                return new ItemBuilder(unlocked ? Material.EMERALD : Material.COAL)
                        .name((unlocked ? "&a" : "&7") + "Level 2 Perk")
                        .lore(getPerkDesc(ally, 2), unlocked ? "&aUnlocked!" : "&7Feed more PetMeals to unlock.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {}
        });

        set(new Slot(33) {
            @Override public ItemStack getItem() {
                boolean unlocked = level >= 3;
                return new ItemBuilder(unlocked ? Material.EMERALD : Material.COAL)
                        .name((unlocked ? "&a" : "&7") + "Level 3 Perk")
                        .lore(getPerkDesc(ally, 3), unlocked ? "&aUnlocked!" : "&7Feed more PetMeals to unlock.").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {}
        });
    }


    private boolean isOwned(String cosmeticId) {
        CosmeticUnlockManager um = plugin.getCosmeticManager().getUnlockManager();
        return um != null && um.hasUnlock(getPlayer().getUniqueId(), cosmeticId);
    }

    private int getCosmeticCost(String category, String id, int defaultCost) {
        // id format: "angel_wings" → key: "wings.unlock-costs.angel_wings"
        String key = category + ".unlock-costs." + id;
        return plugin.getCosmeticManager().getCosmeticsConfig().getInt(key, defaultCost);
    }

    private int getAllyCost(AllyType ally) {
        return plugin.getCosmeticManager().getCosmeticsConfig()
                .getInt("allies.unlock-costs." + ally.getId(), 400);
    }

    private ItemStack locked(String displayName, String type) {
        return new ItemBuilder(Material.INK_SACK).durability((short) 8)
                .name("&c" + CC.translate(displayName))
                .lore("&7Status: &cLocked", "&8You don't own this " + type + ".").build();
    }

    private String buildMealBar(int now, int needed) {
        int filled = (int) ((now / (double) needed) * 10);
        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) bar.append(i < filled ? "&a|" : "&7|");
        bar.append("&8] &7").append(now).append("/").append(needed).append(" PetMeals");
        return bar.toString();
    }

    private String buildPerkLine(AllyType ally, int perkLevel, int playerLevel) {
        String desc = getPerkDesc(ally, perkLevel);
        return (playerLevel >= perkLevel ? "&a✓ " : "&7✗ ") + "&fL" + perkLevel + " " + desc;
    }

    private String getPerkDesc(AllyType ally, int level) {
        switch (ally) {
            case CHARIZARD:
                switch (level) { case 1: return "Flame Trail"; case 2: return "Jump Boost I"; case 3: return "Smoke Aura"; }
            case DR_DUCKY:
                switch (level) { case 1: return "Water Splash Particles"; case 2: return "Random Quack Sounds"; case 3: return "Double Jump"; }
            case FALCON:
                switch (level) { case 1: return "/fly Permission"; case 2: return "Speed II"; case 3: return "Dash Forward"; }
            case KITTY:
                switch (level) { case 1: return "Speed I"; case 2: return "Speed III + Pounce"; case 3: return "Paw Particles"; }
            case MR_PANDA:
                switch (level) { case 1: return "Passive Companion"; case 2: return "/sit and /lie Emotes"; case 3: return "Passive Companion"; }
            case PUG:
                switch (level) { case 1: return "Heart Particles"; case 2: return "Bone Throw"; case 3: return "Passive Companion"; }
            default: return "???";
        }
    }

    /**
     * BUG #8 FIX: Check the player's inventory for a PetMeal item.
     */
    private boolean hasPetMeal(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (isPetMeal(item)) return true;
        }
        return false;
    }

    private void consumePetMeal(Player p) {
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isPetMeal(contents[i])) {
                if (contents[i].getAmount() > 1) {
                    contents[i].setAmount(contents[i].getAmount() - 1);
                } else {
                    p.getInventory().setItem(i, null);
                }
                p.updateInventory();
                return;
            }
        }
    }

    private boolean isPetMeal(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return CC.strip(item.getItemMeta().getDisplayName()).equalsIgnoreCase("PetMeal");
    }
}
