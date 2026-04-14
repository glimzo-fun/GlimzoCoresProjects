package me.pikashrey.glimzocore.utilities.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class LocationSerialization {

    private LocationSerialization() {}

    public static String serialize(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ";"
             + loc.getX() + ";"
             + loc.getY() + ";"
             + loc.getZ() + ";"
             + loc.getYaw() + ";"
             + loc.getPitch();
    }

    public static Location deserialize(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] parts = s.split(";");
        if (parts.length < 4) return null;
        try {
            org.bukkit.World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            double x     = Double.parseDouble(parts[1]);
            double y     = Double.parseDouble(parts[2]);
            double z     = Double.parseDouble(parts[3]);
            float  yaw   = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float  pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isValid(String s) {
        return deserialize(s) != null;
    }
}

