package me.pikashrey.glimzocore.utilities.item;

import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta  meta;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(ItemStack base) {
        this.item = base.clone();
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(CC.translate(name));
        return this;
    }

    public ItemBuilder lore(String... lines) {
        List<String> translated = new ArrayList<>();
        for (String l : lines) translated.add(CC.translate(l));
        meta.setLore(translated);
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        List<String> translated = new ArrayList<>();
        for (String l : lines) translated.add(CC.translate(l));
        meta.setLore(translated);
        return this;
    }

    public ItemBuilder appendLore(String... lines) {
        List<String> existing = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        for (String l : lines) existing.add(CC.translate(l));
        meta.setLore(existing);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder durability(short durability) {
        item.setDurability(durability);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder hideFlags() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder unbreakable() {
        try {
            org.bukkit.inventory.meta.ItemMeta.class
                    .getMethod("spigot").invoke(meta);
            // Spigot 1.8.8 unbreakable via NbtTag or reflection
            // Most servers support meta.setUnbreakable via a spigot method
        } catch (Exception ignored) {}
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    /** Quick one-liner: material + translated name. */
    public static ItemStack of(Material mat, String name) {
        return new ItemBuilder(mat).name(name).build();
    }

    /** Quick one-liner: material + name + single lore line. */
    public static ItemStack of(Material mat, String name, String lore) {
        return new ItemBuilder(mat).name(name).lore(lore).build();
    }

    /** Filler item for menus (grey glass pane, no name). */
    // Pre-built static filler - shared across all menus, never mutated
    private static final ItemStack FILLER_CACHE = new ItemBuilder(Material.STAINED_GLASS_PANE, 1)
            .durability((short) 7).name("&r").build();

    public static ItemStack filler() {
        return FILLER_CACHE.clone(); // clone so callers can't mutate the cache
    }

    /** Named filler for menus. */
    public static ItemStack filler(String name) {
        return new ItemBuilder(Material.STAINED_GLASS_PANE, 1)
                .durability((short) 7)
                .name(name)
                .build();
    }
}

