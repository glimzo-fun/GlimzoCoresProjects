package me.pikashrey.glimzocore.features.rank.security;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RankCooldownManager {

    private final Map<UUID, List<Long>> history = new ConcurrentHashMap<>();

    // ranks with weight >= 500 count as "ladder" grants
    private static final int  LADDER_THRESHOLD   = 500;
    private static final long LADDER_COOLDOWN_MS = 5 * 60 * 1000L;
    private final Map<UUID, Long> ladderTimestamps = new ConcurrentHashMap<>();

    public boolean canExecute(UUID uuid, int rankWeight) {
        long now = System.currentTimeMillis();

        List<Long> actions = history.computeIfAbsent(uuid, u -> new ArrayList<>());
        synchronized (actions) {
            actions.removeIf(t -> now - t > 60_000L);
            if (actions.size() >= 4) return false;

            if (rankWeight >= LADDER_THRESHOLD) {
                Long last = ladderTimestamps.get(uuid);
                if (last != null && now - last < LADDER_COOLDOWN_MS) return false;
                ladderTimestamps.put(uuid, now);
            }

            actions.add(now);
        }
        return true;
    }

    public boolean canExecute(UUID uuid) {
        return canExecute(uuid, 0);
    }

    public void evict(UUID uuid) {
        history.remove(uuid);
        ladderTimestamps.remove(uuid);
    }

    public long getLadderCooldownRemaining(UUID uuid) {
        Long last = ladderTimestamps.get(uuid);
        if (last == null) return 0;
        return Math.max(0, LADDER_COOLDOWN_MS - (System.currentTimeMillis() - last));
    }
}
