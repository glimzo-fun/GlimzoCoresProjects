package me.pikashrey.glimzocore.commands.impl.essential;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
public class WhereAmICommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public WhereAmICommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        Location loc = player.getLocation();
        a.send("&7World: &f" + loc.getWorld().getName()
                + " &7X: &f" + loc.getBlockX()
                + " &7Y: &f" + loc.getBlockY()
                + " &7Z: &f" + loc.getBlockZ());
    }
}
