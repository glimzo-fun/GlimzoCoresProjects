package me.pikashrey.glimzoproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.pikashrey.glimzoproxy.GlimzoProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class SendCommand implements SimpleCommand {

    private final GlimzoProxy plugin;

    public SendCommand(GlimzoProxy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation inv) {
        if (inv.arguments().length < 2) {
            inv.source().sendMessage(Component.text(
                    "Usage: /send <player> <server>", NamedTextColor.RED));
            return;
        }

        String playerName = inv.arguments()[0];
        String serverName = inv.arguments()[1];

        Optional<Player> target = plugin.getServer().getPlayer(playerName);
        if (target.isEmpty()) {
            inv.source().sendMessage(Component.text(
                    "Player " + playerName + " is not online.", NamedTextColor.RED));
            return;
        }

        boolean sent = plugin.getServerManager().sendTo(target.get(), serverName);
        if (!sent) {
            inv.source().sendMessage(Component.text(
                    "Server " + serverName + " not found.", NamedTextColor.RED));
            return;
        }

        inv.source().sendMessage(Component.text(
                "Sent " + playerName + " to " + serverName + ".", NamedTextColor.GREEN));
        target.get().sendMessage(Component.text(
                "You have been sent to " + serverName + ".", NamedTextColor.YELLOW));
    }

    @Override
    public boolean hasPermission(Invocation inv) {
        return inv.source().hasPermission("glimzo.proxy.send");
    }
}
