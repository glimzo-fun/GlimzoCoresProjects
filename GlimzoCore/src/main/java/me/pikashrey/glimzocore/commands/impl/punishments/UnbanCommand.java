package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class UnbanCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public UnbanCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.punish.ban"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/unban <player>"); return; }
        String playerName = a.get(0);
        
        // Try to get online player first, then fallback to offline
        org.bukkit.entity.Player onlineTarget = Bukkit.getPlayer(playerName);
        OfflinePlayer target;
        
        if (onlineTarget != null) {
            target = onlineTarget;
        } else {
            // For offline players, we need to look them up by name or UUID
            target = Bukkit.getOfflinePlayer(playerName);
        }
        
        if (target == null || target.getUniqueId() == null || !target.hasPlayedBefore()) {
            a.sendError("Player not found.");
            return;
        }
        
        plugin.getPunishmentManager().unban(target.getUniqueId(),
                a.isPlayer() ? a.getPlayer().getUniqueId() : null, a.getSender().getName());
        a.sendSuccess("Unbanned &f" + (target.getName() != null ? target.getName() : playerName) + "&a.");
    }
}
