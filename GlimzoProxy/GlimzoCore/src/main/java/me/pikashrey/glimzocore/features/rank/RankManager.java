package me.pikashrey.glimzocore.features.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.api.rank.grant.Grant;
import me.pikashrey.glimzocore.api.events.impl.PlayerGrantEvent;
import me.pikashrey.glimzocore.api.events.impl.PlayerRankChangeEvent;
import me.pikashrey.glimzocore.data.grant.GrantProcedure;
import me.pikashrey.glimzocore.features.permission.PermissionCache;
import me.pikashrey.glimzocore.features.permission.PermissionManager;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RankManager {

    // In-progress grant procedures keyed by staff UUID
    private final Map<UUID, GrantProcedure> activeProcedures = new ConcurrentHashMap<>();

    // Per-player grant lists loaded from DB on join
    private final Map<UUID, List<Grant>> grantCache = new ConcurrentHashMap<>();

    private final GlimzoCore plugin;

    private final PermissionCache permissionCache;
    private final PermissionManager permissionManager;

    public RankManager(GlimzoCore plugin, PermissionCache permissionCache) {
        this.plugin = plugin;
        this.permissionCache = permissionCache;
        this.permissionManager = GlimzoCore.getInstance().getPermissionManager();
    }

    // Player lifecycle

    /** Load all grants for a player. Call async. */
    public void loadGrants(UUID uuid) {
        List<Grant> grants = new CopyOnWriteArrayList<>(fetchGrantsFromDb(uuid));
        grantCache.put(uuid, grants);
        permissionCache.invalidatePlayer(uuid);
    }

    /** Load grants from pre-fetched batched join data - no extra DB call. */
    public void loadGrantsFromData(UUID uuid, java.util.List<java.util.Map<String,Object>> rows) {
        List<Grant> grants = new CopyOnWriteArrayList<>();
        for (java.util.Map<String,Object> row : rows) {
            String rankId = (String) row.get("rank_id");
            me.pikashrey.glimzocore.api.rank.RankRef rankRef = me.pikashrey.glimzocore.api.rank.RankRef.of(rankId);
            if (!rankRef.isValid()) continue;
            String issuedByStr = (String) row.get("issued_by_uuid");
            grants.add(new Grant(
                    (int) row.get("id"),
                    uuid,
                    (String) row.get("target_name"),
                    issuedByStr != null ? java.util.UUID.fromString(issuedByStr) : null,
                    (String) row.get("issued_by_name"),
                    rankRef,
                    (long) row.get("issued_at"),
                    (long) row.get("expires_at"),
                    (boolean) row.get("staff_grant"),
                    (boolean) row.get("active")
            ));
        }
        grantCache.put(uuid, grants);
        permissionCache.invalidatePlayer(uuid);
    }

    /** Remove a player's grants from the in-memory cache. Call on quit. */
    public void unloadGrants(UUID uuid) {
        grantCache.remove(uuid);
        activeProcedures.remove(uuid);
        permissionCache.invalidatePlayer(uuid);
        // Evict cooldown state to prevent unbounded map growth
        GlimzoCore.getInstance().getRankCooldownManager().evict(uuid);
    }

    // Grant resolution

    public Rank getActiveRank(UUID uuid) {
        RankRef ref = getActiveRankRef(uuid);
        Rank enumRank = ref.toEnum();
        return enumRank != null ? enumRank : Rank.getDefault();
    }

    public RankRef getActiveRankRef(UUID uuid) {
        List<Grant> grants = grantCache.get(uuid);
        if (grants == null || grants.isEmpty())
            return RankRef.of(Rank.getDefault());

        Grant best = null;
        for (Grant g : grants) {
            if (!g.isActive()) continue;
            if (best == null || g.getEffectiveWeight() > best.getEffectiveWeight()) {
                best = g;
            }
        }
        return best != null ? best.getRankRef()
                            : RankRef.of(Rank.getDefault());
    }

    /** Convenience overload for online players. */
    public Rank getActiveRank(Player player) {
        return getActiveRank(player.getUniqueId());
    }

    /** Convenience overload - returns RankRef for online players. */
    public RankRef getActiveRankRef(Player player) {
        return getActiveRankRef(player.getUniqueId());
    }

    public String getChatPrefix(UUID uuid) {
        me.pikashrey.glimzocore.api.player.PlayerData data =
                me.pikashrey.glimzocore.api.player.GlobalPlayer.get(uuid);

        // Fast path: return cached prefix if available
        if (data != null) {
            String cached = data.getCachedChatPrefix();
            if (cached != null) return cached;
        }

        // Compute and cache
        String prefix;
        if (data != null && data.hasCustomPrefix()) {
            prefix = me.pikashrey.glimzocore.utilities.chat.CC.translate(data.getCustomPrefix());
        } else {
            prefix = me.pikashrey.glimzocore.utilities.chat.CC.translate(
                    getActiveRankRef(uuid).getChatPrefix());
        }

        if (data != null) data.setCachedChatPrefix(prefix);
        return prefix;
    }

    /** Returns all currently active (non-expired, non-revoked) grants for a player. */
    public List<Grant> getActiveGrants(UUID uuid) {
        List<Grant> all = grantCache.getOrDefault(uuid, Collections.emptyList());
        List<Grant> active = new ArrayList<>();
        for (Grant g : all) {
            if (g.isActive()) {
                active.add(g);
            } else if (g.hasJustExpired()) {
                // Grant crossed its expiry boundary - persist and audit log exactly once
                markGrantExpired(g);
            }
        }
        return active;
    }

    /**
     * Resolve and cache the full permission set for a player from their active grants.
     */
    public Set<String> resolvePermissions(UUID uuid) {
        Set<String> cached = permissionCache.getPlayer(uuid);
        if (cached != null) return cached;

        List<Grant> grants = getActiveGrants(uuid);
        Set<String> result = new HashSet<>();

        for (Grant grant : grants) {
            String id = grant.getRankRef().getId();
            Set<String> rankPerms = permissionCache.getRank(id);
            if (rankPerms != null) {
                result.addAll(rankPerms);
            }
        }

        permissionCache.cachePlayer(uuid, result);
        return result;
    }

    /** Returns every grant ever issued to a player (including expired/revoked). */
    public List<Grant> getAllGrants(UUID uuid) {
        return Collections.unmodifiableList(
                grantCache.getOrDefault(uuid, Collections.emptyList()));
    }

    // Grant management

    public boolean addGrant(Grant grant) {
        UUID uuid = grant.getTargetUuid();

        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null) {
            // Player offline - persist directly without events or permission sync
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                saveGrantToDb(grant);
            });
            return true;
        }

        // Capture rank before change for event
        RankRef rankBefore = getActiveRankRef(uuid);

        // Fire cancellable grant event
        PlayerGrantEvent grantEvent = new PlayerGrantEvent(
                data, grant.getRankRef().getId(), grant.getIssuedByName(), grant.getExpiresAt());
        Bukkit.getPluginManager().callEvent(grantEvent);
        if (grantEvent.isCancelled()) return false;

        // Add to cache
        grantCache.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(grant);

        // Persist async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveGrantToDb(grant));

        permissionCache.invalidatePlayer(uuid);

        // Apply permission and fire rank-change event if rank actually changed
        RankRef rankAfter = getActiveRankRef(uuid);
        applyPermissions(Bukkit.getPlayer(uuid));

        if (!rankAfter.equals(rankBefore)) {
            Bukkit.getPluginManager().callEvent(
                    new PlayerRankChangeEvent(data, rankBefore, rankAfter, grant));
        }

        return true;
    }

    public void revokeGrant(UUID targetUuid, int grantId) {
        List<Grant> grants = grantCache.get(targetUuid);

        RankRef rankBefore = getActiveRankRef(targetUuid);

        if (grants != null) {
            for (Grant g : grants) {
                if (g.getId() == grantId) {
                    g.setActive(false);
                    break;
                }
            }
        }

        // Persist async + notify other servers
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            deactivateGrantInDb(grantId);
            plugin.getNetworkRankSync().publishRankChange(targetUuid);
        });

        // Fire rank-change event if needed
        Player online = Bukkit.getPlayer(targetUuid);
        if (online != null) {
            permissionCache.invalidatePlayer(targetUuid);
            RankRef rankAfter = getActiveRankRef(targetUuid);
            applyPermissions(online);

            if (!rankAfter.equals(rankBefore)) {
                PlayerData data = GlobalPlayer.get(targetUuid);
                if (data != null) {
                    Bukkit.getPluginManager().callEvent(
                            new PlayerRankChangeEvent(data, rankBefore, rankAfter, null));
                }
            }
        }

        // Audit log for revoke
        try {
            plugin.getMysqlManager().logRankAction(
                    null,
                    targetUuid,
                    rankBefore.getId(),
                    "REVOKE",
                    System.currentTimeMillis(),
                    plugin.getConfig().getString("server.id", "unknown")
            );
        } catch (Exception ignored) {
        }
    }

    // Permission sync

    private void applyPermissions(Player player) {
        if (player == null) return;
        Set<String> perms = resolvePermissions(player.getUniqueId());
        permissionManager.applyPermissions(player, perms);
    }

    public String getConfiguredPrefix(Rank rank) {
        if (rank == null) return Rank.getDefault().getChatPrefix();
        me.pikashrey.glimzocore.features.rank.RankLoader loader =
                GlimzoCore.getInstance().getRankLoader();
        if (loader != null) {
            ConfiguredRank cfg = loader.get(rank.getId());
            if (cfg != null) {
                return cfg.getPrefix();
            }
        }
        return rank.getChatPrefix();
    }

    // Grant procedures (staff /rank grant flow)

    public void startProcedure(GrantProcedure procedure) {
        activeProcedures.put(procedure.getStaffUuid(), procedure);
    }

    public GrantProcedure getProcedure(UUID staffUuid) {
        GrantProcedure p = activeProcedures.get(staffUuid);
        if (p != null && p.isExpired()) {
            activeProcedures.remove(staffUuid);
            return null;
        }
        return p;
    }

    public void clearProcedure(UUID staffUuid) {
        activeProcedures.remove(staffUuid);
    }

    public boolean hasProcedure(UUID staffUuid) {
        return getProcedure(staffUuid) != null;
    }

    // Database

    private List<Grant> fetchGrantsFromDb(UUID uuid) {
        List<Grant> grants = new ArrayList<>();
        String sql = "SELECT * FROM glimzo_grants WHERE target_uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rankId = rs.getString("rank_id");
                    RankRef rankRef =
                            RankRef.of(rankId);
                    if (!rankRef.isValid()) {
                        plugin.log("&e[RankManager] Skipping grant with unknown rank id '" + rankId + "'");
                        continue;
                    }

                    String issuedByStr = rs.getString("issued_by_uuid");
                    UUID issuedByUuid = issuedByStr != null ? UUID.fromString(issuedByStr) : null;

                    grants.add(new Grant(
                            rs.getInt("id"),
                            uuid,
                            rs.getString("target_name"),
                            issuedByUuid,
                            rs.getString("issued_by_name"),
                            rankRef,
                            rs.getLong("issued_at"),
                            rs.getLong("expires_at"),
                            rs.getBoolean("staff_grant"),
                            rs.getBoolean("active")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[RankManager] Failed to load grants for " + uuid + ": " + e.getMessage());
        }
        return grants;
    }

    private void saveGrantToDb(Grant grant) {
        String sql =
                "INSERT INTO glimzo_grants " +
                "(target_uuid, target_name, issued_by_uuid, issued_by_name, " +
                " rank_id, issued_at, expires_at, staff_grant, active) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, grant.getTargetUuid().toString());
            ps.setString(2, grant.getTargetName());
            ps.setString(3, grant.getIssuedByUuid() != null ? grant.getIssuedByUuid().toString() : null);
            ps.setString(4, grant.getIssuedByName());
            ps.setString(5, grant.getRankRef().getId());
            ps.setLong(6, grant.getIssuedAt());
            ps.setLong(7, grant.getExpiresAt());
            ps.setBoolean(8, grant.isStaffGrant());
            ps.setBoolean(9, grant.isActive());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[RankManager] Failed to save grant: " + e.getMessage());
        }

        // Audit log + cross-server sync
        try {
            plugin.getMysqlManager().logRankAction(
                    grant.getIssuedByUuid(),
                    grant.getTargetUuid(),
                    grant.getRankRef().getId(),
                    "GRANT",
                    System.currentTimeMillis(),
                    plugin.getConfig().getString("server.id", "unknown")
            );
        } catch (Exception ignored) {
        }
        // Notify other servers via Redis (no-op if Redis not configured)
        plugin.getNetworkRankSync().publishRankChange(grant.getTargetUuid());
    }

    private void deactivateGrantInDb(int grantId) {
        String sql = "UPDATE glimzo_grants SET active = 0 WHERE id = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, grantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[RankManager] Failed to deactivate grant #" + grantId + ": " + e.getMessage());
        }
    }

    private void markGrantExpired(Grant grant) {
        if (grant.getId() <= 0) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            deactivateGrantInDb(grant.getId());
            try {
                plugin.getMysqlManager().logRankAction(
                        null,
                        grant.getTargetUuid(),
                        grant.getRankRef().getId(),
                        "EXPIRE",
                        System.currentTimeMillis(),
                        plugin.getConfig().getString("server.id", "unknown")
                );
            } catch (Exception ignored) {
            }
            // Notify other servers via Redis
            plugin.getNetworkRankSync().publishRankChange(grant.getTargetUuid());
        });
    }

    // Utilities

    public boolean hasGrantsLoaded(UUID uuid) {
        return grantCache.containsKey(uuid);
    }
}
