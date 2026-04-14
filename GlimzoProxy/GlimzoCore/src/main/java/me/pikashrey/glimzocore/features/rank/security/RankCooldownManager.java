package me.pikashrey.glimzocore.features.rank.security;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RankCooldownManager {

    // Global: timestamps of recent grants per staff UUID
    private final Map<UUID, List<Long>> history = new ConcurrentHashMap<>();

    // Escalation: (staffUUID -> last high-weight grant timestamp)
    // Threshold: ranks with weight >= 500 are "ladder" grants
    private static final int LADDER_WEIGHT_THRESHOLD = 500;
    // Cooldown window: 5 minutes between ladder grants
    private static final long LADDER_COOLDOWN_MS = 5 * 60 * 1000L;
    private final Map<UUID, Long> ladderCooldown = new ConcurrentHashMap<>();

    public boolean canExecute(UUID uuid, int rankWeight) {
        long now = System.currentTimeMillis();

        // --- Global rate limit: max 4 per minute ---
        List<Long> actions = history.computeIfAbsent(uuid, u -> new ArrayList<>());
        synchronized (actions) {
            actions.removeIf(t -> now - t > 60_000L);
            if (actions.size() >= 4) {
                return false;
            }

            // --- Ladder escalation check ---
            if (rankWeight >= LADDER_WEIGHT_THRESHOLD) {
                Long lastLadder = ladderCooldown.get(uuid);
                if (lastLadder != null && now - lastLadder < LADDER_COOLDOWN_MS) {
                    return false;
                }
                // Record ladder cooldown timestamp
                ladderCooldown.put(uuid, now);
            }

            actions.add(now);
        }
        return true;
    }

    /**
     * Legacy overload - for callers that don't pass rank weight (treated as non-ladder).
     */
    public boolean canExecute(UUID uuid) {
        return canExecute(uuid, 0);
    }

    public void evict(UUID uuid) {
        history.remove(uuid);
        ladderCooldown.remove(uuid);
    }

    /**
     * Returns the remaining ladder cooldown in milliseconds, or 0 if not on cooldown.
     */
    public long getLadderCooldownRemaining(UUID uuid) {
        Long last = ladderCooldown.get(uuid);
        if (last == null) return 0;
        long remaining = LADDER_COOLDOWN_MS - (System.currentTimeMillis() - last);
        return Math.max(0, remaining);
    }
}
