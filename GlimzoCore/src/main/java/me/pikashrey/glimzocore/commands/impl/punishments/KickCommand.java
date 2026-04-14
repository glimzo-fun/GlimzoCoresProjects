package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KickCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public KickCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.punish.kick"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/kick <player> [reason]"); return; }
        Player target = Bukkit.getPlayer(a.get(0));
        if (target == null) { a.sendError("Player not online."); return; }
        String reason = a.has(1) ? a.join(1) : "Kicked by staff";
        Punishment p = Punishment.create(target.getUniqueId(), target.getName(),
                a.isPlayer() ? a.getPlayer().getUniqueId() : null, a.getSender().getName(),
                PunishmentType.KICK, reason, System.currentTimeMillis());
        plugin.getPunishmentManager().punish(p);
        target.kickPlayer(CC.translate("&cYou were kicked.\n&fReason: &e" + reason));
        a.sendSuccess("Kicked &f" + target.getName() + "&a. Reason: &f" + reason);
    }
}
