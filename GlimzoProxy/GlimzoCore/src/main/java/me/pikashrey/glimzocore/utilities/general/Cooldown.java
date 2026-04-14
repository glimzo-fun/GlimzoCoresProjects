package me.pikashrey.glimzocore.utilities.general;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cooldown {

    private final long durationMs;
    private final Map<UUID, Long> expiries = new java.util.concurrent.ConcurrentHashMap<>();

    public Cooldown(long durationMs) {
        this.durationMs = durationMs;
    }

    /** Mark a player as on cooldown starting now. */
    public void set(UUID uuid) {
        expiries.put(uuid, System.currentTimeMillis() + durationMs);
    }

    /** Set a one-off custom duration for this player. */
    public void set(UUID uuid, long customDurationMs) {
        expiries.put(uuid, System.currentTimeMillis() + customDurationMs);
    }

    /** Whether the player is currently on cooldown. */
    public boolean isOnCooldown(UUID uuid) {
        Long expiry = expiries.get(uuid);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            expiries.remove(uuid);
            return false;
        }
        return true;
    }

    /** Remaining cooldown in milliseconds. 0 if not on cooldown. */
    public long remaining(UUID uuid) {
        Long expiry = expiries.get(uuid);
        if (expiry == null) return 0;
        long r = expiry - System.currentTimeMillis();
        return Math.max(0, r);
    }

    /** Remaining formatted as a human-readable string. */
    public String remainingFormatted(UUID uuid) {
        return TimeFormatUtils.format(remaining(uuid));
    }

    /** Clear a player's cooldown. */
    public void clear(UUID uuid) {
        expiries.remove(uuid);
    }

    /** Clear all cooldowns. */
    public void clearAll() {
        expiries.clear();
    }
}

