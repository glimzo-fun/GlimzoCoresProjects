package me.pikashrey.glimzocore.features.clan;

import me.pikashrey.glimzocore.GlimzoCore;

import java.sql.*;
import java.util.*;

public class ClanLeaderboard {

    protected final GlimzoCore plugin;

    private volatile List<Entry> entries     = Collections.emptyList();
    private volatile long        cachedAt    = 0L;
    private static final long    TTL_MS      = 30_000L;

    public ClanLeaderboard(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public List<Entry> getEntries() {
        if (System.currentTimeMillis() - cachedAt < TTL_MS && !entries.isEmpty()) {
            return entries;
        }
        if (org.bukkit.Bukkit.isPrimaryThread()) {
            org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                    me.pikashrey.glimzocore.GlimzoCore.getInstance(), this::refresh);
        } else {
            refresh();
        }
        return entries;
    }

    public void refresh() {
        List<Entry> fresh = new ArrayList<>();
        String sql = "SELECT id, name, tag, level, xp, leader_name FROM glimzo_clans ORDER BY xp DESC LIMIT ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, plugin.getConfig().getInt("leaderboard.top-size", 10));
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    fresh.add(new Entry(
                            rank++,
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("tag"),
                            rs.getInt("level"),
                            rs.getLong("xp"),
                            rs.getString("leader_name")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[ClanLeaderboard] Refresh failed: " + e.getMessage());
            return;
        }
        entries  = Collections.unmodifiableList(fresh);
        cachedAt = System.currentTimeMillis();
    }

    public static class Entry {
        public final int    rank;
        public final String clanId;
        public final String name;
        public final String tag;
        public final int    level;
        public final long   xp;
        public final String leaderName;

        public Entry(int rank, String clanId, String name, String tag,
                     int level, long xp, String leaderName) {
            this.rank       = rank;
            this.clanId     = clanId;
            this.name       = name;
            this.tag        = tag;
            this.level      = level;
            this.xp         = xp;
            this.leaderName = leaderName;
        }
    }
}
