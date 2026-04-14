package me.pikashrey.glimzocore.features.cosmetics.ally;

import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Gives the player their equipped ally's skull in hotbar slot 3 (index 2).
 * Left-click = special ability. Right-click = open ally detail menu.
 * The item is identified by a lore tag so we never confuse it with real items.
 */
public class AllyItemManager {

    public static final int    HOTBAR_SLOT = 2;
    public static final String LORE_TAG    = "§0§0ALLY_SKULL_ITEM"; // hidden tag in lore

    // Track which ally each player's current skull belongs to
    private final Map<UUID, AllyType> heldAlly = new ConcurrentHashMap<>();

    public void giveItem(Player player, AllyType ally) {
        removeItem(player); // clear old first
        ItemStack skull = buildSkull(ally);
        player.getInventory().setItem(HOTBAR_SLOT, skull);
        heldAlly.put(player.getUniqueId(), ally);
    }

    public void removeItem(Player player) {
        ItemStack current = player.getInventory().getItem(HOTBAR_SLOT);
        if (isAllySkull(current)) {
            player.getInventory().setItem(HOTBAR_SLOT, null);
        }
        heldAlly.remove(player.getUniqueId());
    }

    public boolean isAllySkull(ItemStack item) {
        if (item == null) return false;
        if (item.getItemMeta() == null) return false;
        if (!item.getItemMeta().hasLore()) return false;
        return item.getItemMeta().getLore().contains(LORE_TAG);
    }

    public AllyType getAllyFromSkull(ItemStack item) {
        if (!isAllySkull(item)) return null;
        for (AllyType t : AllyType.values()) {
            if (item.getItemMeta().getDisplayName().contains(t.getDisplayName())) return t;
        }
        return null;
    }

    private ItemStack buildSkull(AllyType ally) {
        ItemStack skull = SkullBuilder.fromBase64(getTexture(ally));
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName("§f" + ally.getDisplayName() + " §7Ally");
        meta.setLore(Arrays.asList(
                "§7Left-click: §eUse ability",
                "§7Right-click: §eOpen ally menu",
                LORE_TAG
        ));
        skull.setItemMeta(meta);
        return skull;
    }

    private String getTexture(AllyType ally) {
        switch (ally) {
            case CHARIZARD: return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkzN2ZiYTBiMWU5ODg1ZmI0YTg0YzkxNTA1MTNkZWU4YjIxN2NkMDRmMTQwZDI1MDVjYWI4YWUzOWI1ZDQifX19";
            case FALCON:    return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDE2ZjU5NWU4ZjY3OTFiYzE1NDY1OWE4OTc2ZjZhOGZmZDk4NDdjZjc1YTJiZjYzOTkyZTNhNjU1ZTAifX19";
            case PUG:       return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmViZjdiNmUxZTY1MzRkODU4ZDRhZjA4MTM1OGM4ZTNjZGE3ZDQ4NzYxM2FiYjY1ODBhZmYzZjE4NTE0M2EifX19";
            case DR_DUCKY:  return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM1N2ZiOGUzMjQyOWI3MWM2NjhkYjg2NjI4YTZkMWM0MDg2MzJiZDgzNWJmYWZhYTdlOTliOTQ0MGRjYTgifX19";
            case MR_PANDA:  return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTk1ODU5ZWZkYjRmNzYyNmJjMjA1NTM0MWNkMmZhYWIzY2MwNjAyYzhhY2I1YzkxNDg1ZmRiYmFlMzExMzI1NCJ9fX0=";
            case KITTY:     return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjMyMTY2NzFmYzZiNGM1NzkyZTBjOGI5YzYzYjdiZGRiYThkZGU0OGI5NDM4MzgxZWIyNTFiNGYwNTg4MTU4ZSJ9fX0=";
            default:        return "";
        }
    }
}