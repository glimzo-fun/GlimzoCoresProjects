package me.pikashrey.glimzocore.features.cosmetics.wings;

import me.pikashrey.glimzocore.features.cosmetics.util.PacketArmorStand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WingSegment {

    public enum Side { LEFT, RIGHT }

    private final PacketArmorStand stand;
    private final Side   side;
    private final int    segmentIndex; // 0 = bottom, 1 = mid, 2 = top
    private final double sideSpread;   // how far out from center per segment
    private final double yRise;        // how high up per segment
    private final double backOffset;   // how far behind the player

    public WingSegment(Location spawnLoc, Side side, int segmentIndex,
                       double sideSpread, double yRise, double backOffset) {
        this.stand        = new PacketArmorStand(spawnLoc);
        this.side         = side;
        this.segmentIndex = segmentIndex;
        this.sideSpread   = sideSpread;
        this.yRise        = yRise;
        this.backOffset   = backOffset;

        this.stand.setSmall(true);
        this.stand.setInvisible(true);
    }

    public void setSkull(ItemStack skull) {
        stand.setHelmet(skull);
    }

    public void spawn(Player player) {
        stand.spawn(player);
    }

    public void destroy(Player player) {
        stand.destroy(player);
    }

    public void update(Player player) {
        stand.teleport(player, calculateLocation(player));
    }

    public Location calculateLocation(Player player) {
        Location origin = player.getLocation().clone().add(0, 1.0, 0);
        double yaw = Math.toRadians(origin.getYaw());

        // Unit vectors
        double sinYaw  = Math.sin(yaw);
        double cosYaw  = Math.cos(yaw);

        // Behind the player
        double bx = sinYaw  * backOffset;
        double bz = -cosYaw * backOffset;

        // Perpendicular (left or right)
        double spread = sideSpread * (segmentIndex + 1);
        double px, pz;
        if (side == Side.LEFT) {
            px = cosYaw  * spread;
            pz = sinYaw  * spread;
        } else {
            px = -cosYaw * spread;
            pz = -sinYaw * spread;
        }

        double finalY = yRise * segmentIndex;

        Location loc = origin.clone().add(bx + px, finalY, bz + pz);
        // Face outward from player
        float segYaw = (float) (origin.getYaw() + (side == Side.LEFT ? 90 : -90));
        loc.setYaw(segYaw);
        return loc;
    }
}
