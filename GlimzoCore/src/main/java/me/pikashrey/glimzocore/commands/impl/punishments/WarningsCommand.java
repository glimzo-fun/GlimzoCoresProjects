package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.utilities.general.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.List;
import java.util.stream.Collectors;

public class WarningsCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public WarningsCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        OfflinePlayer target = a.has(0) && a.getSender().hasPermission("glimzo.punish.history")
                ? Bukkit.getOfflinePlayer(a.get(0))
                : (a.isPlayer() ? a.getPlayer() : null);
        if (target == null) { a.sendError("Specify a player or use in-game."); return; }

        List<Punishment> warns = plugin.getPunishmentManager().getHistory(target.getUniqueId())
                .stream().filter(p -> p.getType() == PunishmentType.WARN).collect(Collectors.toList());

        if (warns.isEmpty()) { a.send("&7" + target.getName() + " has no warnings."); return; }
        a.send("&eWarnings for &f" + target.getName() + " &e(" + warns.size() + "):");
        for (Punishment p : warns) {
            a.send("  &7- &f" + p.getReason() + " &7by &f" + p.getStaffName()
                    + " &7(" + DateUtils.timeAgo(p.getIssuedAt()) + ")");
        }
    }
}
