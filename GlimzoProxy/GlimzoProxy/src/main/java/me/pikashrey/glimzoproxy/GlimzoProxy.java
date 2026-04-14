package me.pikashrey.glimzoproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.pikashrey.glimzoproxy.commands.FindCommand;
import me.pikashrey.glimzoproxy.commands.GlistCommand;
import me.pikashrey.glimzoproxy.commands.SendCommand;
import me.pikashrey.glimzoproxy.listeners.ProxyListener;
import me.pikashrey.glimzoproxy.managers.ServerManager;
import org.slf4j.Logger;

@Plugin(
        id = "glimzoproxy",
        name = "GlimzoProxy",
        version = "1.0.0",
        authors = {"pikashrey"},
        description = "Glimzo Network Proxy Plugin"
)
public class GlimzoProxy {

    private static GlimzoProxy instance;

    private final ProxyServer server;
    private final Logger logger;
    private ServerManager serverManager;

    @Inject
    public GlimzoProxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        instance = this;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        serverManager = new ServerManager(server);

        // Register listeners
        server.getEventManager().register(this, new ProxyListener(this));

        // Register commands
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("glist").build(),
                new GlistCommand(this)
        );
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("send").build(),
                new SendCommand(this)
        );
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("find").build(),
                new FindCommand(this)
        );

        logger.info("GlimzoProxy enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        logger.info("GlimzoProxy disabled.");
    }

    public static GlimzoProxy getInstance() { return instance; }
    public ProxyServer getServer()          { return server; }
    public Logger getLogger()               { return logger; }
    public ServerManager getServerManager() { return serverManager; }
}
