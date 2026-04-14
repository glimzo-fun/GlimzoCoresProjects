package me.pikashrey.glimzocore120.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.pikashrey.glimzocore120.GlimzoCore120;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BridgeListener120 implements PluginMessageListener {

    private final GlimzoCore120 plugin;
    private static final Map<UUID, Consumer<String>> serverCallbacks = new ConcurrentHashMap<>();

    public BridgeListener120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

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

        if (subChannel.equals("GetServer")) {
            String serverName = in.readUTF();
            Consumer<String> cb = serverCallbacks.remove(player.getUniqueId());
            if (cb != null) cb.accept(serverName);
        }
    }
}
