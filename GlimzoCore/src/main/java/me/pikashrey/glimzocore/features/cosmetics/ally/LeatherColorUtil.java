package me.pikashrey.glimzocore.features.cosmetics.ally;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherColorUtil {

    public static ItemStack dyedHelmet(int r, int g, int b) {
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(r, g, b));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack dyedChestplate(int r, int g, int b) {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(r, g, b));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a dyed leather leggings with the given RGB color.
     */
    public static ItemStack dyedLeggings(int r, int g, int b) {
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(r, g, b));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a dyed leather boots with the given RGB color.
     */
    public static ItemStack dyedBoots(int r, int g, int b) {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(r, g, b));
        item.setItemMeta(meta);
        return item;
    }

    // Pre-made colors used by the ally system

    /** Falcon body - tan/brown */
    public static ItemStack falconBody()    { return dyedHelmet(139, 100, 50); }

    /** Charizard body - orange-red */
    public static ItemStack charizardBody() { return dyedHelmet(220, 80, 20); }

    /** Mr. Panda body - white */
    public static ItemStack pandaBody()     { return dyedHelmet(240, 240, 240); }

    /** Dr. Ducky body - yellow */
    public static ItemStack duckyBody()     { return dyedHelmet(255, 215, 0); }
}