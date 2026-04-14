package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class MuteCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public MuteCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.punish.mute"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/mute <player> [reason]"); return; }
        String reason = a.has(1) ? a.join(1) : "Inappropriate language";
        OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(0));
        if (target == null) { a.sendError("Player not found."); return; }
        Punishment p = Punishment.create(target.getUniqueId(), target.getName(),
                a.isPlayer() ? a.getPlayer().getUniqueId() : null, a.getSender().getName(),
                PunishmentType.MUTE, reason, -1);
        plugin.getPunishmentManager().punish(p);

        // Notify the muted player if they are online
        org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(target.getUniqueId());
        if (online != null) {
            online.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate(
                    "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "&c&l✗ &cYou have been permanently muted.\n" +
                    "&7Reason &8» &f" + reason + "\n" +
                    "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        }

        a.sendSuccess("Permanently muted &f" + target.getName() + "&a. Reason: &f" + reason);
    }
}
