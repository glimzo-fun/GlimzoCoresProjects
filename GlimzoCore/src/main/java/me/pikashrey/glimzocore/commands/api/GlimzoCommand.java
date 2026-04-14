package me.pikashrey.glimzocore.commands.api;

import org.bukkit.command.CommandSender;

public interface GlimzoCommand {

    void execute(CommandArgs args);

    /**
     * Return tab-completion suggestions. Default: empty list.
     */
    default java.util.List<String> tabComplete(CommandArgs args) {
        return java.util.Collections.emptyList();
    }

    default String getPermission() { return null; }

    /**
     * Whether this command can only be run by a player (not console).
     */
    default boolean isPlayerOnly() { return true; }
}

