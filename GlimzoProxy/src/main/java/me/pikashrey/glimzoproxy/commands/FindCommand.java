package me.pikashrey.glimzoproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import me.pikashrey.glimzoproxy.GlimzoProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class FindCommand implements SimpleCommand {

    private final GlimzoProxy plugin;

    public FindCommand(GlimzoProxy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation inv) {
        if (inv.arguments().length < 1) {
            inv.source().sendMessage(Component.text(
                    "Usage: /find <player>", NamedTextColor.RED));
            return;
        }

        String playerName = inv.arguments()[0];
        Optional<String> server = plugin.getServerManager().findPlayer(playerName);

        if (server.isEmpty()) {
            inv.source().sendMessage(Component.text(
                    playerName + " is not online.", NamedTextColor.RED));
            return;
        }

        inv.source().sendMessage(
                Component.text(playerName + " is on ", NamedTextColor.YELLOW)
                        .append(Component.text(server.get(), NamedTextColor.AQUA))
        );
    }

    @Override
    public boolean hasPermission(Invocation inv) {
        return inv.source().hasPermission("glimzo.proxy.find");
    }
}
