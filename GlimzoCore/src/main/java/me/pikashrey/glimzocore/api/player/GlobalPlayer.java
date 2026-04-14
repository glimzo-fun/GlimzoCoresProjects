package me.pikashrey.glimzocore.api.player;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalPlayer {

    private static final Map<UUID, PlayerData> cache     = new ConcurrentHashMap<>();
    private static final Map<String, UUID>     nameIndex = new ConcurrentHashMap<>();

    public static PlayerData get(Player player) { return cache.get(player.getUniqueId()); }
    public static PlayerData get(UUID uuid)     { return cache.get(uuid); }

    public static PlayerData get(String name) {
        if (name == null) return null;
        UUID uuid = nameIndex.get(name.toLowerCase());
        return uuid != null ? cache.get(uuid) : null;
    }

    public static Collection<PlayerData> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

    public static int     size()              { return cache.size(); }
    public static boolean isLoaded(UUID uuid) { return cache.containsKey(uuid); }

    public static void load(Player player) {
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) return;

        PlayerData data = GlimzoCore.getInstance().getMysqlManager().loadPlayer(uuid, player.getName());
        data.clearDirty();

        if (!player.isOnline()) return;

        data.getStats().incrementLogins();
        data.setLastSeenTime(System.currentTimeMillis());

        if (data.isFirstJoin()) {
            data.setFirstJoinTime(System.currentTimeMillis());
            data.setFirstJoin(false);
        }

        data.markDirty();

        cache.put(uuid, data);
        nameIndex.put(player.getName().toLowerCase(), uuid);
    }

    public static void unload(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = cache.remove(uuid);
        if (data == null) return;

        nameIndex.remove(player.getName().toLowerCase());
        long now = System.currentTimeMillis();

        long lastSeen = data.getLastSeenTime();
        if (lastSeen > 0) {
            int minutesPlayed = (int) ((now - lastSeen) / 60_000L);
            if (minutesPlayed > 0) {
                data.addPlaytimeMinutes(minutesPlayed);
                data.getStats().addPlaytime(minutesPlayed);
            }
        }
        data.setLastSeenTime(now);
        GlimzoCore.getInstance().getMysqlManager().savePlayer(data);
    }

    public static int saveAll() {
        int count = 0;
        for (PlayerData data : cache.values()) {
            if (!data.isDirty()) continue;
            GlimzoCore.getInstance().getMysqlManager().savePlayer(data);
            data.clearDirty();
            count++;
        }
        return count;
    }

    public static void invalidate(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) nameIndex.remove(data.getName().toLowerCase());
    }
}
