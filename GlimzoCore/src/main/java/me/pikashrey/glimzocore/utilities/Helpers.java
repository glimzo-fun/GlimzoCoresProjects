package me.pikashrey.glimzocore.utilities;

import me.pikashrey.glimzocore.GlimzoCore;

public class Helpers {
    private final GlimzoCore plugin;

    public Helpers(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public boolean isLobbyWorld(org.bukkit.World world) {
        String defaultWorldName = plugin.getConfig().getString("default-world-name", "world");
        return world != null && world.getName().equals(defaultWorldName);
    }
}
