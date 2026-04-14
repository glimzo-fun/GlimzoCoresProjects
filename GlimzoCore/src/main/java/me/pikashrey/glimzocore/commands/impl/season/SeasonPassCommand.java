package me.pikashrey.glimzocore.commands.impl.season;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.SeasonPassMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SeasonPassCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public SeasonPassCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;

        if (a.has(0) && a.get(0).equalsIgnoreCase("activate")
                && player.hasPermission("glimzo.staff.mode")) {
            // Admin activate for another player
            String target = a.has(1) ? a.get(1) : player.getName();
            Player t = Bukkit.getPlayer(target);
            if (t == null) { a.sendError("Player not online."); return; }
            plugin.getSeasonPassManager().activatePass(t.getUniqueId());
            a.sendSuccess("Season pass activated for &f" + t.getName() + "&a.");
            return;
        }

        // Regular player - open season pass menu
        new SeasonPassMenu(plugin, player).open();
    }
}
