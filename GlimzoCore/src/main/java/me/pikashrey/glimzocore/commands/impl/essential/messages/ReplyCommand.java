package me.pikashrey.glimzocore.commands.impl.essential.messages;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class ReplyCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public ReplyCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        if (!a.has(0)) { a.usage("/reply <message>"); return; }
        UUID lastSender = plugin.getChatManager().getLastMessageSender(player.getUniqueId());
        if (lastSender == null) { a.sendError("No one to reply to."); return; }
        Player target = Bukkit.getPlayer(lastSender);
        if (target == null) { a.sendError("That player is no longer online."); return; }
        plugin.getChatManager().sendPrivateMessage(player, target, a.join(0));
    }
}
