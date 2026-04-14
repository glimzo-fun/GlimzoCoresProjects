package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.Ally;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPart;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class KittyAlly extends Ally {

    private KittyOcelotPart kittyPart;

    // Smoothed follow position (lerped each tick)
    private double smoothX, smoothZ;
    private boolean smoothInit = false;

    public KittyAlly(Player owner) { super(AllyType.KITTY, owner); }

    @Override public boolean isFloating()      { return false; }
    @Override public double  getHeightOffset() { return 0.0; }
    @Override public double  getSideOffset()   { return 0.9; }
    @Override public double  getBehindOffset() { return 0.3; }

    @Override
    protected List<AllyPart> buildParts(Location root) {
        List<AllyPart> list = new ArrayList<>();
        kittyPart = new KittyOcelotPart(root, owner);
        smoothX = root.getX();
        smoothZ = root.getZ();
        smoothInit = true;
        list.add(kittyPart);
        return list;
    }

    @Override
    public void onTick() {
        if (kittyPart == null) return;
        Ocelot ocelot = kittyPart.getOcelot();
        if (ocelot == null || !ocelot.isValid()) return;

        // Target = base class interpolated position (currentX/Z already lerped)
        double targetX = currentX;
        double targetZ = currentZ;

        if (!smoothInit) {
            smoothX = targetX; smoothZ = targetZ; smoothInit = true;
        }

        // Lerp smoothed position toward target
        double speed = 0.20;
        smoothX += (targetX - smoothX) * speed;
        smoothZ += (targetZ - smoothZ) * speed;

        // Find the safe Y at the smoothed XZ - highest non-air block + 1
        World world = owner.getWorld();
        int bx = (int) Math.floor(smoothX);
        int bz = (int) Math.floor(smoothZ);
        // getHighestBlockYAt returns the Y of the topmost solid block.
        // We add 1.0 so Kitty stands ON TOP of that block, not inside it.
        double safeY = world.getHighestBlockYAt(bx, bz) + 1.0;

        double dist = Math.sqrt((targetX-smoothX)*(targetX-smoothX)+(targetZ-smoothZ)*(targetZ-smoothZ));

        if (dist > 8.0) {
            // Snap on large distance (teleport)
            smoothX = targetX; smoothZ = targetZ;
        }

        // Face the direction toward player
        float yaw = 0;
        double dx = owner.getLocation().getX() - smoothX;
        double dz = owner.getLocation().getZ() - smoothZ;
        if (Math.abs(dx) > 0.01 || Math.abs(dz) > 0.01) {
            yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        }

        Location dest = new Location(world, smoothX, safeY, smoothZ, yaw, 0);
        ocelot.teleport(dest);
        ocelot.setVelocity(new Vector(0, 0, 0));
    }

    private static class KittyOcelotPart extends AllyPart {
        private Ocelot ocelot;

        KittyOcelotPart(Location spawnLoc, Player owner) {
            super(spawnLoc, 0, 0, 0, false, false);
            Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.OCELOT);
            if (entity instanceof Ocelot) {
                ocelot = (Ocelot) entity;
                Ocelot.Type[] types = { Ocelot.Type.RED_CAT, Ocelot.Type.SIAMESE_CAT };
                ocelot.setCatType(types[(int)(Math.random() * types.length)]);
                ocelot.setTamed(false);
                ocelot.setCustomName("§r");
                ocelot.setCustomNameVisible(false);
                ocelot.setRemoveWhenFarAway(false);
                ocelot.setVelocity(new Vector(0, 0, 0));
                me.pikashrey.glimzocore.features.cosmetics.util.NmsEntityUtil.suppressAI(ocelot);
            }
        }

        @Override public void spawn(Player player) {}
        @Override public void teleport(Player player, Location location) {} // KittyAlly.onTick handles it
        @Override public void destroy(Player player) {
            if (ocelot != null) { ocelot.remove(); ocelot = null; }
        }
        Ocelot getOcelot() { return ocelot; }
    }
}
