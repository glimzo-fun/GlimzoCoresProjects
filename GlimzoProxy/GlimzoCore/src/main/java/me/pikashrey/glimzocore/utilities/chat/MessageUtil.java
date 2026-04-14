package me.pikashrey.glimzocore.utilities.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Helpers for sending formatted messages to players and the console.
 */
public final class MessageUtil {

    private static final String PREFIX = CC.translate("&8[&bGlimzo&8] &r");

    private MessageUtil() {}

    /** Send a color-translated message with the Glimzo prefix. */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + CC.translate(message));
    }

    /** Send a raw color-translated message without the prefix. */
    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(CC.translate(message));
    }

    /** Send a message to every online player. */
    public static void broadcast(String message) {
        String formatted = CC.translate(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(formatted);
        }
    }

    /** Send a message with the prefix to every online player. */
    public static void broadcastPrefixed(String message) {
        broadcast(PREFIX + message);
    }

    /** Send a message to a collection of players. */
    public static void sendToAll(Collection<? extends Player> players, String message) {
        String formatted = CC.translate(message);
        for (Player p : players) p.sendMessage(formatted);
    }

    /** Send an error message (red prefix). */
    public static void error(CommandSender sender, String message) {
        sender.sendMessage(CC.translate("&c" + message));
    }

    /** Send a success message (green). */
    public static void success(CommandSender sender, String message) {
        sender.sendMessage(CC.translate("&a" + message));
    }

    /** Send a divider line. */
    public static void divider(CommandSender sender) {
        sender.sendMessage(CC.translate("&8&m----------------------------------------"));
    }
}

