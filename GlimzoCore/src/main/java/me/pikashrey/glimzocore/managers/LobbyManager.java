package me.pikashrey.glimzocore.managers;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LobbyManager {

    protected final GlimzoCore plugin;

    public LobbyManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    /** Returns the configured lobby spawn, falling back to world default if not set. */
    public Location getSpawn() {
        String worldName = plugin.getConfig().getString("spawn.world", "world");
        double x   = plugin.getConfig().getDouble("spawn.x", 0);
        double y   = plugin.getConfig().getDouble("spawn.y", 64);
        double z   = plugin.getConfig().getDouble("spawn.z", 0);
        float yaw   = (float) plugin.getConfig().getDouble("spawn.yaw", 0);
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch", 0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // Fallback to first loaded world's spawn
            world = Bukkit.getWorlds().get(0);
            return world.getSpawnLocation();
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /** Save the given location as the lobby spawn in config. */
    public void setSpawn(Location loc) {
        plugin.getConfig().set("spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("spawn.x",     loc.getX());
        plugin.getConfig().set("spawn.y",     loc.getY());
        plugin.getConfig().set("spawn.z",     loc.getZ());
        plugin.getConfig().set("spawn.yaw",   (double) loc.getYaw());
        plugin.getConfig().set("spawn.pitch", (double) loc.getPitch());
        plugin.saveConfig();
    }
}
