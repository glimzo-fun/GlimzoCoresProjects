package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TempBanCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public TempBanCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.punish.ban"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(2)) { a.usage("/tempban <player> <duration> [reason]"); return; }
        String targetName = a.get(0);
        long duration = TimeFormatUtils.parse(a.get(1));
        if (duration == 0) { a.sendError("Invalid duration. Example: 30m, 2h, 7d"); return; }
        String reason = a.has(2) ? a.join(2) : "Temporary ban";

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

        long expires = duration == -1 ? -1 : System.currentTimeMillis() + duration;
        Punishment p = Punishment.create(target.getUniqueId(), resolvedName,
                a.isPlayer() ? a.getPlayer().getUniqueId() : null,
                a.getSender().getName(),
                PunishmentType.TEMP_BAN, reason, expires);
        plugin.getPunishmentManager().punish(p);
        a.sendSuccess("Temp-banned &f" + resolvedName + " &afor &f" + a.get(1) + "&a. Reason: &f" + reason);
    }
}
