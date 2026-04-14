package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class UnmuteCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public UnmuteCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.punish.mute"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/unmute <player>"); return; }
        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(0));
        if (target == null || !target.hasPlayedBefore()) { a.sendError("Player not found."); return; }
        plugin.getPunishmentManager().unmute(target.getUniqueId(),
                a.isPlayer() ? a.getPlayer().getUniqueId() : null, a.getSender().getName());
        a.sendSuccess("Unmuted &f" + target.getName() + "&a.");
    }
}
