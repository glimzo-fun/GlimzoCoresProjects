package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public SetSpawnCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.admin.setspawn"; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;
        plugin.getLobbyManager().setSpawn(player.getLocation());
        a.sendSuccess("Spawn set to your current location.");
    }
}
