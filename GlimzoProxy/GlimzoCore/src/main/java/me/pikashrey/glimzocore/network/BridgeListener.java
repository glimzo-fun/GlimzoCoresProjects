package me.pikashrey.glimzocore.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class BridgeListener implements PluginMessageListener {

    // Pending callbacks for GetServer responses, keyed by player UUID
    private static final Map<UUID, Consumer<String>> serverCallbacks = new ConcurrentHashMap<>();

    /**
     * Register a one-shot callback for when a GetServer response arrives for this player.
     */
    public static void onServerResponse(UUID playerUuid, Consumer<String> callback) {
        serverCallbacks.put(playerUuid, callback);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel;
        try {
            subChannel = in.readUTF();
        } catch (Exception e) {
            return;
        }

        switch (subChannel) {
            case "GetServer": {
                String serverName = in.readUTF();
                Consumer<String> cb = serverCallbacks.remove(player.getUniqueId());
                if (cb != null) cb.accept(serverName);
                break;
            }
            case "PlayerCount": {
                // in.readUTF() = server name, in.readInt() = count
                // Currently unused - extend as needed
                break;
            }
            case "PlayerList": {
                // in.readUTF() = server name, in.readUTF() = comma-separated player names
                // Currently unused - extend as needed
                break;
            }
            default:
                break;
        }
    }
}

