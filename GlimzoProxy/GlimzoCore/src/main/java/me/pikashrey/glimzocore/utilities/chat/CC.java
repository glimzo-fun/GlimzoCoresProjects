package me.pikashrey.glimzocore.utilities.chat;

import org.bukkit.ChatColor;

public class CC {

    public static String translate(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String strip(String message) {
        return ChatColor.stripColor(translate(message));
    }
}
