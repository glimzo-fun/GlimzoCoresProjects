package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.listeners.ChatListener;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TempMuteCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public TempMuteCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.punish.mute"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        // Requires: /tempmute <player> <duration> [reason...]
        // Minimum 2 args: player + duration. Reason is optional.
        if (!a.has(1)) { a.usage("/tempmute <player> <duration> [reason]"); return; }

        String targetName = a.get(0);
        long   duration   = TimeFormatUtils.parse(a.get(1));

        if (duration == 0) {
            a.sendError("Invalid duration. Examples: 30m, 2h, 7d, 1w");
            return;
        }

        // duration == -1 means "permanent" - redirect to /mute
        if (duration == -1) {
            a.sendError("Use /mute for a permanent mute.");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || target.getName() == null) {
            a.sendError("Player not found.");
            return;
        }

        String reason  = a.has(2) ? a.join(2) : "Temporary mute";
        long   expires = System.currentTimeMillis() + duration;

        Punishment p = Punishment.create(
                target.getUniqueId(), target.getName(),
                a.isPlayer() ? a.getPlayer().getUniqueId() : null,
                a.getSender().getName(),
                PunishmentType.TEMP_MUTE, reason, expires);

        plugin.getPunishmentManager().punish(p);

        // Notify the muted player if they are online
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) {
            online.sendMessage(CC.translate(
                    "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "&c&l✗ &cYou have been muted.\n" +
                    "&7Reason &8» &f" + reason + "\n" +
                    "&7Duration &8» &c" + ChatListener.formatRemaining(duration) + "\n" +
                    "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        }

        a.sendSuccess("Temp-muted &f" + target.getName()
                + " &afor &f" + ChatListener.formatRemaining(duration)
                + "&a. Reason: &f" + reason);
    }
}
