package me.pikashrey.glimzocore.commands.impl.profile;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.ProfileMenu;
import org.bukkit.entity.Player;

/**
 * /profile [player]
 *
 * No aliases - /p is reserved for /party.
 */
public class ProfileCommand implements GlimzoCommand {

    private final GlimzoCore plugin;

    public ProfileCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        PlayerData target = a.has(0)
                ? GlobalPlayer.get(a.get(0))
                : GlobalPlayer.get(player);

        if (target == null) { a.sendError("Player not found or not online."); return; }
        new ProfileMenu(plugin, player, target).open();
    }
}
