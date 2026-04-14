package me.pikashrey.glimzocore.commands.impl.essential.staff;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
public class InvseeCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public InvseeCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return "glimzo.staff.invsee"; }
    @Override public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        if (!a.has(0)) { a.usage("/invsee <player>"); return; }
        Player target = Bukkit.getPlayer(a.get(0));
        if (target == null) { a.sendError("Player not online."); return; }
        player.openInventory(target.getInventory());
    }
}
