package me.pikashrey.glimzocore.network;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BridgeSender {

    private final GlimzoCore plugin;

    public BridgeSender(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    /** Send a specific player to a BungeeCord server. */
    public void connect(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(PacketType.CONNECT.getChannel());
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /** Send all online players to a BungeeCord server. Uses ConnectOther for each. */
    public void sendToAll(String server) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            connect(p, server);
        }
    }

    /** Kick a player on another server by name. */
    public void kickPlayer(Player messenger, String targetName, String reason) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(PacketType.KICK_PLAYER.getChannel());
        out.writeUTF(targetName);
        out.writeUTF(reason);
        messenger.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /** Get the server this player is on (result delivered via BridgeListener). */
    public void getServer(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(PacketType.GET_SERVER.getChannel());
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /** Broadcast a message to all players on all servers. */
    public void broadcastToNetwork(Player messenger, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(PacketType.MESSAGE_RAW.getChannel());
        out.writeUTF("ALL");
        out.writeUTF("{\"text\":\"" + message.replace("\"", "\\\"") + "\"}");
        messenger.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}

