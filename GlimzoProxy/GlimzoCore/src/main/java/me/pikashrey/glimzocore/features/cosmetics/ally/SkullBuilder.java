package me.pikashrey.glimzocore.features.cosmetics.ally;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SkullBuilder {

    /**
     * Returns a skull item showing the given player's own skin.
     */
    public static ItemStack fromPlayer(org.bukkit.entity.Player player) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        skull.setItemMeta(meta);
        return skull;
    }

    /**
     * Returns a skull item with a custom base64 texture.
     *
     * Uses direct NMS NBT manipulation to guarantee the texture is embedded
     * in the item's NBT tag - bypassing the CraftMetaSkull reflection chain
     * which is unreliable in 1.8.8 when items are sent via packets.
     *
     * UUID is derived deterministically from the texture string so the client
     * can cache it - random UUIDs always cause Steve fallback.
     */
    public static ItemStack fromBase64(String base64Texture) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (base64Texture == null || base64Texture.isEmpty()) return skull;

        // Step 1: Set via Bukkit API (for inventory display)
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        UUID uuid = UUID.nameUUIDFromBytes(base64Texture.getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(uuid, "CustomSkull");
        profile.getProperties().put("textures", new Property("textures", base64Texture));
        try {
            Field f = meta.getClass().getDeclaredField("profile");
            f.setAccessible(true);
            f.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        skull.setItemMeta(meta);

        // Step 2: Also write NBT directly onto the NMS item
        // This guarantees the texture survives CraftItemStack.asNMSCopy()
        // and PacketPlayOutEntityEquipment serialization in 1.8.8
        try {
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(skull);
            if (nms == null) return skull;

            NBTTagCompound tag = nms.hasTag() ? nms.getTag() : new NBTTagCompound();

            // SkullOwner NBT structure:
            // SkullOwner: { Id: "uuid", Properties: { textures: [ { Value: "base64" } ] } }
            NBTTagCompound skullOwner = new NBTTagCompound();
            skullOwner.setString("Id", uuid.toString());
            skullOwner.setString("Name", "CustomSkull");

            NBTTagCompound properties = new NBTTagCompound();
            NBTTagList texturesList = new NBTTagList();
            NBTTagCompound textureEntry = new NBTTagCompound();
            textureEntry.setString("Value", base64Texture);
            texturesList.add(textureEntry);
            properties.set("textures", texturesList);
            skullOwner.set("Properties", properties);

            tag.set("SkullOwner", skullOwner);
            nms.setTag(tag);

            // Convert back to Bukkit item
            return CraftItemStack.asBukkitCopy(nms);
        } catch (Exception e) {
            e.printStackTrace();
            return skull; // fallback to reflection-only version
        }
    }
}