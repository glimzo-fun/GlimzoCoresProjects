package me.pikashrey.glimzocore.features.cosmetics.ally;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AllyPart {

    private final EntityArmorStand nmsEntity;
    private final int              entityId;

    public final double dx;
    public final double dy;
    public final double dz;

    public AllyPart(Location spawnLoc, double dx, double dy, double dz,
                    boolean gravity, boolean small) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;

        WorldServer world = ((CraftWorld) spawnLoc.getWorld()).getHandle();
        this.nmsEntity = new EntityArmorStand(world);
        this.nmsEntity.setLocation(
                spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(),
                spawnLoc.getYaw(), spawnLoc.getPitch());

        // THE FIX: In 1.8.8 NMS the setter methods (setSmall, setBasePlate, etc.)
        // do update the DataWatcher internally, BUT the DataWatcher byte at index 10
        // must be written directly to guarantee the value is flushed before the
        // spawn packet is built.  PacketArmorStand (used by working wings) does
        // exactly this - we must mirror it here.
        //
        // Armor-stand DataWatcher byte 10 flag bits:
        //   0x01 = small
        //   0x02 = has arms
        //   0x04 = no base plate
        //   0x08 = marker (zero hitbox)
        //
        // We want: small=param, arms=true (for hand items), no baseplate, no marker.
        byte flags = 0;
        if (small)   flags |= 0x01;
        flags       |= 0x02; // arms always on so hand items render
        flags       |= 0x04; // no base plate

        DataWatcher watcher = nmsEntity.getDataWatcher();
        watcher.watch(10, flags);

        // Entity-level flag byte 0: bit 0x20 = invisible
        byte entityFlags = watcher.getByte(0);
        entityFlags |= 0x20; // invisible
        watcher.watch(0, entityFlags);

        // Gravity: NMS EntityArmorStand overrides setGravity but we call it anyway
        // for completeness - the DataWatcher already controls rendering.
        nmsEntity.setGravity(gravity);

        this.entityId = nmsEntity.getId();
    }

    // --- Equipment ---

    public void setHelmet(ItemStack item) {
        nmsEntity.setEquipment(4, CraftItemStack.asNMSCopy(item));
    }

    public void setChestplate(ItemStack item) {
        nmsEntity.setEquipment(3, CraftItemStack.asNMSCopy(item));
    }

    public void setLeggings(ItemStack item) {
        nmsEntity.setEquipment(2, CraftItemStack.asNMSCopy(item));
    }

    public void setBoots(ItemStack item) {
        nmsEntity.setEquipment(1, CraftItemStack.asNMSCopy(item));
    }

    public void setHandItem(ItemStack item) {
        nmsEntity.setEquipment(0, CraftItemStack.asNMSCopy(item));
    }

    // --- Poses ---

    public void setHeadPose(float x, float y, float z) {
        nmsEntity.setHeadPose(new Vector3f(x, y, z));
    }

    public void setBodyPose(float x, float y, float z) {
        nmsEntity.setBodyPose(new Vector3f(x, y, z));
    }

    public void setLeftArmPose(float x, float y, float z) {
        nmsEntity.setLeftArmPose(new Vector3f(x, y, z));
    }

    public void setRightArmPose(float x, float y, float z) {
        nmsEntity.setRightArmPose(new Vector3f(x, y, z));
    }

    public void setLeftLegPose(float x, float y, float z) {
        nmsEntity.setLeftLegPose(new Vector3f(x, y, z));
    }

    public void setRightLegPose(float x, float y, float z) {
        nmsEntity.setRightLegPose(new Vector3f(x, y, z));
    }

    // --- Lifecycle ---

    public void spawn(Player player) {
        send(player, new PacketPlayOutSpawnEntityLiving(nmsEntity));
        send(player, new PacketPlayOutEntityMetadata(entityId, nmsEntity.getDataWatcher(), true));
        sendAllEquipment(player);
    }

    public void teleport(Player player, Location loc) {
        nmsEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        send(player, new PacketPlayOutEntityTeleport(nmsEntity));
    }

    public void updateMetadata(Player player) {
        send(player, new PacketPlayOutEntityMetadata(entityId, nmsEntity.getDataWatcher(), true));
    }

    /**
     * Broadcast metadata update to all players within 64 blocks of the given location.
     * Use this for animations (wing flap, head bob) so other players see them too.
     */
    public void broadcastMetadata(org.bukkit.Location center) {
        PacketPlayOutEntityMetadata packet =
                new PacketPlayOutEntityMetadata(entityId, nmsEntity.getDataWatcher(), true);
        for (org.bukkit.entity.Player viewer : center.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(center) <= 4096) {
                send(viewer, packet);
            }
        }
    }

    public void destroy(Player player) {
        send(player, new PacketPlayOutEntityDestroy(entityId));
    }

    public int getEntityId() { return entityId; }

    // --- Internals ---

    private void sendAllEquipment(Player player) {
        for (int slot = 0; slot <= 4; slot++) {
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = nmsEntity.getEquipment(slot);
            if (nmsItem != null && nmsItem.getItem() != null) {
                send(player, new PacketPlayOutEntityEquipment(entityId, slot, nmsItem));
            }
        }
    }

    private void send(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}