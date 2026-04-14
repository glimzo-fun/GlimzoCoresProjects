package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.leveling.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Awards XP to players from in-game activities:
 *   - Login XP (first join of session)
 *   - Chat message XP (with per-player cooldown)
 *   - Passive online-time XP (via repeating task per player)
 *
 * All values are read from leveling.yml under xp-sources.
 */
public class LevelingListener implements Listener {

    private final GlimzoCore plugin;

    // Per-player: next time they're allowed to earn chat XP (epoch ms)
    private final Map<UUID, Long> chatXpCooldown = new ConcurrentHashMap<>();
    // Per-player online-time task IDs
    private final Map<UUID, Integer> onlineTimeTasks = new ConcurrentHashMap<>();

    public LevelingListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }


    private FileConfiguration cfg() {
        return plugin.getConfigManager().getLeveling();
    }

    private long src(String key, long def) {
        return cfg().getLong("xp-sources." + key, def);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID   uuid   = player.getUniqueId();

        long loginXp = src("login", 50L);

        // Delay 2 ticks so PlayerData is guaranteed loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (loginXp > 0) plugin.getLevelManager().addXp(uuid, loginXp);

            // Start online-time XP task
            startOnlineTask(player);
        }, 2L);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID   uuid   = player.getUniqueId();

        long chatXp = src("chat-message", 5L);
        if (chatXp <= 0) return;

        long cooldownMs = src("chat-cooldown-seconds", 30L) * 1000L;
        long now = System.currentTimeMillis();

        Long next = chatXpCooldown.get(uuid);
        if (next != null && now < next) return; // still on cooldown

        chatXpCooldown.put(uuid, now + cooldownMs);

        // addXp must run on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) plugin.getLevelManager().addXp(uuid, chatXp);
        });
    }


    private void startOnlineTask(Player player) {
        UUID uuid = player.getUniqueId();
        stopOnlineTask(uuid); // cancel any previous task

        long xpPer = src("time-online", 10L);
        long intervalSec = src("time-online-interval-seconds", 60L);
        if (xpPer <= 0 || intervalSec <= 0) return;

        long intervalTicks = intervalSec * 20L;
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) { stopOnlineTask(uuid); return; }
            PlayerData data = GlobalPlayer.get(uuid);
            if (data == null) return;
            plugin.getLevelManager().addXp(uuid, xpPer);
        }, intervalTicks, intervalTicks).getTaskId();

        onlineTimeTasks.put(uuid, taskId);
    }

    public void onQuit(UUID uuid) {
        stopOnlineTask(uuid);
        chatXpCooldown.remove(uuid);
    }

    private void stopOnlineTask(UUID uuid) {
        Integer taskId = onlineTimeTasks.remove(uuid);
        if (taskId != null) Bukkit.getScheduler().cancelTask(taskId);
    }
}
