package me.pikashrey.glimzocore.utilities.general;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.network.BridgeSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Server-level helpers: connect to servers, TPS reading, online count.
 */
public final class ServerUtils {

    private ServerUtils() {}

    /**
     * Send a player to a BungeeCord server.
     */
    public static void sendToServer(Player player, String server) {
        new BridgeSender(GlimzoCore.getInstance()).connect(player, server);
    }

    public static double getTps() {
        try {
            Object minecraftServer = Bukkit.getServer().getClass()
                    .getMethod("getServer").invoke(Bukkit.getServer());
            double[] tpsArray = (double[]) minecraftServer.getClass()
                    .getField("recentTps").get(minecraftServer);
            return Math.min(20.0, tpsArray[0]);
        } catch (Exception e) {
            return 20.0;
        }
    }

    /** Whether the server is running at acceptable TPS (≥ 18.0). */
    public static boolean isLagging() {
        return getTps() < 18.0;
    }

    public static int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    public static int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }
}

