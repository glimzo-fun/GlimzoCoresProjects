package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.entity.Player;

public class FlyCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public FlyCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.fly"; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        boolean newState = !player.getAllowFlight();
        player.setAllowFlight(newState);
        if (!newState) player.setFlying(false);

        // Persist the fly state so it survives reconnects
        PlayerData data = GlobalPlayer.get(player);
        if (data != null) data.setFlyEnabled(newState);

        a.sendSuccess("Flight " + (newState ? "&aenabled" : "&cdisabled") + "&a.");
    }
}
