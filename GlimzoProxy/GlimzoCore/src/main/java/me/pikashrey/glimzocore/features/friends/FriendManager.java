package me.pikashrey.glimzocore.features.friends;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendManager {

    // Read from social.yml at call-time - no static cache needed (TTL is set once on server start)

    protected final GlimzoCore plugin;

    // UUID -> set of friend UUIDs. Values are ConcurrentHashMap.newKeySet() so
    // both the map and its Set values are individually thread-safe.
    private final Map<UUID, Set<UUID>>     friends  = new ConcurrentHashMap<>();
    // Requestee UUID -> list of pending requests (supports multiple senders)
    private final Map<UUID, java.util.List<FriendRequest>> requests = new java.util.concurrent.ConcurrentHashMap<>();

    public FriendManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void loadFriends(UUID uuid) {
        Set<UUID> loaded = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
        loaded.addAll(fetchFromDb(uuid));
        friends.put(uuid, loaded);
    }

    public void loadFriendsFromData(UUID uuid, java.util.Set<String> uuidStrings) {
        Set<UUID> loaded = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (String s : uuidStrings) { try { loaded.add(UUID.fromString(s)); } catch (Exception ignored) {} }
        friends.put(uuid, loaded);
    }

    public void unloadFriends(UUID uuid) {
        friends.remove(uuid);
        requests.remove(uuid); // clears all pending requests to/from this player
    }

    // Friend requests

    public void sendRequest(UUID from, UUID to) {
        long ttlMs = plugin.getConfigManager().getSocial().getLong("friends.request-ttl-seconds", 120L) * 1000L;
        requests.computeIfAbsent(to, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(new FriendRequest(from, System.currentTimeMillis() + ttlMs));

        Player toPlayer = Bukkit.getPlayer(to);
        if (toPlayer != null) {
            PlayerData fromData = GlobalPlayer.get(from);
            String fromName = fromData != null ? fromData.getDisplayName() : from.toString();
            // Send clickable notification via tellraw-style JSON (uses Bukkit spigot component if available)
            String msg = CC.translate("&a" + fromName + " &7sent you a friend request!");
            String hint = CC.translate("&7Click: &a[Accept] &cor &c[Deny]");
            toPlayer.sendMessage(msg);
            toPlayer.sendMessage(hint);
            toPlayer.sendMessage(CC.translate("&7Type &a/friend accept " + fromName + " &7or &c/friend deny " + fromName));
        }
    }

    public boolean hasRequest(UUID from, UUID to) {
        java.util.List<FriendRequest> list = requests.get(to);
        if (list == null) return false;
        long now = System.currentTimeMillis();
        list.removeIf(r -> now > r.expiryMs);
        for (FriendRequest r : list) if (r.requesterUuid.equals(from)) return true;
        return false;
    }

    public boolean hasPendingRequest(UUID to) {
        java.util.List<FriendRequest> list = requests.get(to);
        if (list == null) return false;
        list.removeIf(r -> System.currentTimeMillis() > r.expiryMs);
        return !list.isEmpty();
    }

    /** Returns the UUID of the first non-expired pending request to this player, or null. */
    public UUID getPendingRequester(UUID recipient) {
        java.util.List<FriendRequest> list = requests.get(recipient);
        if (list == null) return null;
        long now = System.currentTimeMillis();
        list.removeIf(r -> now > r.expiryMs);
        return list.isEmpty() ? null : list.get(0).requesterUuid;
    }

    public void denyRequest(UUID recipient, UUID requesterUuid) {
        java.util.List<FriendRequest> list = requests.get(recipient);
        if (list != null) list.removeIf(r -> r.requesterUuid.equals(requesterUuid));
    }

    // Add / remove

    public void addFriend(UUID a, UUID b) {
        friends.computeIfAbsent(a, k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(b);
        friends.computeIfAbsent(b, k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(a);
        // Remove request entries between these two players only
        java.util.List<FriendRequest> ra = requests.get(a);
        java.util.List<FriendRequest> rb = requests.get(b);
        if (ra != null) ra.removeIf(r -> r.requesterUuid.equals(b));
        if (rb != null) rb.removeIf(r -> r.requesterUuid.equals(a));

        PlayerData aData = GlobalPlayer.get(a);
        PlayerData bData = GlobalPlayer.get(b);
        if (aData != null) aData.getStats().incrementFriendsAdded();
        if (bData != null) bData.getStats().incrementFriendsAdded();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveToDb(a, b);
            saveToDb(b, a);
        });

        // Notify both if online
        notifyIfOnline(a, "&a" + (bData != null ? bData.getDisplayName() : b) + " &7is now your friend!");
        notifyIfOnline(b, "&a" + (aData != null ? aData.getDisplayName() : a) + " &7is now your friend!");

        // Achievement triggers
        if (plugin.getAchievementManager() != null) {
            plugin.getAchievementManager().checkFriendAdded(a, getFriendCount(a));
            plugin.getAchievementManager().checkFriendAdded(b, getFriendCount(b));
        }

        // Sync to other servers
        String nameA = aData != null ? aData.getName() : a.toString();
        String nameB = bData != null ? bData.getName() : b.toString();
        if (plugin.getSocialSync() != null) {
            plugin.getSocialSync().publishFriendAdd(a, b, nameA, nameB);
        }
    }

    public void removeFriend(UUID a, UUID b) {
        Set<UUID> aFriends = friends.get(a);
        Set<UUID> bFriends = friends.get(b);
        if (aFriends != null) aFriends.remove(b);
        if (bFriends != null) bFriends.remove(a);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> removeFromDb(a, b));

        if (plugin.getSocialSync() != null) {
            plugin.getSocialSync().publishFriendRemove(a, b);
        }
    }

    // Queries

    // Called by NetworkSocialSync on the receiving server - only updates in-memory cache
    /**
     * Finds the UUID of someone who sent a pending friend request to {@code recipient}
     * matching the given player name. Works even if the requester is offline.
     */
    public java.util.UUID findRequesterByName(UUID recipient, String name) {
        java.util.List<FriendRequest> list = requests.get(recipient);
        if (list == null) return null;
        long now = System.currentTimeMillis();
        for (FriendRequest req : list) {
            if (now > req.expiryMs) continue;
            me.pikashrey.glimzocore.api.player.PlayerData pd =
                    me.pikashrey.glimzocore.api.player.GlobalPlayer.get(req.requesterUuid);
            String requesterName = pd != null ? pd.getName()
                    : (org.bukkit.Bukkit.getOfflinePlayer(req.requesterUuid).getName());
            if (name.equalsIgnoreCase(requesterName)) return req.requesterUuid;
        }
        return null;
    }

    public int getFriendCount(UUID uuid) {
        return friends.getOrDefault(uuid, Collections.emptySet()).size();
    }

    public Set<UUID> getFriends(UUID uuid) {
        return friends.getOrDefault(uuid, Collections.emptySet());
    }

    public boolean areFriends(UUID a, UUID b) {
        Set<UUID> aFriends = friends.get(a);
        return aFriends != null && aFriends.contains(b);
    }

    public void addFriendLocal(UUID a, UUID b) {
        friends.computeIfAbsent(a, k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(b);
    }

    public void removeFriendLocal(UUID a, UUID b) {
        Set<UUID> aFriends = friends.get(a);
        if (aFriends != null) aFriends.remove(b);
    }

    public List<UUID> getOnlineFriends(UUID uuid) {
        List<UUID> online = new ArrayList<>();
        for (UUID fUuid : friends.getOrDefault(uuid, Collections.emptySet())) {
            if (Bukkit.getPlayer(fUuid) != null) online.add(fUuid);
        }
        return online;
    }

    private void notifyIfOnline(UUID uuid, String message) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) player.sendMessage(CC.translate(message));
    }

    // Database

    private Set<UUID> fetchFromDb(UUID uuid) {
        Set<UUID> set = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
        String sql = "SELECT friend_uuid FROM glimzo_friends WHERE uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(UUID.fromString(rs.getString("friend_uuid")));
            }
        } catch (SQLException e) {
            plugin.log("&c[FriendManager] Failed to load friends for " + uuid + ": " + e.getMessage());
        }
        return set;
    }

    private void saveToDb(UUID uuid, UUID friendUuid) {
        String sql = "INSERT IGNORE INTO glimzo_friends (uuid, friend_uuid) VALUES (?,?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, friendUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[FriendManager] Failed to save friendship: " + e.getMessage());
        }
    }

    private void removeFromDb(UUID a, UUID b) {
        String sql = "DELETE FROM glimzo_friends WHERE (uuid=? AND friend_uuid=?) OR (uuid=? AND friend_uuid=?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.toString()); ps.setString(2, b.toString());
            ps.setString(3, b.toString()); ps.setString(4, a.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[FriendManager] Failed to remove friendship: " + e.getMessage());
        }
    }

    private static class FriendRequest {
        final UUID requesterUuid;
        final long expiryMs;
        FriendRequest(UUID r, long e) { requesterUuid = r; expiryMs = e; }
    }
}

