package me.pikashrey.glimzocore.features.social;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class NetworkSocialSync {

    private static final String CHANNEL_DEFAULT = "glimzo:social";

    // Event type constants - also written to glimzo_social_events for polling
    public static final String FRIEND_ADD    = "FRIEND_ADD";
    public static final String FRIEND_REMOVE = "FRIEND_REMOVE";
    public static final String CLAN_UPDATE   = "CLAN_UPDATE";
    public static final String CLAN_DISBAND  = "CLAN_DISBAND";
    public static final String PARTY_MSG     = "PARTY_MSG";
    public static final String NICK_CLEAR    = "NICK_CLEAR";

    private final GlimzoCore plugin;

    private volatile boolean redisActive = false;
    private Object jedisPool;
    private Thread subscriberThread;
    private org.bukkit.scheduler.BukkitTask fallbackTask;
    private org.bukkit.scheduler.BukkitTask reconnectTask;
    private long lastSeenTimestamp = 0L;

    public NetworkSocialSync(GlimzoCore plugin) {
        this.plugin = plugin;
    }


    public void start() {
        if (tryStartRedis()) {
            plugin.log("&a[SocialSync] Redis pub/sub active on channel '" + getChannel() + "'.");
        } else {
            long ticks = plugin.getConfig().getLong("social.sync-interval-ticks", 200L);
            fallbackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    plugin, this::mysqlPoll, 200L, ticks);
            plugin.log("&e[SocialSync] Using MySQL polling (" + (ticks / 20) + "s interval).");
        }
    }

    public void stop() {
        redisActive = false;
        if (subscriberThread != null) subscriberThread.interrupt();
        if (fallbackTask   != null) { fallbackTask.cancel();   fallbackTask   = null; }
        if (reconnectTask  != null) { reconnectTask.cancel();  reconnectTask  = null; }
        closePool();
    }


    /** Notify the network that two players became friends. */
    public void publishFriendAdd(UUID a, UUID b, String nameA, String nameB) {
        String payload = a + ":" + b + ":" + nameA + ":" + nameB;
        publish(FRIEND_ADD, payload);
    }

    /** Notify the network that a friendship was removed. */
    public void publishFriendRemove(UUID a, UUID b) {
        publish(FRIEND_REMOVE, a + ":" + b);
    }

    /** Notify the network that a clan's membership changed (join / leave / role). */
    public void publishClanUpdate(String clanId, UUID affectedPlayer) {
        publish(CLAN_UPDATE, clanId + ":" + affectedPlayer);
    }

    /** Notify the network that a clan was disbanded. */
    public void publishClanDisband(String clanId) {
        publish(CLAN_DISBAND, clanId);
    }

    /** Notify all servers to clear a player's nickname (called by /unnick). */
    public void publishNickClear(java.util.UUID uuid) {
        publish(NICK_CLEAR, uuid.toString());
    }

    /** Send a party chat message across servers. */
    public void publishPartyMessage(UUID leaderUuid, String message) {
        publish(PARTY_MSG, leaderUuid + ":" + message);
    }

    public boolean isRedisActive() { return redisActive; }


    void onRedisMessage(String raw) {
        if (raw == null || raw.isEmpty()) return;

        // Format: EVENT_TYPE:server_id:data
        String[] parts = raw.split(":", 3);
        if (parts.length < 3) return;

        String eventType = parts[0];
        String sourceId  = parts[1];
        String data      = parts[2];

        String thisId = plugin.getConfig().getString("server.id", "unknown");
        if (thisId.equals(sourceId)) return; // ignore own publishes

        handleEvent(eventType, data);
    }

    /**
     * Dispatch an incoming social event. Safe to call from any thread -
     * all Bukkit API access is marshalled to the main thread internally.
     */
    private void handleEvent(String eventType, String data) {
        switch (eventType) {

            case FRIEND_ADD: {
                // data = uuid_a:uuid_b:name_a:name_b
                String[] p = data.split(":", 4);
                if (p.length < 4) return;
                UUID a = parseUuid(p[0]); UUID b = parseUuid(p[1]);
                if (a == null || b == null) return;
                final String nameA = p[2], nameB = p[3];
                final UUID fa = a, fb = b;
                // Cache update + Bukkit notify - must run on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getFriendManager().addFriendLocal(fa, fb);
                    plugin.getFriendManager().addFriendLocal(fb, fa);
                    notifyIfOnline(fa, "&a" + nameB + " &7is now your friend!");
                    notifyIfOnline(fb, "&a" + nameA + " &7is now your friend!");
                });
                break;
            }

            case FRIEND_REMOVE: {
                // data = uuid_a:uuid_b
                String[] p = data.split(":", 2);
                if (p.length < 2) return;
                UUID a = parseUuid(p[0]); UUID b = parseUuid(p[1]);
                if (a == null || b == null) return;
                final UUID fa = a, fb = b;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getFriendManager().removeFriendLocal(fa, fb);
                    plugin.getFriendManager().removeFriendLocal(fb, fa);
                });
                break;
            }

            case CLAN_UPDATE: {
                // data = clan_id:player_uuid - reload is a DB op, keep async
                String[] p = data.split(":", 2);
                if (p.length < 2) return;
                final String clanId = p[0];
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                        plugin.getClanManager().reloadClan(clanId));
                break;
            }

            case CLAN_DISBAND: {
                // evictClan only touches the in-memory cache - safe on main thread
                final String clanId = data;
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getClanManager().evictClan(clanId));
                break;
            }

            case PARTY_MSG: {
                // data = leader_uuid:message
                int sep = data.indexOf(':');
                if (sep < 0) return;
                UUID leaderUuid = parseUuid(data.substring(0, sep));
                if (leaderUuid == null) return;
                final UUID lu = leaderUuid;
                final String message = data.substring(sep + 1);
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getPartyManager().deliverCrossServerChat(lu, message));
                break;
            }

            case NICK_CLEAR: {
                // data = player UUID - clear nick if player is online on this server
                java.util.UUID targetUuid = parseUuid(data);
                if (targetUuid == null) return;
                final java.util.UUID fu = targetUuid;
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(fu);
                    if (online != null) {
                        plugin.getNickManager().clearNick(online);
                    } else {
                        // Player offline on this server - clear stored nick in their cached data
                        me.pikashrey.glimzocore.api.player.PlayerData pd =
                                me.pikashrey.glimzocore.api.player.GlobalPlayer.get(fu);
                        if (pd != null) pd.setNick(null);
                    }
                });
                break;
            }

            default:
                break;
        }
    }


    private void mysqlPoll() {
        String serverId = plugin.getConfig().getString("server.id", "unknown");
        List<String[]> events = plugin.getMysqlManager().pollSocialEvents(lastSeenTimestamp, serverId);
        for (String[] ev : events) {
            String eventType = ev[0];
            String payload   = ev[1];
            long   ts        = Long.parseLong(ev[2]);
            lastSeenTimestamp = Math.max(lastSeenTimestamp, ts);
            handleEvent(eventType, payload);
        }
    }


    private boolean tryStartRedis() {
        if (!plugin.getConfig().getBoolean("redis.enabled", false)) return false;
        try {
            Class.forName("redis.clients.jedis.JedisPool");
            Class.forName("redis.clients.jedis.JedisPubSub");
        } catch (ClassNotFoundException e) {
            return false;
        }
        String host     = plugin.getConfig().getString("redis.host",     "127.0.0.1");
        int    port     = plugin.getConfig().getInt   ("redis.port",     6379);
        String password = plugin.getConfig().getString("redis.password", "");
        try {
            buildPool(host, port, password);
            pingPool();
            launchSubscriberThread();
            redisActive = true;
            return true;
        } catch (Exception e) {
            plugin.log("&c[SocialSync] Redis failed (" + e.getMessage() + ") - falling back to MySQL polling.");
            closePool();
            return false;
        }
    }

    private void buildPool(String host, int port, String password) throws Exception {
        Class<?> poolClass = Class.forName("redis.clients.jedis.JedisPool");
        if (password == null || password.isEmpty()) {
            jedisPool = poolClass.getConstructor(String.class, int.class).newInstance(host, port);
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
        SocialSyncSubscriber subscriber = new SocialSyncSubscriber(this::onRedisMessage);

        subscriberThread = new Thread(() -> {
            try {
                withJedis(jedis -> jedis.getClass()
                        .getMethod("subscribe",
                                Class.forName("redis.clients.jedis.JedisPubSub"),
                                String[].class)
                        .invoke(jedis, subscriber, new String[]{channel}));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                } else {
                    plugin.log("&c[SocialSync] Redis subscriber error: " +
                            (cause != null ? cause.getMessage() : e.getMessage()));
                    fallbackToMysqlPolling();
                }
            } catch (Exception e) {
                plugin.log("&c[SocialSync] Redis subscriber crashed: " + e.getMessage());
                fallbackToMysqlPolling();
            }
        }, "GlimzoCore-SocialSync-Redis");

        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void fallbackToMysqlPolling() {
        if (!redisActive) return;
        redisActive = false;
        plugin.log("&e[SocialSync] Switching to MySQL polling after Redis failure.");
        long ticks = plugin.getConfig().getLong("social.sync-interval-ticks", 200L);
        fallbackTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::mysqlPoll, 20L, ticks);
        // Schedule periodic reconnect attempts every 60 seconds
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (reconnectTask != null) return; // already scheduled
        reconnectTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (redisActive) {
                // Already reconnected - cancel this task
                if (reconnectTask != null) { reconnectTask.cancel(); reconnectTask = null; }
                return;
            }
            plugin.log("&7[SocialSync] Attempting Redis reconnection...");
            try {
                closePool();
                if (tryStartRedis()) {
                    // Redis is back - cancel MySQL fallback
                    if (fallbackTask != null) { fallbackTask.cancel(); fallbackTask = null; }
                    if (reconnectTask != null) { reconnectTask.cancel(); reconnectTask = null; }
                    plugin.log("&a[SocialSync] Redis reconnected - switched back from MySQL polling.");
                }
            } catch (Exception e) {
                plugin.log("&7[SocialSync] Redis reconnect attempt failed: " + e.getMessage());
            }
        }, 1200L, 1200L); // every 60 seconds
    }


    private void publish(String eventType, String data) {
        String serverId = plugin.getConfig().getString("server.id", "unknown");
        String payload  = eventType + ":" + serverId + ":" + data;

        // Always log to DB for MySQL polling fallback on other servers
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getMysqlManager().logSocialEvent(eventType, payload, serverId));

        if (!redisActive || jedisPool == null) return;
        try {
            final String ch = getChannel();
            withJedis(jedis -> jedis.getClass()
                    .getMethod("publish", String.class, String.class)
                    .invoke(jedis, ch, payload));
        } catch (Exception e) {
            plugin.log("&c[SocialSync] Publish failed: " + e.getMessage());
        }
    }


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
        return plugin.getConfig().getString("redis.social-channel", CHANNEL_DEFAULT);
    }

    private UUID parseUuid(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    private void notifyIfOnline(UUID uuid, String message) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate(message));
    }
}
