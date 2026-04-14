package me.pikashrey.glimzocore.tasks;

import me.pikashrey.glimzocore.GlimzoCore;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PruneTask implements Runnable {

    private static final long SOCIAL_TTL_MS   =  7L * 24 * 3600 * 1000;
    private static final long RANK_LOG_TTL_MS = 30L * 24 * 3600 * 1000;

    protected final GlimzoCore plugin;

    public PruneTask(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        prune("glimzo_social_events", "created_at", now - SOCIAL_TTL_MS);
        prune("glimzo_rank_logs",     "timestamp",  now - RANK_LOG_TTL_MS);
    }

    private void prune(String table, String column, long cutoff) {
        String sql = "DELETE FROM " + table + " WHERE " + column + " < ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, cutoff);
            int deleted = ps.executeUpdate();
            if (deleted > 0) plugin.log("&7[Prune] Removed " + deleted + " old rows from " + table + ".");
        } catch (Exception e) {
            plugin.log("&c[Prune] Failed to prune " + table + ": " + e.getMessage());
        }
    }
}
