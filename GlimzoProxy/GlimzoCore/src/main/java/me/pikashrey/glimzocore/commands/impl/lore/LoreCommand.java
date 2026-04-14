package me.pikashrey.glimzocore.commands.impl.lore;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.LoreMenu;
import org.bukkit.entity.Player;

public class LoreCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public LoreCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        if (plugin.getLoreManager().getChapters().isEmpty()) {
            a.send("&7No lore content has been configured yet.");
            return;
        }
        new LoreMenu(plugin, player).open();
    }
}
