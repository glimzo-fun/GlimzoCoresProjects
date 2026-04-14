package me.pikashrey.glimzoproxy.managers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

public class ServerManager {

    private final ProxyServer proxy;

    // Server name constants, must match velocity.toml [servers] section
    public static final String LOBBY      = "lobby";
    public static final String BEDWARS    = "bedwars";
    public static final String WOOLWARS   = "woolwars";
    public static final String RAMPAGE    = "rampage";
    public static final String BOATRACING = "boatracing";

    public ServerManager(ProxyServer proxy) {
        this.proxy = proxy;
    }

    /** Send a player to a server by name. Returns false if server not found. */
    public boolean sendTo(Player player, String serverName) {
        Optional<RegisteredServer> server = proxy.getServer(serverName);
        if (server.isEmpty()) return false;
        player.createConnectionRequest(server.get()).fireAndForget();
        return true;
    }

    /** Get total online player count across all servers. */
    public int getTotalOnline() {
        return proxy.getPlayerCount();
    }

    /** Get player count on a specific server. */
    public int getServerCount(String serverName) {
        return proxy.getServer(serverName)
                .map(s -> s.getPlayersConnected().size())
                .orElse(0);
    }

    /** Find which server a player is on. Returns empty if not found or not connected to a server. */
    public Optional<String> findPlayer(String playerName) {
        return proxy.getPlayer(playerName)
                .flatMap(p -> p.getCurrentServer()
                        .map(s -> s.getServerInfo().getName()));
    }
}
