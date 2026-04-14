package me.pikashrey.glimzocore.features.season;

import me.pikashrey.glimzocore.GlimzoCore;

import java.sql.*;
import java.util.*;

public class SeasonLeaderboard {

    protected final GlimzoCore plugin;

    private volatile List<Entry> entries     = Collections.emptyList();
    private volatile long        cachedAt    = 0L;
    private static final long    TTL_MS      = 30_000L;

    public SeasonLeaderboard(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the cached leaderboard immediately (never blocks the calling thread).
     * If the cache is stale, an async refresh is scheduled; the next call will
     * return fresh data. The first call ever blocks briefly on the async task -
     * this is acceptable at startup when there are no players to lag.
     */
    public List<Entry> getEntries() {
        if (System.currentTimeMillis() - cachedAt < TTL_MS && !entries.isEmpty()) {
            return entries; // fast path - cache is fresh
        }
        // Cache is stale: schedule async refresh and return whatever we have now.
        // Commands will show slightly stale data for at most one TTL cycle, which
        // is far better than blocking a player's command thread on a DB query.
        if (org.bukkit.Bukkit.isPrimaryThread()) {
            org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                    me.pikashrey.glimzocore.GlimzoCore.getInstance(), this::refresh);
        } else {
            refresh(); // called from LeaderboardTask (already async) - refresh inline
        }
        return entries;
    }

    public void refresh() {
        List<Entry> fresh = new ArrayList<>();
        String sql = "SELECT uuid, name, season_rank, season_xp FROM glimzo_players " +
                     "ORDER BY season_xp DESC LIMIT ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, plugin.getConfig().getInt("leaderboard.top-size", 10));
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    fresh.add(new Entry(
                            rank++,
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getInt("season_rank"),
                            rs.getLong("season_xp")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[SeasonLeaderboard] Refresh failed: " + e.getMessage());
            return;
        }
        entries  = Collections.unmodifiableList(fresh);
        cachedAt = System.currentTimeMillis();
    }

    public static class Entry {
        public final int    position;
        public final UUID   uuid;
        public final String name;
        public final int    seasonRank;
        public final long   seasonXp;

        public Entry(int position, UUID uuid, String name, int seasonRank, long seasonXp) {
            this.position   = position;
            this.uuid       = uuid;
            this.name       = name;
            this.seasonRank = seasonRank;
            this.seasonXp   = seasonXp;
        }
    }
}
