package me.pikashrey.glimzocore.features.cosmetics.ally;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Ally {

    protected final AllyType      type;
    protected final Player        owner;
    protected final List<AllyPart> parts = new ArrayList<>();

    // Smooth follow interpolation
    protected double  currentX, currentY, currentZ;
    protected boolean initialized = false;
    protected int     tickCount   = 0;

    public Ally(AllyType type, Player owner) {
        this.type  = type;
        this.owner = owner;
    }

    /** Build and return all AllyParts. Called once on spawn. */
    protected abstract List<AllyPart> buildParts(Location rootLoc);

    /** Called every tick - subclasses can override for animations. */
    public void onTick() {}

    /** Side offset from player (positive = player's right). Default 0.8. */
    public double getSideOffset()    { return 0.8; }

    /** Height offset from ground. Default 0.1 (ground-level). Override for floaters. */
    public double getHeightOffset()  { return 0.1; }

    /** Distance behind the player. Default 0.8. */
    public double getBehindOffset()  { return 0.8; }

    /** Whether this ally hovers/floats (no gravity, smoother vertical interpolation). */
    public boolean isFloating()      { return false; }

    public final void spawn() {
        Location root = getRootLocation();
        currentX = root.getX();
        currentY = root.getY();
        currentZ = root.getZ();
        initialized = true;

        List<AllyPart> built = buildParts(root);
        parts.addAll(built);
        // Spawn for the owner AND all nearby players
        for (Player viewer : owner.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(owner.getLocation()) <= 4096) {
                for (AllyPart part : parts) part.spawn(viewer);
            }
        }
    }

    /** Call when a new player comes into range - show them the ally. */
    public final void spawnForViewer(Player viewer) {
        if (!initialized) return;
        for (AllyPart part : parts) part.spawn(viewer);
    }

    public final void tick() {
        if (!initialized) return;
        tickCount++;

        // Smooth interpolation toward target position
        Location target = getRootLocation();
        double speed = isFloating() ? 0.18 : 0.22;

        currentX += (target.getX() - currentX) * speed;
        currentY += (target.getY() - currentY) * speed;
        currentZ += (target.getZ() - currentZ) * speed;

        // Snap if too far (teleport case)
        double distSq = (target.getX() - currentX) * (target.getX() - currentX)
                      + (target.getZ() - currentZ) * (target.getZ() - currentZ);
        if (distSq > 16) {
            currentX = target.getX();
            currentY = target.getY();
            currentZ = target.getZ();
        }

        float yaw = owner.getLocation().getYaw();

        // Cache the center location once per tick instead of allocating a new
        // Location object for every viewer × every part (was 800+ allocations/tick).
        Location centerCache = new Location(owner.getWorld(), currentX, currentY, currentZ);
        java.util.List<Player> nearbyViewers = new java.util.ArrayList<>();
        for (Player viewer : owner.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(centerCache) <= 4096) {
                nearbyViewers.add(viewer);
            }
        }

        for (AllyPart part : parts) {
            Location partLoc = applyPartOffset(part, yaw);
            for (Player viewer : nearbyViewers) {
                part.teleport(viewer, partLoc);
            }
        }

        onTick();
    }

    public final void remove() {
        // Destroy for all online players (prevents ghost armor stands)
        for (Player viewer : owner.getWorld().getPlayers()) {
            for (AllyPart part : parts) part.destroy(viewer);
        }
        parts.clear();
        initialized = false;
    }

    protected Location applyPartOffset(AllyPart part, float yaw) {
        double rad = Math.toRadians(yaw);
        double sinYaw = Math.sin(rad);
        double cosYaw = Math.cos(rad);

        // dx is side (perpendicular), dz is forward/back
        double worldX = currentX + (cosYaw * part.dx) - (sinYaw * part.dz);
        double worldY = currentY + part.dy;
        double worldZ = currentZ + (sinYaw * part.dx) + (cosYaw * part.dz);

        Location loc = owner.getLocation().clone();
        loc.setX(worldX);
        loc.setY(worldY);
        loc.setZ(worldZ);
        loc.setYaw(yaw);
        return loc;
    }

    protected Location getRootLocation() {
        Location playerLoc = owner.getLocation().clone();
        double yaw = Math.toRadians(playerLoc.getYaw());
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);

        double side   = getSideOffset();
        double behind = getBehindOffset();

        playerLoc.add(
            cosYaw * side - sinYaw * (-behind),
            getHeightOffset(),
            sinYaw * side + cosYaw * (-behind)
        );
        return playerLoc;
    }

    public AllyType getType()   { return type; }
    public Player   getOwner()  { return owner; }
}
