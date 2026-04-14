package me.pikashrey.glimzoproxy.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.pikashrey.glimzoproxy.GlimzoProxy;
import me.pikashrey.glimzoproxy.managers.ServerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class ProxyListener {

    private final GlimzoProxy plugin;

    public ProxyListener(GlimzoProxy plugin) {
        this.plugin = plugin;
    }

    /** Send every new player to the lobby on first join. */
    @Subscribe
    public void onChooseServer(PlayerChooseInitialServerEvent e) {
        Optional<RegisteredServer> lobby = plugin.getServer().getServer(ServerManager.LOBBY);
        lobby.ifPresent(e::setInitialServer);
    }

    @Subscribe
    public void onLogin(LoginEvent e) {
        plugin.getLogger().info("{} connected to the network.", e.getPlayer().getUsername());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        plugin.getLogger().info("{} disconnected from the network.", e.getPlayer().getUsername());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent e) {
        String serverName = e.getServer().getServerInfo().getName();
        plugin.getLogger().info("{} joined server: {}", e.getPlayer().getUsername(), serverName);
    }

    /**
     * If a player gets kicked from a gamemode server, send them back to lobby
     * instead of disconnecting them from the network entirely.
     */
    @Subscribe(order = PostOrder.LAST)
    public void onKickedFromServer(KickedFromServerEvent e) {
        // Don't redirect if they were kicked from lobby - let them disconnect
        if (e.getServer().getServerInfo().getName().equals(ServerManager.LOBBY)) return;

        Optional<RegisteredServer> lobby = plugin.getServer().getServer(ServerManager.LOBBY);
        if (lobby.isEmpty()) return;

        e.setResult(KickedFromServerEvent.RedirectPlayer.create(
                lobby.get(),
                Component.text("You were kicked from " +
                        e.getServer().getServerInfo().getName() +
                        ". Returning to lobby.", NamedTextColor.RED)
        ));
    }
}
