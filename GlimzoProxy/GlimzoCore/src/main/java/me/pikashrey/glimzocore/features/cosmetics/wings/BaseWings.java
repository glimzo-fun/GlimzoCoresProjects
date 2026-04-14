package me.pikashrey.glimzocore.features.cosmetics.wings;

import org.bukkit.entity.Player;

/** Wings system - Coming Soon. Stub only. */
public abstract class BaseWings {

    private final String id;
    private final String displayName;

    public BaseWings(String id, String displayName) {
        this.id          = id;
        this.displayName = displayName;
    }

    public void equip(Player player)   { /* Coming Soon */ }
    public void unequip(Player player) { /* Coming Soon */ }
    public void tick(Player player)    { /* Coming Soon */ }
    public void cleanup()              { /* Coming Soon */ }
    public void spawnForNewViewer(Player wearer, Player viewer) { /* Coming Soon */ }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getPermission()  { return null; }
    public boolean hasPermission() { return false; }
}
