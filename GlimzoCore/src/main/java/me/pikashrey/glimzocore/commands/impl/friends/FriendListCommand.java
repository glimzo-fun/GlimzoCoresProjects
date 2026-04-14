package me.pikashrey.glimzocore.commands.impl.friends;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.entity.Player;

/** /friendlist - alias that delegates to /friend list */
public class FriendListCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public FriendListCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;
        // Re-use FriendCommand with synthetic "list" arg
        String[] listArgs = {"list"};
        new FriendCommand(plugin).execute(
                new me.pikashrey.glimzocore.commands.api.CommandArgs(
                        plugin, player, "friendlist", listArgs));
    }
}
