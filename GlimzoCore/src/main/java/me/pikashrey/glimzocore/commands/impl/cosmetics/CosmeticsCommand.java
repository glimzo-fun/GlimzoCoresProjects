package me.pikashrey.glimzocore.commands.impl.cosmetics;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.CosmeticsMenu;
import org.bukkit.entity.Player;

public class CosmeticsCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public CosmeticsCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        new CosmeticsMenu(plugin, player).open();
    }
}
