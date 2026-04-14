package me.pikashrey.glimzoproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.pikashrey.glimzoproxy.GlimzoProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GlistCommand implements SimpleCommand {

    private final GlimzoProxy plugin;

    public GlistCommand(GlimzoProxy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation inv) {
        inv.source().sendMessage(Component.text(

        for (RegisteredServer server : plugin.getServer().getAllServers()) {
            String name    = server.getServerInfo().getName();
            int count      = server.getPlayersConnected().size();
            inv.source().sendMessage(
                    Component.text(" " + capitalize(name) + ": ", NamedTextColor.AQUA)
                            .append(Component.text(count + " players", NamedTextColor.WHITE))
            );
        }

        inv.source().sendMessage(
                Component.text("Total: ", NamedTextColor.AQUA)
                        .append(Component.text(
                                plugin.getServer().getPlayerCount() + " players online",
                                NamedTextColor.WHITE))
        );
    }

    @Override
    public boolean hasPermission(Invocation inv) {
        return true; // everyone can use /glist
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
