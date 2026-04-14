package me.pikashrey.glimzocore.features.cosmetics.aura;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BaseAura {

    private final String id;
    private final String displayName;
    private final String permission; // null = no permission required

    // Per-player state tracking (e.g., animation angles)
    protected final Map<UUID, AuraState> playerStates = new HashMap<>();

    public BaseAura(String id, String displayName, String permission) {
        this.id          = id;
        this.displayName = displayName;
        this.permission  = permission;
    }

    public abstract void tick(Player player);

    /**
     * Called when the aura is equipped - use for setup if needed.
     */
    public void equip(Player player) {
        // Initialize per-player state
        playerStates.put(player.getUniqueId(), new AuraState());
    }

    /**
     * Called when the aura is removed - clean up any leftover particles/state.
     */
    public void unequip(Player player) {
        playerStates.remove(player.getUniqueId());
    }

    /**
     * Called on plugin shutdown or aura removal - global cleanup.
     */
    public void cleanup() {
        playerStates.clear();
    }

    /**
     * Get per-player state, creating if not exists
     */
    protected AuraState getState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), k -> new AuraState());
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getPermission()  { return permission; }
    public boolean hasPermission() { return permission != null && !permission.isEmpty(); }

    /**
     * Per-player aura state container
     */
    public static class AuraState {
        public double angle = 0;
        public long lastTick = 0L; // plain tick counter, NOT a timestamp
        public int particleCount = 0;

        public void reset() {
            angle = 0;
            lastTick = 0L;
            particleCount = 0;
        }
    }
}