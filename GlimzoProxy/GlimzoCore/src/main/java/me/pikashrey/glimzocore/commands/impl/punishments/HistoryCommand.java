package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.utilities.general.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.List;

public class HistoryCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public HistoryCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.punish.history"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/history <player>"); return; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(0));
        if (target == null || target.getName() == null) { a.sendError("Player not found."); return; }

        List<Punishment> list = plugin.getPunishmentManager().getHistory(target.getUniqueId());
        if (list.isEmpty()) { a.send("&7" + target.getName() + " has no punishment history."); return; }

        a.send("&8&m-----------------");
        a.send("&6History &7for &f" + target.getName() + " &7(" + list.size() + ")");
        a.send("&8&m-----------------");
        for (Punishment p : list) {
            String status = p.isActive() ? "&a●" : "&8●";
            a.send(status + " &7[&f" + p.getType().name() + "&7] &f" + p.getReason()
                    + " &7by &f" + p.getStaffName()
                    + " &7- &f" + DateUtils.timeAgo(p.getIssuedAt()));
        }
    }
}
