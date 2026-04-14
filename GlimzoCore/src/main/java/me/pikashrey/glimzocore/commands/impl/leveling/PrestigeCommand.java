package me.pikashrey.glimzocore.commands.impl.leveling;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.PrestigeMenu;
import org.bukkit.entity.Player;

public class PrestigeCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public PrestigeCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;
        new PrestigeMenu(plugin, player).open();
    }
}
