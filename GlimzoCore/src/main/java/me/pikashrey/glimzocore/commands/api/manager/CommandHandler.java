package me.pikashrey.glimzocore.commands.api.manager;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final GlimzoCore  plugin;
    private final GlimzoCommand command;

    public CommandHandler(GlimzoCore plugin, GlimzoCommand command) {
        this.plugin  = plugin;
        this.command = command;
    }

    public static void register(GlimzoCore plugin, String name, GlimzoCommand cmd) {
        CommandHandler handler = new CommandHandler(plugin, cmd);
        org.bukkit.command.PluginCommand pluginCmd = plugin.getCommand(name);
        if (pluginCmd == null) {
            plugin.log("&c[CommandHandler] No command '" + name + "' in plugin.yml - skipping.");
            return;
        }
        pluginCmd.setExecutor(handler);
        pluginCmd.setTabCompleter(handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Player-only check
        if (command.isPlayerOnly() && !(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(CC.translate("&cThis command can only be used by players."));
            return true;
        }

        // Permission check
        String perm = command.getPermission();
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage(CC.translate("&cYou don't have permission to do that."));
            return true;
        }

        command.execute(new CommandArgs(plugin, sender, label, args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        String perm = command.getPermission();
        if (perm != null && !sender.hasPermission(perm)) return Collections.emptyList();
        List<String> completions = command.tabComplete(
                new CommandArgs(plugin, sender, label, args));
        return completions != null ? completions : Collections.emptyList();
    }
}

