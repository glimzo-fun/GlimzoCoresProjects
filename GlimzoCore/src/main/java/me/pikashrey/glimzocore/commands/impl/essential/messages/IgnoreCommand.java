package me.pikashrey.glimzocore.commands.impl.essential.messages;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class IgnoreCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public IgnoreCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        if (!a.has(0)) { a.usage("/ignore <player>"); return; }
        Player target = Bukkit.getPlayer(a.get(0));
        if (target == null || target.equals(player)) { a.sendError("Player not online."); return; }

        if (plugin.getChatManager().isIgnoring(player.getUniqueId(), target.getUniqueId())) {
            plugin.getChatManager().unignore(player.getUniqueId(), target.getUniqueId());
            a.sendSuccess("You are no longer ignoring &f" + target.getName() + "&a.");
        } else {
            plugin.getChatManager().ignore(player.getUniqueId(), target.getUniqueId());
            a.sendSuccess("You are now ignoring &f" + target.getName() + "&a.");
        }
    }
}
