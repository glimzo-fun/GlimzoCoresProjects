package me.pikashrey.glimzocore.utilities.serialization;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public final class BukkitSerialization {

    private BukkitSerialization() {}

    /** Serialize an array of ItemStacks to a Base64 string. */
    public static String itemsToBase64(ItemStack[] items) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(bos)) {
            oos.writeInt(items.length);
            for (ItemStack item : items) oos.writeObject(item);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /** Deserialize a Base64 string back to an ItemStack array. */
    public static ItemStack[] itemsFromBase64(String data) {
        if (data == null || data.isEmpty()) return new ItemStack[0];
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bis)) {
            int length = ois.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) ois.readObject();
            }
            return items;
        } catch (Exception e) {
            return new ItemStack[0];
        }
    }

    /** Serialize a single ItemStack. */
    public static String itemToBase64(ItemStack item) {
        return itemsToBase64(new ItemStack[]{ item });
    }

    /** Deserialize a single ItemStack. */
    public static ItemStack itemFromBase64(String data) {
        ItemStack[] items = itemsFromBase64(data);
        return items.length > 0 ? items[0] : null;
    }
}

