package me.pikashrey.glimzocore.features.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NetworkRankSync {

    private static final String CHANNEL_DEFAULT = "glimzo:rank";

    private final GlimzoCore plugin;

    /** MySQL fallback: cursor timestamp. */
    private long lastSeenTimestamp = 0L;

    /** True when Redis subscriber thread is alive and healthy. */
    private volatile boolean redisActive = false;

    private Object jedisPool;

    /** Background thread running the blocking JedisPubSub.subscribe() call. */
    private Thread subscriberThread;

    /** BukkitTask for the MySQL polling fallback - stored so stop() can cancel it. */
    private org.bukkit.scheduler.BukkitTask fallbackTask;

    public NetworkRankSync(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    /** Start the appropriate sync mode. Called once from GlimzoCore.initManagers(). */
    public void start() {
        if (tryStartRedis()) {
            plugin.log("&a[RankSync] Redis pub/sub active on channel '" + getChannel() + "'.");
        } else {
            long ticks = plugin.getConfig().getLong("ranks.sync-interval-ticks", 200L);
            fallbackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::mysqlPoll, 200L, ticks);
            plugin.log("&e[RankSync] Using MySQL polling (" + (ticks / 20) + "s interval).");
        }
    }

    /** Clean shutdown - called from GlimzoCore.onDisable(). */
    public void stop() {
        redisActive = false;
        if (subscriberThread != null) {
            subscriberThread.interrupt();
        }
        if (fallbackTask != null) {
            fallbackTask.cancel();
            fallbackTask = null;
        }
        closePool();
    }

    // Public API - called by RankManager after grant / revoke / expire

    public void publishRankChange(UUID targetUuid) {
        if (!redisActive || jedisPool == null) return;
        String serverId = plugin.getConfig().getString("server.id", "unknown");
        String payload  = targetUuid.toString() + ":" + serverId;
        try {
            withJedis(jedis -> jedis.getClass()
                    .getMethod("publish", String.class, String.class)
                    .invoke(jedis, getChannel(), payload));
        } catch (Exception e) {
            plugin.log("&c[RankSync] Publish failed: " + e.getMessage());
        }
    }

    public boolean isRedisActive() {
        return redisActive;
    }

    // Redis startup

    private boolean tryStartRedis() {
        if (!plugin.getConfig().getBoolean("redis.enabled", false)) return false;

        // Verify Jedis is present without a hard compile-time dependency
        try {
            Class.forName("redis.clients.jedis.JedisPool");
            Class.forName("redis.clients.jedis.JedisPubSub");
        } catch (ClassNotFoundException e) {
            plugin.log("&e[RankSync] Jedis not found - place jedis.jar in /plugins/ to enable Redis sync.");
            return false;
        }

        String host     = plugin.getConfig().getString("redis.host",     "127.0.0.1");
        int    port     = plugin.getConfig().getInt   ("redis.port",     6379);
        String password = plugin.getConfig().getString("redis.password", "");

        try {
            buildPool(host, port, password);
            pingPool();   // throws if connection refused / wrong password
            launchSubscriberThread();
            redisActive = true;
            return true;
        } catch (Exception e) {
            plugin.log("&c[RankSync] Redis failed (" + e.getMessage() + ") - falling back to MySQL polling.");
            closePool();
            return false;
        }
    }

    private void buildPool(String host, int port, String password) throws Exception {
        Class<?> poolClass = Class.forName("redis.clients.jedis.JedisPool");
        if (password == null || password.isEmpty()) {
            jedisPool = poolClass.getConstructor(String.class, int.class)
                    .newInstance(host, port);
        } else {
            jedisPool = poolClass.getConstructor(String.class, int.class, int.class, String.class)
                    .newInstance(host, port, 2000, password);
        }
    }

    private void pingPool() throws Exception {
        withJedis(jedis -> jedis.getClass().getMethod("ping").invoke(jedis));
    }

    private void launchSubscriberThread() {
        String channel = getChannel();

        // RankSyncSubscriber extends JedisPubSub - only safe to instantiate here
        // because we already verified JedisPubSub is on the classpath above.
        RankSyncSubscriber subscriber = new RankSyncSubscriber(this::onRedisMessage);

        subscriberThread = new Thread(() -> {
            try {
                withJedis(jedis -> jedis.getClass()
                        .getMethod("subscribe",
                                Class.forName("redis.clients.jedis.JedisPubSub"),
                                String[].class)
                        .invoke(jedis, subscriber, new String[]{channel}));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // clean shutdown
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                } else {
                    plugin.log("&c[RankSync] Redis subscriber error: "
                            + (cause != null ? cause.getMessage() : e.getMessage()));
                    fallbackToMysqlPolling();
                }
            } catch (Exception e) {
                plugin.log("&c[RankSync] Redis subscriber crashed: " + e.getMessage());
                fallbackToMysqlPolling();
            }
        }, "GlimzoCore-RankSync-Redis");

        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    // Redis message handler - runs on the subscriber thread

    void onRedisMessage(String message) {
        if (message == null) return;
        int sep = message.indexOf(':');
        if (sep < 0) return;

        String uuidStr  = message.substring(0, sep);
        String sourceId = message.substring(sep + 1);
        String thisId   = plugin.getConfig().getString("server.id", "unknown");

        if (thisId.equals(sourceId)) return; // ignore own events

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        refreshPlayer(uuid);
    }

    // MySQL polling fallback

    // Max rows consumed per poll tick - prevents long-running queries on burst
    private static final int POLL_BATCH_LIMIT = 200;
    // Clock-skew tolerance - ignore rows timestamped more than 5s in the future
    private static final long CLOCK_SKEW_MS   = 5_000L;

    private void mysqlPoll() {
        String serverId  = plugin.getConfig().getString("server.id", "unknown");
        long   ceiling   = System.currentTimeMillis() + CLOCK_SKEW_MS;
        Set<UUID> affected = new HashSet<>();

        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT target_uuid, timestamp FROM glimzo_rank_logs " +
                     "WHERE timestamp > ? AND timestamp <= ? AND server <> ? " +
                     "ORDER BY timestamp ASC LIMIT " + POLL_BATCH_LIMIT)) {
            ps.setLong(1, lastSeenTimestamp);
            ps.setLong(2, ceiling);
            ps.setString(3, serverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("target_uuid"));
                    long ts   = rs.getLong("timestamp");
                    lastSeenTimestamp = Math.max(lastSeenTimestamp, ts);
                    affected.add(uuid);
                }
            }
        } catch (Exception e) {
            plugin.log("&c[RankSync] MySQL poll failed: " + e.getMessage());
            return;
        }

        for (UUID uuid : affected) {
            refreshPlayer(uuid);
        }
    }

    private void fallbackToMysqlPolling() {
        if (!redisActive) return; // already switched
        redisActive = false;
        plugin.log("&e[RankSync] Switching to MySQL polling after Redis failure.");
        long ticks = plugin.getConfig().getLong("ranks.sync-interval-ticks", 200L);
        fallbackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::mysqlPoll, 20L, ticks);
    }

    // Shared player refresh - safe for both modes

    private void refreshPlayer(UUID uuid) {
        // Reload grants from DB - safe on this async thread
        plugin.getRankManager().loadGrants(uuid);

        // Resolve permissions and apply attachment on the main thread.
        // A single resolvePermissions() call here avoids a race where the
        // intermediate async cache-fill could be invalidated before the main
        // thread reads it.
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;
            plugin.getPermissionManager().applyPermissions(
                    player,
                    plugin.getRankManager().resolvePermissions(uuid));
        });
    }

    // Jedis helper - borrow a resource, execute, return it

    @FunctionalInterface
    private interface JedisAction {
        void run(Object jedis) throws Exception;
    }

    private void withJedis(JedisAction action) throws Exception {
        Object jedis = jedisPool.getClass().getMethod("getResource").invoke(jedisPool);
        try {
            action.run(jedis);
        } finally {
            try { jedis.getClass().getMethod("close").invoke(jedis); } catch (Exception ignored) {}
        }
    }

    private void closePool() {
        if (jedisPool == null) return;
        try { jedisPool.getClass().getMethod("close").invoke(jedisPool); } catch (Exception ignored) {}
        jedisPool = null;
    }

    private String getChannel() {
        return plugin.getConfig().getString("redis.channel", CHANNEL_DEFAULT);
    }
}
