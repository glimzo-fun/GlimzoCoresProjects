package me.pikashrey.glimzocore.features.clan;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.clan.ClanLevel;
import me.pikashrey.glimzocore.api.clan.ClanMember;
import me.pikashrey.glimzocore.api.clan.ClanPerk;
import me.pikashrey.glimzocore.api.clan.ClanRole;
import me.pikashrey.glimzocore.api.events.impl.ClanCreateEvent;
import me.pikashrey.glimzocore.api.events.impl.ClanDisbandEvent;
import me.pikashrey.glimzocore.api.events.impl.ClanJoinEvent;
import me.pikashrey.glimzocore.api.events.impl.ClanLeaveEvent;
import me.pikashrey.glimzocore.api.events.impl.ClanLevelUpEvent;
import me.pikashrey.glimzocore.api.events.impl.ClanXpGainEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.api.rank.RankRef;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClanManager {

    // Invite TTL read from social.yml at call-time

    protected final GlimzoCore plugin;

    // Clans currently loaded in memory, keyed by clan id
    private final Map<String, ClanData> clanCache = new ConcurrentHashMap<>();

    private final Map<UUID, String> memberIndex = new ConcurrentHashMap<>();

    // Pending invites: invitee UUID -> (clan id, expiry epoch ms)
    // ConcurrentHashMap: accessed from both main thread and async join context
    private final Map<UUID, PendingInvite> pendingInvites = new ConcurrentHashMap<>();

    // Players who have /cc toggled on
    // Players who have /cc toggled on - accessed from async chat thread, must be thread-safe
    private final Set<UUID> clanChatEnabled = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public ClanManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    // Player lifecycle

    /** Load the clan for a player if they are in one. Call async on join. */
    public void loadForPlayer(UUID uuid, String clanId) {
        if (clanId == null || clanId.isEmpty()) return;
        if (clanCache.containsKey(clanId)) {
            // Clan already loaded - just ensure member index is up to date
            memberIndex.put(uuid, clanId);
            return;
        }
        ClanData clan = fetchClanFromDb(clanId);
        if (clan == null) return;
        clanCache.put(clanId, clan);
        for (ClanMember m : clan.getMembers()) {
            memberIndex.put(m.getUuid(), clanId);
        }
    }

    public void unloadForPlayer(UUID uuid) {
        clanChatEnabled.remove(uuid);
        pendingInvites.remove(uuid);

        String clanId = memberIndex.get(uuid);
        if (clanId == null) return;

        ClanData clan = clanCache.get(clanId);
        if (clan == null) {
            memberIndex.remove(uuid);
            return;
        }

        // Check if any other member is still online
        boolean anyOnline = false;
        for (ClanMember m : clan.getMembers()) {
            if (m.getUuid().equals(uuid)) continue;
            if (Bukkit.getPlayer(m.getUuid()) != null) {
                anyOnline = true;
                break;
            }
        }

        if (!anyOnline) {
            clanCache.remove(clanId);
            // Remove all members from index (they're all offline)
            for (ClanMember m : clan.getMembers()) {
                memberIndex.remove(m.getUuid());
            }
        }
    }

    // Cross-server sync helpers - called by NetworkSocialSync on receiving servers

    public void reloadClan(String clanId) {
        ClanData fresh = fetchClanFromDb(clanId);
        if (fresh == null) {
            evictClan(clanId);
            return;
        }
        clanCache.put(clanId, fresh);
        for (me.pikashrey.glimzocore.api.clan.ClanMember m : fresh.getMembers()) {
            memberIndex.put(m.getUuid(), clanId);
        }
    }

    public void evictClan(String clanId) {
        ClanData clan = clanCache.remove(clanId);
        if (clan == null) return;
        for (me.pikashrey.glimzocore.api.clan.ClanMember m : clan.getMembers()) {
            memberIndex.remove(m.getUuid());
        }
    }

    // Clan creation / disbanding

    public ClanData createClan(PlayerData founder, Rank founderRank,
                                String name, String tag) {
        // Rank gate
        if (!founderRank.canCreateClan()) {
            return null;
        }

        // Already in a clan
        if (founder.isInClan()) return null;

        // Name/tag uniqueness checked in DB
        if (clanNameExists(name) || clanTagExists(tag)) return null;

        String clanId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        ClanData clan = new ClanData(clanId, name, tag, "", "",
                founder.getUuid(), founder.getName(), 1, 0L, now);

        ClanMember leaderMember = new ClanMember(
                founder.getUuid(), founder.getName(), ClanRole.LEADER, now);
        clan.addMember(leaderMember);

        // Fire cancellable event
        ClanCreateEvent event = new ClanCreateEvent(founder, clan);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;
        // Publish cross-server sync AFTER confirming the event wasn't cancelled
        if (plugin.getSocialSync() != null) {
            plugin.getSocialSync().publishClanUpdate(clanId, founder.getUuid());
        }

        // Persist
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveClanToDb(clan);
            saveMemberToDb(clanId, leaderMember);
        });

        // Update player data
        founder.setClanId(clanId);
        founder.setClanRole(ClanRole.LEADER.getOrdinalValue());

        // Cache
        clanCache.put(clanId, clan);
        memberIndex.put(founder.getUuid(), clanId);

        return clan;
    }

    /**
     * Disband a clan entirely. Removes all members and deletes from DB.
     */
    public void disbandClan(ClanData clan, UUID disbandedByUuid,
                             String disbandedByName, boolean byAdmin) {
        ClanDisbandEvent event = new ClanDisbandEvent(clan, disbandedByUuid, disbandedByName, byAdmin);
        Bukkit.getPluginManager().callEvent(event);
        if (plugin.getSocialSync() != null) {
            plugin.getSocialSync().publishClanDisband(clan.getId());
        }

        // Notify + clear all members
        for (ClanMember m : clan.getMembers()) {
            memberIndex.remove(m.getUuid());
            clanChatEnabled.remove(m.getUuid());

            PlayerData data = GlobalPlayer.get(m.getUuid());
            if (data != null) {
                data.setClanId(null);
                data.setClanRole(ClanRole.MEMBER.getOrdinalValue());

                // Fire leave event for each member
                Bukkit.getPluginManager().callEvent(
                        new ClanLeaveEvent(data, clan, ClanLeaveEvent.Reason.CLAN_DISBANDED));
            }
        }

        clanCache.remove(clan.getId());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> deleteClanFromDb(clan.getId()));
    }

    // Membership

    /**
     * Invite a player to a clan. Stores a pending invite (TTL from social.yml clans.invite-ttl-seconds).
     */
    public void invitePlayer(UUID inviteeUuid, String clanId) {
        pendingInvites.put(inviteeUuid,
                new PendingInvite(clanId, System.currentTimeMillis() +
                    plugin.getConfigManager().getSocial().getLong("clans.invite-ttl-seconds", 120L) * 1000L));
    }

    public boolean hasPendingInvite(UUID uuid) {
        PendingInvite inv = pendingInvites.get(uuid);
        if (inv == null) return false;
        if (System.currentTimeMillis() > inv.expiryMs) {
            pendingInvites.remove(uuid);
            return false;
        }
        return true;
    }

    public String getPendingInviteClanId(UUID uuid) {
        PendingInvite inv = pendingInvites.get(uuid);
        return inv != null ? inv.clanId : null;
    }

    public void clearInvite(UUID uuid) {
        pendingInvites.remove(uuid);
    }

    public boolean joinClan(PlayerData player, ClanData clan, Rank leaderRank) {
        if (player.isInClan()) return false;

        int cap = getEffectiveCapacity(clan, leaderRank);
        if (clan.getMemberCount() >= cap) return false;

        // Fire cancellable event
        ClanJoinEvent event = new ClanJoinEvent(player, clan);
        Bukkit.getPluginManager().callEvent(event);
        if (plugin.getSocialSync() != null) {
            plugin.getSocialSync().publishClanUpdate(clan.getId(), player.getUuid());
        }
        if (event.isCancelled()) return false;

        long now = System.currentTimeMillis();
        ClanMember member = new ClanMember(player.getUuid(), player.getName(), ClanRole.MEMBER, now);
        clan.addMember(member);

        player.setClanId(clan.getId());
        player.setClanRole(ClanRole.MEMBER.getOrdinalValue());
        memberIndex.put(player.getUuid(), clan.getId());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                saveMemberToDb(clan.getId(), member));

        return true;
    }

    /**
     * Remove a player from a clan (leave or kick).
     */
    public void leaveClan(PlayerData player, ClanData clan, ClanLeaveEvent.Reason reason) {
        ClanLeaveEvent event = new ClanLeaveEvent(player, clan, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (plugin.getSocialSync() != null) {
            plugin.getSocialSync().publishClanUpdate(clan.getId(), player.getUuid());
        }

        clan.removeMember(player.getUuid());
        player.setClanId(null);
        player.setClanRole(ClanRole.MEMBER.getOrdinalValue());
        memberIndex.remove(player.getUuid());
        clanChatEnabled.remove(player.getUuid());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                removeMemberFromDb(clan.getId(), player.getUuid()));
    }

    /**
     * Promote or demote a member's role.
     */
    public void setMemberRole(ClanData clan, UUID targetUuid, ClanRole newRole) {
        ClanMember member = clan.getMember(targetUuid);
        if (member == null) return;
        member.setRole(newRole);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                updateMemberRoleInDb(clan.getId(), targetUuid, newRole));

        // If transferring leadership, demote the old leader
        if (newRole == ClanRole.LEADER) {
            for (ClanMember m : clan.getMembers()) {
                if (m.getUuid().equals(targetUuid)) continue;
                if (m.isLeader()) {
                    m.setRole(ClanRole.OFFICER);
                    UUID oldLeaderUuid = m.getUuid();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                            updateMemberRoleInDb(clan.getId(), oldLeaderUuid, ClanRole.OFFICER));
                }
            }
            clan.setLeaderUuid(targetUuid);
            PlayerData newLeaderData = GlobalPlayer.get(targetUuid);
            if (newLeaderData != null) clan.setLeaderName(newLeaderData.getName());
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                    updateClanLeaderInDb(clan.getId(), targetUuid));
        }
    }

    // XP and leveling

    public void addXp(ClanData clan, UUID triggeredByUuid,
                       ClanXpGainEvent.Source source, long amount) {
        ClanXpGainEvent xpEvent = new ClanXpGainEvent(clan, triggeredByUuid, source, amount);
        Bukkit.getPluginManager().callEvent(xpEvent);
        if (xpEvent.isCancelled() || xpEvent.getAmount() <= 0) return;

        int levelBefore = clan.getLevel();
        int levelAfter  = clan.addXp(xpEvent.getAmount());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                updateClanXpInDb(clan.getId(), clan.getXp(), clan.getLevel()));

        if (levelAfter > levelBefore) {
            Bukkit.getPluginManager().callEvent(
                    new ClanLevelUpEvent(clan.getId(), levelBefore, levelAfter));
        }
    }

    // Clan chat

    public void toggleClanChat(UUID uuid) {
        if (clanChatEnabled.contains(uuid)) {
            clanChatEnabled.remove(uuid);
        } else {
            clanChatEnabled.add(uuid);
        }
    }

    public boolean hasClanChatEnabled(UUID uuid) {
        return clanChatEnabled.contains(uuid);
    }

    public void sendClanChat(ClanData clan, String rankPrefix, String playerName, String message) {
        String formatted = me.pikashrey.glimzocore.utilities.chat.CC.translate(
                "&8[&aClan&8] &8>> &r" + rankPrefix + " " + playerName + "&7: &f" + message);
        for (ClanMember m : clan.getMembers()) {
            org.bukkit.entity.Player online = Bukkit.getPlayer(m.getUuid());
            if (online != null) online.sendMessage(formatted);
        }
    }

    // Lookups

    public ClanData getClan(String clanId) {
        return clanCache.get(clanId);
    }

    public ClanData getClanByPlayer(UUID uuid) {
        String clanId = memberIndex.get(uuid);
        return clanId != null ? clanCache.get(clanId) : null;
    }

    public boolean isInClan(UUID uuid) {
        return memberIndex.containsKey(uuid);
    }

    /**
     * Effective member capacity = base (from leader rank) + INCREASED_CAPACITY perk bonus.
     */
    public int getEffectiveCapacity(ClanData clan, Rank leaderRank) {
        int base  = leaderRank.getMaxClanSize();
        int bonus = (int) clan.getClanLevel().getPerkValue(ClanPerk.INCREASED_CAPACITY);
        return base + bonus;
    }

    // Database

    private ClanData fetchClanFromDb(String clanId) {
        ClanData clan = null;
        try (Connection con = plugin.getMysqlManager().getConnection()) {

            // Load clan row
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM glimzo_clans WHERE id = ?")) {
                ps.setString(1, clanId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        clan = new ClanData(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getString("tag"),
                                rs.getString("description"),
                                rs.getString("motd"),
                                UUID.fromString(rs.getString("leader_uuid")),
                                rs.getString("leader_name"),
                                rs.getInt("level"),
                                rs.getLong("xp"),
                                rs.getLong("created_at")
                        );
                    }
                }
            }

            if (clan == null) return null;

            // Load members
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM glimzo_clan_members WHERE clan_id = ?")) {
                ps.setString(1, clanId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        clan.addMember(new ClanMember(
                                UUID.fromString(rs.getString("uuid")),
                                rs.getString("name"),
                                ClanRole.fromOrdinal(rs.getInt("role")),
                                rs.getLong("joined_at")
                        ));
                    }
                }
            }

        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to load clan " + clanId + ": " + e.getMessage());
        }
        return clan;
    }

    private void saveClanToDb(ClanData clan) {
        String sql =
                "INSERT INTO glimzo_clans " +
                "(id, name, tag, description, motd, leader_uuid, leader_name, level, xp, created_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?) " +
                "AS new_row ON DUPLICATE KEY UPDATE " +
                "name=new_row.name, tag=new_row.tag, description=new_row.description, " +
                "motd=new_row.motd, leader_uuid=new_row.leader_uuid, leader_name=new_row.leader_name, " +
                "level=new_row.level, xp=new_row.xp";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clan.getId());
            ps.setString(2, clan.getName());
            ps.setString(3, clan.getTag());
            ps.setString(4, clan.getDescription());
            ps.setString(5, clan.getMotd());
            ps.setString(6, clan.getLeaderUuid().toString());
            ps.setString(7, clan.getLeaderName());
            ps.setInt(8, clan.getLevel());
            ps.setLong(9, clan.getXp());
            ps.setLong(10, clan.getCreatedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to save clan " + clan.getName() + ": " + e.getMessage());
        }
    }

    private void saveMemberToDb(String clanId, ClanMember member) {
        String sql =
                "INSERT IGNORE INTO glimzo_clan_members (clan_id, uuid, name, role, joined_at) " +
                "VALUES (?,?,?,?,?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clanId);
            ps.setString(2, member.getUuid().toString());
            ps.setString(3, member.getName());
            ps.setInt(4, member.getRole().getOrdinalValue());
            ps.setLong(5, member.getJoinedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to save member: " + e.getMessage());
        }
    }

    private void removeMemberFromDb(String clanId, UUID uuid) {
        String sql = "DELETE FROM glimzo_clan_members WHERE clan_id = ? AND uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clanId);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to remove member: " + e.getMessage());
        }
    }

    private void updateMemberRoleInDb(String clanId, UUID uuid, ClanRole role) {
        String sql = "UPDATE glimzo_clan_members SET role = ? WHERE clan_id = ? AND uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, role.getOrdinalValue());
            ps.setString(2, clanId);
            ps.setString(3, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to update member role: " + e.getMessage());
        }
    }

    private void updateClanLeaderInDb(String clanId, UUID leaderUuid) {
        String sql = "UPDATE glimzo_clans SET leader_uuid = ? WHERE id = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, leaderUuid.toString());
            ps.setString(2, clanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to update clan leader: " + e.getMessage());
        }
    }

    private void updateClanXpInDb(String clanId, long xp, int level) {
        String sql = "UPDATE glimzo_clans SET xp = ?, level = ? WHERE id = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, xp);
            ps.setInt(2, level);
            ps.setString(3, clanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to update clan XP: " + e.getMessage());
        }
    }

    private void deleteClanFromDb(String clanId) {
        // ON DELETE CASCADE handles glimzo_clan_members automatically
        String sql = "DELETE FROM glimzo_clans WHERE id = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[ClanManager] Failed to delete clan: " + e.getMessage());
        }
    }

    private boolean clanNameExists(String name) {
        String sql = "SELECT 1 FROM glimzo_clans WHERE name = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean clanTagExists(String tag) {
        String sql = "SELECT 1 FROM glimzo_clans WHERE tag = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static class PendingInvite {
        final String clanId;
        final long   expiryMs;
        PendingInvite(String clanId, long expiryMs) {
            this.clanId   = clanId;
            this.expiryMs = expiryMs;
        }
    }

    // RankRef bridge overloads
    // Allows callers using the config-first RankRef to interact with
    // clan creation/capacity logic without rewriting ClanManager internals.
    // canCreateClan() and getMaxClanSize() are still enum-driven; config-only
    // ranks default to "cannot create clan" (size 0) unless they map to an enum rank.

    public ClanData createClan(PlayerData founder,
                                RankRef founderRankRef,
                                String name, String tag) {
        // Weight-based check: any rank with weight >= legendary (400) can create a clan.
        // We intentionally DO NOT fall back to the enum canCreateClan() check, because
        // config-only ranks above legendary would otherwise be blocked.
        int legendaryWeight = 400;
        if (founderRankRef.getWeight() < legendaryWeight) return null;
        return createClanInternal(founder, name, tag);
    }

    /** Internal clan creation - weight gate already checked by callers. */
    private ClanData createClanInternal(PlayerData founder, String name, String tag) {
        // Forward to the Rank overload with a synthetic check-only Rank instance
        // by using the weight-based path directly.
        if (founder.isInClan()) return null;
        if (clanNameExists(name) || clanTagExists(tag)) return null;
        String clanId = java.util.UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        me.pikashrey.glimzocore.api.clan.ClanData clan = new me.pikashrey.glimzocore.api.clan.ClanData(
                clanId, name, tag, "", "",
                founder.getUuid(), founder.getName(), 1, 0L, now);
        me.pikashrey.glimzocore.api.clan.ClanMember leaderMember =
                new me.pikashrey.glimzocore.api.clan.ClanMember(
                        founder.getUuid(), founder.getName(),
                        me.pikashrey.glimzocore.api.clan.ClanRole.LEADER, now);
        clan.addMember(leaderMember);
        me.pikashrey.glimzocore.api.events.impl.ClanCreateEvent event =
                new me.pikashrey.glimzocore.api.events.impl.ClanCreateEvent(founder, clan);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;
        if (plugin.getSocialSync() != null)
            plugin.getSocialSync().publishClanUpdate(clanId, founder.getUuid());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveClanToDb(clan);
            saveMemberToDb(clanId, leaderMember);
        });
        founder.setClanId(clanId);
        founder.setClanRole(me.pikashrey.glimzocore.api.clan.ClanRole.LEADER.getOrdinalValue());
        clanCache.put(clanId, clan);
        for (me.pikashrey.glimzocore.api.clan.ClanMember m : clan.getMembers())
            memberIndex.put(m.getUuid(), clanId);
        return clan;
    }

    public boolean joinClan(PlayerData player,
                             ClanData clan,
                             RankRef leaderRankRef) {
        // Try enum path first for backwards compat; fall back to capacity check by weight
        me.pikashrey.glimzocore.api.rank.Rank enumRank = leaderRankRef.toEnum();
        if (enumRank != null) return joinClan(player, clan, enumRank);
        // Config-only rank: use getEffectiveCapacity with weight-derived max size
        int capacity = getEffectiveCapacityByWeight(clan, leaderRankRef.getWeight());
        return joinClanWithCapacity(player, clan, capacity);
    }

    public int getEffectiveCapacity(ClanData clan,
                                     RankRef leaderRankRef) {
        me.pikashrey.glimzocore.api.rank.Rank enumRank = leaderRankRef.toEnum();
        if (enumRank != null) return getEffectiveCapacity(clan, enumRank);
        return getEffectiveCapacityByWeight(clan, leaderRankRef.getWeight());
    }

    /**
     * Derives max clan size from ConfiguredRank.getMaxClanSize() for config-only ranks.
     * Falls back to the configured rank with the closest weight.
     */
    private int getEffectiveCapacityByWeight(me.pikashrey.glimzocore.api.clan.ClanData clan, int weight) {
        me.pikashrey.glimzocore.features.rank.RankLoader loader = plugin.getRankLoader();
        if (loader == null) return clan.getMemberCount();
        // Find the ConfiguredRank with weight <= leaderWeight that has the highest maxClanSize
        int best = clan.getClanLevel().getMaxMembers();
        for (me.pikashrey.glimzocore.features.rank.ConfiguredRank r : loader.getAll()) {
            if (r.getWeight() <= weight && r.getMaxClanSize() > 0) {
                best = Math.max(best, r.getMaxClanSize());
            }
        }
        return best;
    }

    private boolean joinClanWithCapacity(PlayerData player,
                                          me.pikashrey.glimzocore.api.clan.ClanData clan,
                                          int capacity) {
        if (player.isInClan()) return false;
        if (clan.getMemberCount() >= capacity) return false;
        me.pikashrey.glimzocore.api.clan.ClanMember member =
                new me.pikashrey.glimzocore.api.clan.ClanMember(
                        player.getUuid(), player.getName(),
                        me.pikashrey.glimzocore.api.clan.ClanRole.MEMBER,
                        System.currentTimeMillis());
        clan.addMember(member);
        memberIndex.put(player.getUuid(), clan.getId());
        player.setClanId(clan.getId());
        player.setClanRole(me.pikashrey.glimzocore.api.clan.ClanRole.MEMBER.getOrdinalValue());
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> saveMemberToDb(clan.getId(), member));
        return true;
    }
}
