package me.pikashrey.glimzocore.commands.impl.essential.messages;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MsgCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public MsgCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        if (!a.has(1)) { a.usage("/msg <player> <message>"); return; }
        Player target = Bukkit.getPlayer(a.get(0));
        if (target == null || target.equals(player)) { a.sendError("Player not online."); return; }
        plugin.getChatManager().sendPrivateMessage(player, target, a.join(1));
    }
}
