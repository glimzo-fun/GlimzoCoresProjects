package me.pikashrey.glimzocore.features.hotbar;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyItemManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import me.pikashrey.glimzocore.menus.CosmeticsMenu;
import me.pikashrey.glimzocore.menus.ProfileMenu;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class HotbarManager implements Listener {

    private static final String COSMETICS_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWQ4NmU3YmQyOGMxNDZmNzE1MTRjNzgyY2FjMDU1ODYwZDFmMzcyYjRhOWJlM2ZlNjVjZmUxMTA0NzMzYmEifX19";
    private static final String GLOBE_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjFkZDRmZTRhNDI5YWJkNjY1ZGZkYjNlMjEzMjFkNmVmYTZhNmI1ZTdiOTU2ZGI5YzVkNTljOWVmYWIyNSJ9fX0=";
    private static final String MARIO_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJhOGQ4ZTUzZDhhNWE3NTc3MGI2MmNjZTczZGI2YmFiNzAxY2MzZGU0YTliNjU0ZDIxM2Q1NGFmOTYxNSJ9fX0=";
    private static final String BATMAN_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI1NmY3MTczNWVmNDU4NTgxYzlkYWNmMzk0MTg1ZWVkOWIzM2NiNmVjNWNkNTk0YTU3MTUzYThiNTY2NTYwIn19fQ==";
    private static final String STORE_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2ZlMjQ0OWQyMjdmYTk5MWYyYTcxMDI3MDlkOTk4MmRlMWY3OGY0MTE4MjYyOGJkYWUzOTQ2MjI1NGM2ODFhMyJ9fX0=";

    private static final String TAG_PROFILE    = "\u00a70\u00a70HOTBAR_PROFILE";
    private static final String TAG_COSMETICS  = "\u00a70\u00a70HOTBAR_COSMETICS";
    private static final String TAG_VISIBILITY = "\u00a70\u00a70HOTBAR_VISIBILITY";
    private static final String TAG_GAMEMODE   = "\u00a70\u00a70HOTBAR_GAMEMODE";
    private static final String TAG_STORE      = "\u00a70\u00a70HOTBAR_STORE";

    private final GlimzoCore plugin;

    public HotbarManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void giveHotbar(Player player) {
        // Only clear the 9 hotbar slots (indices 0-8), not the full inventory
        // Clearing all inventory would destroy staff tools, ally meals, etc.
        for (int i = 0; i < 9; i++) player.getInventory().setItem(i, null);
        player.getInventory().setItem(0, buildProfile(player));
        player.getInventory().setItem(1, buildCosmetics());
        updateAllySlot(player);
        // slots 3, 5, 6 stay empty
        player.getInventory().setItem(4, buildGameSelector());
        player.getInventory().setItem(7, buildVisibility(player));
        player.getInventory().setItem(8, buildStore());
        player.getInventory().setHeldItemSlot(4); // default to slot 5
    }

    public void updateAllySlot(Player player) {
        AllyPerkManager pm = plugin.getCosmeticManager() != null
                ? plugin.getCosmeticManager().getAllyPerkManager() : null;
        AllyType equipped = pm != null ? pm.getEquippedAlly(player.getUniqueId()) : null;
        if (equipped != null) {
            ItemStack skull = SkullBuilder.fromBase64(equipped.getHeadTexture());
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName(CC.translate("&d" + equipped.getDisplayName() + " &7Ally"));
            meta.setLore(Arrays.asList(
                    "\u00a77Left-click: \u00a7eUse ability",
                    "\u00a77Right-click: \u00a7eOpen ally menu",
                    AllyItemManager.LORE_TAG
            ));
            skull.setItemMeta(meta);
            player.getInventory().setItem(2, skull);
        } else {
            player.getInventory().setItem(2, null);
        }
    }

    public void updateVisibilitySlot(Player player) {
        player.getInventory().setItem(7, buildVisibility(player));
    }

    private ItemStack buildProfile(Player player) {
        ItemStack skull = SkullBuilder.fromPlayer(player);
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(CC.translate("&f" + player.getName() + "'s Profile"));
        meta.setLore(Arrays.asList("\u00a77Click to open your profile.", TAG_PROFILE));
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack buildCosmetics() {
        ItemStack skull = SkullBuilder.fromBase64(COSMETICS_TEXTURE);
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(CC.translate("&bCosmetics"));
        meta.setLore(Arrays.asList("\u00a77Click to open cosmetics menu.", TAG_COSMETICS));
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack buildGameSelector() {
        ItemStack skull = SkullBuilder.fromBase64(GLOBE_TEXTURE);
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(CC.translate("&aGame Selector"));
        meta.setLore(Arrays.asList(
                "\u00a77Click to browse games.",
                "\u00a78(Proxy setup coming soon)",
                TAG_GAMEMODE
        ));
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack buildVisibility(Player player) {
        boolean on = plugin.getSettingsManager().isPlayerVisibilityEnabled(player.getUniqueId());
        ItemStack skull = SkullBuilder.fromBase64(on ? MARIO_TEXTURE : BATMAN_TEXTURE);
        ItemMeta meta = skull.getItemMeta();
        if (on) {
            meta.setDisplayName(CC.translate("&aPlayer Visibility: ON"));
            meta.setLore(Arrays.asList("\u00a77Players are \u00a7avisible\u00a77.", "\u00a77Click to hide them.", TAG_VISIBILITY));
        } else {
            meta.setDisplayName(CC.translate("&cPlayer Visibility: OFF"));
            meta.setLore(Arrays.asList("\u00a77Players are \u00a7chidden\u00a77.", "\u00a77Click to show them.", TAG_VISIBILITY));
        }
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack buildStore() {
        ItemStack skull = SkullBuilder.fromBase64(STORE_TEXTURE);
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(CC.translate("&6&lStore"));
        meta.setLore(Arrays.asList("\u00a77Click to browse the store.", "\u00a78GUI coming soon!", TAG_STORE));
        skull.setItemMeta(meta);
        return skull;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action a = event.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK
                && a != Action.LEFT_CLICK_AIR && a != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;
        List<String> lore = item.getItemMeta().getLore();

        if (lore.contains(TAG_PROFILE)) {
            event.setCancelled(true);
            me.pikashrey.glimzocore.api.player.PlayerData data =
                    me.pikashrey.glimzocore.api.player.GlobalPlayer.get(player.getUniqueId());
            if (data != null) new ProfileMenu(plugin, player, data).open();

        } else if (lore.contains(TAG_COSMETICS)) {
            event.setCancelled(true);
            new CosmeticsMenu(plugin, player).open();

        } else if (lore.contains(TAG_GAMEMODE)) {
            event.setCancelled(true);
            player.sendMessage(CC.translate("&6Game Selector coming soon via proxy!"));

        } else if (lore.contains(TAG_VISIBILITY)) {
            event.setCancelled(true);
            boolean newState = plugin.getSettingsManager().togglePlayerVisibility(player.getUniqueId());
            player.sendMessage(CC.translate("&7Player visibility " + (newState ? "&aenabled" : "&cdisabled") + "&7."));
            updateVisibilitySlot(player);

        } else if (lore.contains(TAG_STORE)) {
            event.setCancelled(true);
            new me.pikashrey.glimzocore.menus.StoreMenu(plugin, player).open();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return;
        List<String> lore = item.getItemMeta().getLore();
        if (lore.contains(TAG_PROFILE) || lore.contains(TAG_COSMETICS)
                || lore.contains(TAG_VISIBILITY) || lore.contains(TAG_GAMEMODE)
                || lore.contains(TAG_STORE) || lore.contains(AllyItemManager.LORE_TAG)) {
            event.setCancelled(true);
        }
    }
}