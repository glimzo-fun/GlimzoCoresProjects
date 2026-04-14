package me.pikashrey.glimzocore.commands.impl.quests;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.QuestsMenu;
import org.bukkit.entity.Player;

public class QuestsCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public QuestsCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        new QuestsMenu(plugin, player).open();
    }
}
