package me.pikashrey.glimzocore.commands.impl.stats;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.StatsMenu;
import org.bukkit.entity.Player;

public class StatsCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public StatsCommand(GlimzoCore p) { this.plugin = p; }

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
        new StatsMenu(plugin, player).open();
    }
}
