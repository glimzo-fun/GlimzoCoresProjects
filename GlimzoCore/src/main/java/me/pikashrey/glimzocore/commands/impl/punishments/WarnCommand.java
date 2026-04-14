package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WarnCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public WarnCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.punish.warn"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(1)) { a.usage("/warn <player> <reason>"); return; }
        Player target = Bukkit.getPlayer(a.get(0));
        if (target == null) { a.sendError("Player not online."); return; }
        String reason = a.join(1);
        Punishment p = Punishment.create(target.getUniqueId(), target.getName(),
                a.isPlayer() ? a.getPlayer().getUniqueId() : null, a.getSender().getName(),
                PunishmentType.WARN, reason, -1);
        plugin.getPunishmentManager().punish(p);
        String warnMsg = plugin.getConfigManager().getMessage("warn.received", "&c&lWarning! &fYou have been warned: &e{reason}")
                .replace("{reason}", reason);
        target.sendMessage(CC.translate(warnMsg));
        a.sendSuccess("Warned &f" + target.getName() + "&a: &f" + reason);
    }
}
