package me.pikashrey.glimzocore.features.permission;

import java.util.*;

public class PermissionResolver {

    private final PermissionCache cache;

    public PermissionResolver(PermissionCache cache) {
        this.cache = cache;
    }

    public Set<String> resolve(String rank,
                               Map<String, List<String>> inheritance,
                               Map<String, List<String>> permissions) {
        Set<String> cached = cache.getRank(rank);
        if (cached != null) return cached;

        Set<String> result = new HashSet<>();
        resolveRecursive(rank, inheritance, permissions, result, new HashSet<>());
        cache.cacheRank(rank, result);
        return result;
    }

    private void resolveRecursive(String rank,
                                  Map<String, List<String>> inheritance,
                                  Map<String, List<String>> permissions,
                                  Set<String> result,
                                  Set<String> visited) {
        if (visited.contains(rank)) throw new IllegalStateException("Rank inheritance loop: " + rank);
        visited.add(rank);

        List<String> parents = inheritance.get(rank);
        if (parents != null) {
            for (String parent : parents) resolveRecursive(parent, inheritance, permissions, result, visited);
        }

        List<String> perms = permissions.get(rank);
        if (perms != null) result.addAll(perms);
    }
}
