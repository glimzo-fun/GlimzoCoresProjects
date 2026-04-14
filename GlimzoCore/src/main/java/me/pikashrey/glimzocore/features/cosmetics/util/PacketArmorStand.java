package me.pikashrey.glimzocore.features.cosmetics.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PacketArmorStand {

    private final EntityArmorStand nmsEntity;
    private final int entityId;
    private ItemStack helmetItem = null;

    public PacketArmorStand(Location loc) {
        WorldServer worldServer = ((CraftWorld) loc.getWorld()).getHandle();
        this.nmsEntity = new EntityArmorStand(worldServer);
        this.nmsEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.nmsEntity.setInvisible(true);
        this.nmsEntity.setGravity(false);

        // Default flags: no baseplate, arms on, NOT small, NOT marker
        // (subclasses can call setSmall after construction)
        DataWatcher watcher = nmsEntity.getDataWatcher();
        byte flags = 0;
        flags |= 0x02; // arms
        flags |= 0x04; // no baseplate
        // NOT 0x01 (small) and NOT 0x08 (marker) by default
        watcher.watch(10, flags);

        this.entityId = nmsEntity.getId();
    }

    public void setSmall(boolean small) {
        DataWatcher watcher = nmsEntity.getDataWatcher();
        byte flags = watcher.getByte(10);
        if (small) flags |= 0x01;
        else flags &= ~0x01;
        watcher.watch(10, flags);
    }

    public void setInvisible(boolean invisible) {
        nmsEntity.setInvisible(invisible);
    }

    public void setHelmet(ItemStack item) {
        this.helmetItem = item;
        nmsEntity.setEquipment(4, CraftItemStack.asNMSCopy(item));
    }

    public void setChestplate(ItemStack item) {
        nmsEntity.setEquipment(3, CraftItemStack.asNMSCopy(item));
    }

    public void setHeadPose(float x, float y, float z) {
        nmsEntity.setHeadPose(new Vector3f(x, y, z));
    }

    public void setBodyPose(float x, float y, float z) {
        nmsEntity.setBodyPose(new Vector3f(x, y, z));
    }

    public void setLocation(Location loc) {
        nmsEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public void spawn(Player player) {
        sendPacket(player, new PacketPlayOutSpawnEntityLiving(nmsEntity));
        sendPacket(player, new PacketPlayOutEntityMetadata(entityId, nmsEntity.getDataWatcher(), true));
        // Explicitly send helmet equipment after spawn
        if (helmetItem != null) {
            sendPacket(player, new PacketPlayOutEntityEquipment(entityId, 4, CraftItemStack.asNMSCopy(helmetItem)));
        }
    }

    public void teleport(Player player, Location loc) {
        nmsEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        sendPacket(player, new PacketPlayOutEntityTeleport(nmsEntity));
    }

    public void sendMetadata(Player player) {
        sendPacket(player, new PacketPlayOutEntityMetadata(entityId, nmsEntity.getDataWatcher(), true));
    }

    public void sendEquipment(Player player, int slot, ItemStack item) {
        sendPacket(player, new PacketPlayOutEntityEquipment(entityId, slot, CraftItemStack.asNMSCopy(item)));
    }

    public void destroy(Player player) {
        sendPacket(player, new PacketPlayOutEntityDestroy(entityId));
    }

    public int getEntityId() { return entityId; }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
