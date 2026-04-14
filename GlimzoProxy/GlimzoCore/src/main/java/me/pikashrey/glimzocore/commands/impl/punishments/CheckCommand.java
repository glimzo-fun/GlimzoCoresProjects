package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class CheckCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public CheckCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.punish.check"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/check <player>"); return; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(0));
        if (target == null || target.getName() == null) { a.sendError("Player not found."); return; }

        a.send("&6Check &7» &f" + target.getName());
        
        // Get all punishments history
        List<Punishment> history = plugin.getPunishmentManager().getHistory(target.getUniqueId());
        
        if (history.isEmpty()) {
            a.send("  &aNo punishment history.");
            
            // Open GUI if player is online
            if (a.isPlayer()) {
                new me.pikashrey.glimzocore.menus.punishments.CheckMenu(
                    plugin, a.getPlayer(), target.getUniqueId(), target.getName()).open();
            }
            return;
        }
        
        // Show active bans/mutes
        Punishment activeBan = plugin.getPunishmentManager().getActiveBan(target.getUniqueId());
        if (activeBan != null) {
            a.send("  &cBan (ACTIVE) &7- " + activeBan.getReason()
                    + " &7(" + TimeFormatUtils.formatExpiry(activeBan.getExpiresAt()) + ")");
        }

        Punishment activeMute = plugin.getPunishmentManager().getActiveMute(target.getUniqueId());
        if (activeMute != null) {
            a.send("  &eMute (ACTIVE) &7- " + activeMute.getReason()
                    + " &7(" + TimeFormatUtils.formatExpiry(activeMute.getExpiresAt()) + ")");
        }
        
        // Count total punishments by type
        int totalBans = 0, totalMutes = 0, totalWarns = 0;
        for (Punishment p : history) {
            if (p.getType() == PunishmentType.BAN || p.getType() == PunishmentType.TEMP_BAN) totalBans++;
            else if (p.getType() == PunishmentType.MUTE || p.getType() == PunishmentType.TEMP_MUTE) totalMutes++;
            else if (p.getType() == PunishmentType.WARN) totalWarns++;
        }
        
        a.send("  &7Total Bans: &c" + totalBans);
        a.send("  &7Total Mutes: &e" + totalMutes);
        a.send("  &7Total Warnings: &6" + totalWarns);
        
        // Open GUI if player is online
        if (a.isPlayer()) {
            Player player = a.getPlayer();
            new me.pikashrey.glimzocore.menus.punishments.CheckMenu(
                plugin, player, target.getUniqueId(), target.getName()).open();
        }
    }
}
