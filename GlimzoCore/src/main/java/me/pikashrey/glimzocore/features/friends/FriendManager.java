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

    protected final GlimzoCore plugin;

    private final Map<UUID, Set<UUID>>             friends  = new ConcurrentHashMap<>();
    private final Map<UUID, List<FriendRequest>>   requests = new ConcurrentHashMap<>();

    public FriendManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void loadFriends(UUID uuid) {
        Set<UUID> loaded = Collections.newSetFromMap(new ConcurrentHashMap<>());
        loaded.addAll(fetchFromDb(uuid));
        friends.put(uuid, loaded);
    }

    public void loadFriendsFromData(UUID uuid, java.util.Set<String> uuidStrings) {
        Set<UUID> loaded = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (String s : uuidStrings) { try { loaded.add(UUID.fromString(s)); } catch (Exception ignored) {} }
        friends.put(uuid, loaded);
    }

    public void unloadFriends(UUID uuid) {
        friends.remove(uuid);
        requests.remove(uuid);
    }

    public void sendRequest(UUID from, UUID to) {
        long ttlMs = plugin.getConfigManager().getSocial().getLong("friends.request-ttl-seconds", 120L) * 1000L;
        requests.computeIfAbsent(to, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(new FriendRequest(from, System.currentTimeMillis() + ttlMs));

        Player toPlayer = Bukkit.getPlayer(to);
        if (toPlayer != null) {
            PlayerData fromData = GlobalPlayer.get(from);
            String fromName = fromData != null ? fromData.getDisplayName() : from.toString();
            toPlayer.sendMessage(CC.translate("&a" + fromName + " &7sent you a friend request!"));
            toPlayer.sendMessage(CC.translate("&7Type &a/friend accept " + fromName + " &7or &c/friend deny " + fromName));
        }
    }

    public boolean hasRequest(UUID from, UUID to) {
        List<FriendRequest> list = requests.get(to);
        if (list == null) return false;
        long now = System.currentTimeMillis();
        list.removeIf(r -> now > r.expiryMs);
        for (FriendRequest r : list) if (r.requesterUuid.equals(from)) return true;
        return false;
    }

    public boolean hasPendingRequest(UUID to) {
        List<FriendRequest> list = requests.get(to);
        if (list == null) return false;
        list.removeIf(r -> System.currentTimeMillis() > r.expiryMs);
        return !list.isEmpty();
    }

    public UUID getPendingRequester(UUID recipient) {
        List<FriendRequest> list = requests.get(recipient);
        if (list == null) return null;
        list.removeIf(r -> System.currentTimeMillis() > r.expiryMs);
        return list.isEmpty() ? null : list.get(0).requesterUuid;
    }

    public void denyRequest(UUID recipient, UUID requester) {
        List<FriendRequest> list = requests.get(recipient);
        if (list != null) list.removeIf(r -> r.requesterUuid.equals(requester));
    }

    public void addFriend(UUID a, UUID b) {
        friends.computeIfAbsent(a, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(b);
        friends.computeIfAbsent(b, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(a);

        List<FriendRequest> ra = requests.get(a);
        List<FriendRequest> rb = requests.get(b);
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

        notifyIfOnline(a, "&a" + (bData != null ? bData.getDisplayName() : b) + " &7is now your friend!");
        notifyIfOnline(b, "&a" + (aData != null ? aData.getDisplayName() : a) + " &7is now your friend!");

        if (plugin.getAchievementManager() != null) {
            plugin.getAchievementManager().checkFriendAdded(a, getFriendCount(a));
            plugin.getAchievementManager().checkFriendAdded(b, getFriendCount(b));
        }

        String nameA = aData != null ? aData.getName() : a.toString();
        String nameB = bData != null ? bData.getName() : b.toString();
        if (plugin.getSocialSync() != null) plugin.getSocialSync().publishFriendAdd(a, b, nameA, nameB);
    }

    public void removeFriend(UUID a, UUID b) {
        Set<UUID> aFriends = friends.get(a);
        Set<UUID> bFriends = friends.get(b);
        if (aFriends != null) aFriends.remove(b);
        if (bFriends != null) bFriends.remove(a);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> removeFromDb(a, b));
        if (plugin.getSocialSync() != null) plugin.getSocialSync().publishFriendRemove(a, b);
    }

    public UUID findRequesterByName(UUID recipient, String name) {
        List<FriendRequest> list = requests.get(recipient);
        if (list == null) return null;
        long now = System.currentTimeMillis();
        for (FriendRequest req : list) {
            if (now > req.expiryMs) continue;
            PlayerData pd = GlobalPlayer.get(req.requesterUuid);
            String reqName = pd != null ? pd.getName() : Bukkit.getOfflinePlayer(req.requesterUuid).getName();
            if (name.equalsIgnoreCase(reqName)) return req.requesterUuid;
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
        friends.computeIfAbsent(a, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(b);
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
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) p.sendMessage(CC.translate(message));
    }

    private Set<UUID> fetchFromDb(UUID uuid) {
        Set<UUID> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
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
