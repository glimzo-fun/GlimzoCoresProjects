package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PingCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public PingCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player target = a.has(0) ? Bukkit.getPlayer(a.get(0)) : (a.isPlayer() ? a.getPlayer() : null);
        if (target == null) { a.sendError("Player not online."); return; }
        try {
            int ping = (int) target.getClass().getMethod("getPing").invoke(target);
            String color = ping < 80 ? "&a" : ping < 150 ? "&e" : "&c";
            a.send("&7" + target.getName() + "'s ping: " + color + ping + "ms");
        } catch (Exception e) {
            a.send("&7Could not read ping for " + target.getName() + ".");
        }
    }
}
