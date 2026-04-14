package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BanCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public BanCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.punish.ban"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(1)) { a.usage("/ban <player> [reason]"); return; }
        String targetName = a.get(0);
        String reason = a.has(1) ? a.join(1) : "Inappropriate behaviour";

        // Try online first, then offline
        Player online = Bukkit.getPlayer(targetName);
        OfflinePlayer target;
        String resolvedName;

        if (online != null) {
            target = online;
            resolvedName = online.getName();
        } else {
            target = Bukkit.getOfflinePlayer(targetName);
            if (target == null || !target.hasPlayedBefore()) {
                a.sendError("Player not found.");
                return;
            }
            resolvedName = target.getName() != null ? target.getName() : targetName;
        }

        Punishment p = Punishment.create(target.getUniqueId(), resolvedName,
                a.isPlayer() ? a.getPlayer().getUniqueId() : null,
                a.isPlayer() ? a.getSender().getName() : "CONSOLE",
                PunishmentType.BAN, reason, -1);
        plugin.getPunishmentManager().punish(p);
        a.sendSuccess("Banned &f" + resolvedName + " &apermanently. Reason: &f" + reason);
    }
}
