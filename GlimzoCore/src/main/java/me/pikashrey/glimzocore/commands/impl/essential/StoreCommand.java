package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.StoreMenu;
import org.bukkit.entity.Player;

public class StoreCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public StoreCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) { a.send("&cOnly players can use this command."); return; }
        new StoreMenu(plugin, player).open();
    }
}
