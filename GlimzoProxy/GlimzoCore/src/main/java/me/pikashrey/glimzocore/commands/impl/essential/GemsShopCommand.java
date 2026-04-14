package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.GemsShopMenu;
import org.bukkit.entity.Player;

public class GemsShopCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public GemsShopCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;
        new GemsShopMenu(plugin, player).open();
    }
}
