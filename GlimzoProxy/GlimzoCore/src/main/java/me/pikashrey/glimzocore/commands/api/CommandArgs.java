package me.pikashrey.glimzocore.commands.api;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandArgs {

    private final GlimzoCore  plugin;
    private final CommandSender sender;
    private final String      label;
    private final String[]    args;

    public CommandArgs(GlimzoCore plugin, CommandSender sender, String label, String[] args) {
        this.plugin = plugin;
        this.sender = sender;
        this.label  = label;
        this.args   = args;
    }

    // --- Sender ---

    public CommandSender getSender()   { return sender; }
    public String        getLabel()    { return label; }
    public String[]      getArgs()     { return args; }
    public int           length()      { return args.length; }
    public GlimzoCore    getPlugin()   { return plugin; }

    public boolean isPlayer() { return sender instanceof Player; }

    public Player getPlayer() {
        if (sender instanceof Player) return (Player) sender;
        sender.sendMessage(CC.translate("&cThis command can only be used by players."));
        return null;
    }

    // --- Args helpers ---

    public String get(int index) {
        return index < args.length ? args[index] : null;
    }

    public String get(int index, String def) {
        return index < args.length ? args[index] : def;
    }

    public boolean has(int index) {
        return index < args.length;
    }

    /** Join args from startIndex to end into a single space-separated string. */
    public String join(int startIndex) {
        if (startIndex >= args.length) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }

    // --- Messaging shorthands ---

    public void send(String message) {
        sender.sendMessage(CC.translate(message));
    }

    public void sendError(String message) {
        sender.sendMessage(CC.translate("&c&l✗ &c" + message));
    }

    public void sendSuccess(String message) {
        sender.sendMessage(CC.translate("&a&l✔ &a" + message));
    }

    public void noPermission() {
        sender.sendMessage(CC.translate("&c&l✗ &cYou do not have permission to use this command."));
    }

    public void usage(String usage) {
        sender.sendMessage(CC.translate("&2&l▶ &7Usage: &f" + usage));
    }
}

