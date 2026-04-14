package me.pikashrey.glimzocore.managers;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DatabaseManager {

    protected final GlimzoCore plugin;

    private BukkitTask healthTask;
    private volatile int  consecutiveFailures = 0;
    private volatile long lastSuccessfulPing  = System.currentTimeMillis();

    private static final int  MAX_FAILURES    = 3;
    private static final long PING_TICKS      = 1200L;

    public DatabaseManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        healthTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::ping, PING_TICKS, PING_TICKS);
    }

    public void stop() {
        if (healthTask != null) {
            healthTask.cancel();
            healthTask = null;
        }
    }

    public int getActiveConnections() {
        try {
            com.zaxxer.hikari.HikariDataSource ds = getDataSource();
            return ds != null ? ds.getHikariPoolMXBean().getActiveConnections() : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    public int getTotalConnections() {
        try {
            com.zaxxer.hikari.HikariDataSource ds = getDataSource();
            return ds != null ? ds.getHikariPoolMXBean().getTotalConnections() : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    public long getLastSuccessfulPingMs() {
        return System.currentTimeMillis() - lastSuccessfulPing;
    }

    private void ping() {
        if (!plugin.getMysqlManager().isConnected()) {
            onFailure("DataSource is closed");
            return;
        }
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1")) {
            ps.executeQuery();
            lastSuccessfulPing  = System.currentTimeMillis();
            consecutiveFailures = 0;
        } catch (Exception e) {
            onFailure(e.getMessage());
        }
    }

    private void onFailure(String reason) {
        consecutiveFailures++;
        plugin.log("&e[Database] Ping failed (" + consecutiveFailures + "/" + MAX_FAILURES + "): " + reason);
        if (consecutiveFailures >= MAX_FAILURES) {
            plugin.log("&c[Database] " + MAX_FAILURES + " consecutive failures - check your MySQL server.");
            consecutiveFailures = 0;
        }
    }

    @SuppressWarnings("unchecked")
    private com.zaxxer.hikari.HikariDataSource getDataSource() {
        try {
            java.lang.reflect.Field f = plugin.getMysqlManager().getClass().getDeclaredField("dataSource");
            f.setAccessible(true);
            return (com.zaxxer.hikari.HikariDataSource) f.get(plugin.getMysqlManager());
        } catch (Exception e) {
            return null;
        }
    }
}
