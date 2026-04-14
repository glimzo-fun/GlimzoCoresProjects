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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RankManager {

    private final Map<UUID, GrantProcedure> activeProcedures = new ConcurrentHashMap<>();
    private final Map<UUID, List<Grant>>    grantCache       = new ConcurrentHashMap<>();

    private final GlimzoCore plugin;
    private final PermissionCache permissionCache;
    private final PermissionManager permissionManager;

    public RankManager(GlimzoCore plugin, PermissionCache permissionCache) {
        this.plugin = plugin;
        this.permissionCache = permissionCache;
        this.permissionManager = GlimzoCore.getInstance().getPermissionManager();
    }

    public void loadGrants(UUID uuid) {
        List<Grant> grants = new CopyOnWriteArrayList<>(fetchGrantsFromDb(uuid));
        grantCache.put(uuid, grants);
        permissionCache.invalidatePlayer(uuid);
    }

    public void loadGrantsFromData(UUID uuid, java.util.List<java.util.Map<String, Object>> rows) {
        List<Grant> grants = new CopyOnWriteArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            String rankId = (String) row.get("rank_id");
            RankRef rankRef = RankRef.of(rankId);
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

    public void unloadGrants(UUID uuid) {
        grantCache.remove(uuid);
        activeProcedures.remove(uuid);
        permissionCache.invalidatePlayer(uuid);
        GlimzoCore.getInstance().getRankCooldownManager().evict(uuid);
    }

    public Rank getActiveRank(UUID uuid) {
        RankRef ref = getActiveRankRef(uuid);
        Rank enumRank = ref.toEnum();
        return enumRank != null ? enumRank : Rank.getDefault();
    }

    public RankRef getActiveRankRef(UUID uuid) {
        List<Grant> grants = grantCache.get(uuid);
        if (grants == null || grants.isEmpty()) return RankRef.of(Rank.getDefault());

        Grant best = null;
        for (Grant g : grants) {
            if (!g.isActive()) continue;
            if (best == null || g.getEffectiveWeight() > best.getEffectiveWeight()) best = g;
        }
        return best != null ? best.getRankRef() : RankRef.of(Rank.getDefault());
    }

    public Rank getActiveRank(Player player) {
        return getActiveRank(player.getUniqueId());
    }

    public RankRef getActiveRankRef(Player player) {
        return getActiveRankRef(player.getUniqueId());
    }

    public String getChatPrefix(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);

        if (data != null) {
            String cached = data.getCachedChatPrefix();
            if (cached != null) return cached;
        }

        String prefix;
        if (data != null && data.hasCustomPrefix()) {
            prefix = me.pikashrey.glimzocore.utilities.chat.CC.translate(data.getCustomPrefix());
        } else {
            prefix = me.pikashrey.glimzocore.utilities.chat.CC.translate(getActiveRankRef(uuid).getChatPrefix());
        }

        if (data != null) data.setCachedChatPrefix(prefix);
        return prefix;
    }

    public List<Grant> getActiveGrants(UUID uuid) {
        List<Grant> all = grantCache.getOrDefault(uuid, Collections.emptyList());
        List<Grant> active = new ArrayList<>();
        for (Grant g : all) {
            if (g.isActive()) {
                active.add(g);
            } else if (g.hasJustExpired()) {
                markGrantExpired(g);
            }
        }
        return active;
    }

    public Set<String> resolvePermissions(UUID uuid) {
        Set<String> cached = permissionCache.getPlayer(uuid);
        if (cached != null) return cached;

        List<Grant> grants = getActiveGrants(uuid);
        Set<String> result = new HashSet<>();
        for (Grant grant : grants) {
            Set<String> rankPerms = permissionCache.getRank(grant.getRankRef().getId());
            if (rankPerms != null) result.addAll(rankPerms);
        }

        permissionCache.cachePlayer(uuid, result);
        return result;
    }

    public List<Grant> getAllGrants(UUID uuid) {
        return Collections.unmodifiableList(grantCache.getOrDefault(uuid, Collections.emptyList()));
    }

    public boolean addGrant(Grant grant) {
        UUID uuid = grant.getTargetUuid();
        PlayerData data = GlobalPlayer.get(uuid);

        if (data == null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveGrantToDb(grant));
            return true;
        }

        RankRef rankBefore = getActiveRankRef(uuid);

        PlayerGrantEvent grantEvent = new PlayerGrantEvent(
                data, grant.getRankRef().getId(), grant.getIssuedByName(), grant.getExpiresAt());
        Bukkit.getPluginManager().callEvent(grantEvent);
        if (grantEvent.isCancelled()) return false;

        grantCache.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(grant);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveGrantToDb(grant));
        permissionCache.invalidatePlayer(uuid);

        RankRef rankAfter = getActiveRankRef(uuid);
        applyPermissions(Bukkit.getPlayer(uuid));

        if (!rankAfter.equals(rankBefore)) {
            Bukkit.getPluginManager().callEvent(new PlayerRankChangeEvent(data, rankBefore, rankAfter, grant));
        }

        return true;
    }

    public void revokeGrant(UUID targetUuid, int grantId) {
        List<Grant> grants = grantCache.get(targetUuid);
        RankRef rankBefore = getActiveRankRef(targetUuid);

        if (grants != null) {
            for (Grant g : grants) {
                if (g.getId() == grantId) { g.setActive(false); break; }
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            deactivateGrantInDb(grantId);
            plugin.getNetworkRankSync().publishRankChange(targetUuid);
        });

        Player online = Bukkit.getPlayer(targetUuid);
        if (online != null) {
            permissionCache.invalidatePlayer(targetUuid);
            RankRef rankAfter = getActiveRankRef(targetUuid);
            applyPermissions(online);
            if (!rankAfter.equals(rankBefore)) {
                PlayerData data = GlobalPlayer.get(targetUuid);
                if (data != null) {
                    Bukkit.getPluginManager().callEvent(new PlayerRankChangeEvent(data, rankBefore, rankAfter, null));
                }
            }
        }

        try {
            plugin.getMysqlManager().logRankAction(
                    null, targetUuid, rankBefore.getId(), "REVOKE",
                    System.currentTimeMillis(), plugin.getConfig().getString("server.id", "unknown"));
        } catch (Exception ignored) {}
    }

    private void applyPermissions(Player player) {
        if (player == null) return;
        permissionManager.applyPermissions(player, resolvePermissions(player.getUniqueId()));
    }

    public String getConfiguredPrefix(Rank rank) {
        if (rank == null) return Rank.getDefault().getChatPrefix();
        RankLoader loader = GlimzoCore.getInstance().getRankLoader();
        if (loader != null) {
            ConfiguredRank cfg = loader.get(rank.getId());
            if (cfg != null) return cfg.getPrefix();
        }
        return rank.getChatPrefix();
    }

    public void startProcedure(GrantProcedure procedure) {
        activeProcedures.put(procedure.getStaffUuid(), procedure);
    }

    public GrantProcedure getProcedure(UUID staffUuid) {
        GrantProcedure p = activeProcedures.get(staffUuid);
        if (p != null && p.isExpired()) { activeProcedures.remove(staffUuid); return null; }
        return p;
    }

    public void clearProcedure(UUID staffUuid) { activeProcedures.remove(staffUuid); }
    public boolean hasProcedure(UUID staffUuid) { return getProcedure(staffUuid) != null; }

    private List<Grant> fetchGrantsFromDb(UUID uuid) {
        List<Grant> grants = new ArrayList<>();
        String sql = "SELECT * FROM glimzo_grants WHERE target_uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rankId = rs.getString("rank_id");
                    RankRef rankRef = RankRef.of(rankId);
                    if (!rankRef.isValid()) {
                        plugin.log("&e[RankManager] Skipping grant with unknown rank id '" + rankId + "'");
                        continue;
                    }
                    String issuedByStr = rs.getString("issued_by_uuid");
                    grants.add(new Grant(
                            rs.getInt("id"), uuid, rs.getString("target_name"),
                            issuedByStr != null ? UUID.fromString(issuedByStr) : null,
                            rs.getString("issued_by_name"), rankRef,
                            rs.getLong("issued_at"), rs.getLong("expires_at"),
                            rs.getBoolean("staff_grant"), rs.getBoolean("active")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[RankManager] Failed to load grants for " + uuid + ": " + e.getMessage());
        }
        return grants;
    }

    private void saveGrantToDb(Grant grant) {
        String sql = "INSERT INTO glimzo_grants " +
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

        try {
            plugin.getMysqlManager().logRankAction(
                    grant.getIssuedByUuid(), grant.getTargetUuid(),
                    grant.getRankRef().getId(), "GRANT",
                    System.currentTimeMillis(), plugin.getConfig().getString("server.id", "unknown"));
        } catch (Exception ignored) {}
        plugin.getNetworkRankSync().publishRankChange(grant.getTargetUuid());
    }

    private void deactivateGrantInDb(int grantId) {
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE glimzo_grants SET active = 0 WHERE id = ?")) {
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
                        null, grant.getTargetUuid(), grant.getRankRef().getId(), "EXPIRE",
                        System.currentTimeMillis(), plugin.getConfig().getString("server.id", "unknown"));
            } catch (Exception ignored) {}
            plugin.getNetworkRankSync().publishRankChange(grant.getTargetUuid());
        });
    }

    public boolean hasGrantsLoaded(UUID uuid) { return grantCache.containsKey(uuid); }
}
