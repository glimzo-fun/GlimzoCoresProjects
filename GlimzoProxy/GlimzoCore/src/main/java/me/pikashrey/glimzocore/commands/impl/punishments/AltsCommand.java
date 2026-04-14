package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class AltsCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public AltsCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.punish.check"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (a.getArgs().length < 1) { a.send("&cUsage: /alts <player>"); return; }
        String target = a.getArgs()[0];

        a.send("&7Looking up alts for &f" + target + "&7...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = resolveUuid(target);
            if (uuid == null) {
                Bukkit.getScheduler().runTask(plugin, () -> a.send("&cPlayer not found: " + target));
                return;
            }

            List<String> alts = plugin.getMysqlManager().getAlts(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (alts.isEmpty()) {
                    a.send("&7No known alts for &f" + target + "&7.");
                } else {
                    a.send("&7Alts for &f" + target + " &7(" + alts.size() + "):");
                    for (String name : alts) a.send("  &8- &f" + name);
                }
            });
        });
    }

    @SuppressWarnings("deprecation")
    private UUID resolveUuid(String name) {
        org.bukkit.entity.Player online = Bukkit.getPlayer(name);
        if (online != null) return online.getUniqueId();
        OfflinePlayer off = Bukkit.getOfflinePlayer(name);
        return (off != null && off.getName() != null) ? off.getUniqueId() : null;
    }
}
