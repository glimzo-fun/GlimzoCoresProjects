package me.pikashrey.glimzocore.commands.impl.essential.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.entity.Player;

public class VanishCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public VanishCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.staff.vanish"; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        plugin.getStaffManager().toggleVanish(player);
    }
}
