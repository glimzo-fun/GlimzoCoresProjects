package me.pikashrey.glimzocore.hologram;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class HologramLine {

    public static final double LINE_HEIGHT = 0.25; // vertical gap between lines

    private String text;
    private Location location;
    private EntityArmorStand armorStand;

    public HologramLine(String text, Location location) {
        this.text     = text;
        this.location = location.clone();
        buildEntity();
    }

    //  Build

    private void buildEntity() {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        armorStand = new EntityArmorStand(world);
        armorStand.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);

        applyFlags();
    }

    private void applyFlags() {
        // Invisible
        armorStand.setInvisible(true);
        // No gravity
        armorStand.setGravity(false);
        // No base plate
        armorStand.setBasePlate(false);
        // Small armor stand (smaller hitbox)
        armorStand.setSmall(true);
        // Custom name tag
        armorStand.setCustomName(text != null ? text : " ");
        armorStand.setCustomNameVisible(!isEmpty());
    }

    //  Spawn / Destroy for individual players

    public void spawn(Player player) {
        PacketPlayOutSpawnEntityLiving packet =
                new PacketPlayOutSpawnEntityLiving(armorStand);
        sendPacket(player, packet);

        // Send metadata so the client knows about invisible/no-gravity flags
        PacketPlayOutEntityMetadata metaPacket =
                new PacketPlayOutEntityMetadata(
                        armorStand.getId(),
                        armorStand.getDataWatcher(),
                        true // send all entries
                );
        sendPacket(player, metaPacket);
    }

    public void destroy(Player player) {
        sendPacket(player, new PacketPlayOutEntityDestroy(armorStand.getId()));
    }

    //  Text update (hot-swap without full respawn)

    /**
     * Updates the text of this line for all given viewers without despawning.
     */
    public void updateText(String newText, Iterable<Player> viewers) {
        this.text = newText;
        armorStand.setCustomName(newText != null ? newText : " ");
        armorStand.setCustomNameVisible(!isEmpty());

        PacketPlayOutEntityMetadata metaPacket =
                new PacketPlayOutEntityMetadata(
                        armorStand.getId(),
                        armorStand.getDataWatcher(),
                        true
                );
        for (Player p : viewers) {
            if (p.isOnline()) sendPacket(p, metaPacket);
        }
    }
    //  Helpers

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public boolean isEmpty() {
        return text == null || text.trim().isEmpty();
    }

    //  Getters

    public String getText()           { return text; }
    public Location getLocation()     { return location.clone(); }
    public int getEntityId()          { return armorStand.getId(); }
    public EntityArmorStand getArmorStand() { return armorStand; }
}
