package me.pikashrey.glimzobedwars;

import me.pikashrey.glimzobedwars.config.BWConfig;
import me.pikashrey.glimzobedwars.listeners.BedWarsListener;
import me.pikashrey.glimzobedwars.stats.BWStatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GlimzoBedWars extends JavaPlugin {

    private static GlimzoBedWars instance;
    private BWStatsManager statsManager;
    private BWConfig bwConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Check GlimzoCore is present
        if (getServer().getPluginManager().getPlugin("GlimzoCore") == null) {
            getLogger().severe("GlimzoCore not found! Disabling GlimzoBedWars.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Check BedWars1058 is present
        if (getServer().getPluginManager().getPlugin("BedWars1058") == null) {
            getLogger().severe("BedWars1058 not found! Disabling GlimzoBedWars.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        bwConfig     = new BWConfig(this);
        statsManager = new BWStatsManager(this);

        getServer().getPluginManager().registerEvents(new BedWarsListener(this), this);

        getLogger().info("GlimzoBedWars enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GlimzoBedWars disabled.");
    }

    public static GlimzoBedWars getInstance() { return instance; }
    public BWStatsManager getStatsManager()   { return statsManager; }
    public BWConfig getBWConfig()              { return bwConfig; }
}
