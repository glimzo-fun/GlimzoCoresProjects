package me.pikashrey.glimzocore.features.permission;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionCache {

    // Rank cache is written once at startup/reload and read frequently - safe to use ConcurrentHashMap
    private final Map<String, Set<String>> rankCache   = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>>   playerCache = new ConcurrentHashMap<>();

    // --- Player cache ---

    public Set<String> getPlayer(UUID uuid) {
        return playerCache.get(uuid);
    }

    public void cachePlayer(UUID uuid, Set<String> perms) {
        playerCache.put(uuid, Collections.unmodifiableSet(new HashSet<>(perms)));
    }

    public void invalidatePlayer(UUID uuid) {
        playerCache.remove(uuid);
    }

    // --- Rank cache ---

    public void cacheRank(String rank, Set<String> perms) {
        rankCache.put(rank.toLowerCase(), Collections.unmodifiableSet(new HashSet<>(perms)));
    }

    public Set<String> getRank(String rank) {
        return rank == null ? null : rankCache.get(rank.toLowerCase());
    }

    public void clearRanks() {
        rankCache.clear();
    }

    public void clearAll() {
        playerCache.clear();
        rankCache.clear();
    }
}
