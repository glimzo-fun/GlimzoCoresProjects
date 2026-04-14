package me.pikashrey.glimzocore.managers;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    protected final GlimzoCore plugin;

    private FileConfiguration settings;
    private FileConfiguration messages;
    private FileConfiguration leveling;
    private FileConfiguration seasons;
    private FileConfiguration social;
    private FileConfiguration lore;
    private FileConfiguration cosmetics;
    private FileConfiguration nicks;
    private FileConfiguration store;

    public ConfigManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        settings  = plugin.getConfig();
        messages  = loadOrCreate("messages.yml");
        leveling  = loadOrCreate("leveling.yml");
        seasons   = loadOrCreate("seasons.yml");
        social    = loadOrCreate("social.yml");
        lore      = loadOrCreate("lore.yml");
        cosmetics = loadOrCreate("cosmetics.yml");
        nicks     = loadOrCreate("nicks.yml");
        store     = loadOrCreate("store.yml");
    }

    public void reload() {
        plugin.reloadConfig();
        settings  = plugin.getConfig();
        messages  = loadOrCreate("messages.yml");
        leveling  = loadOrCreate("leveling.yml");
        seasons   = loadOrCreate("seasons.yml");
        social    = loadOrCreate("social.yml");
        lore      = loadOrCreate("lore.yml");
        cosmetics = loadOrCreate("cosmetics.yml");
        nicks     = loadOrCreate("nicks.yml");
        store     = loadOrCreate("store.yml");
    }

    public FileConfiguration getSettings()  { return settings != null ? settings : plugin.getConfig(); }
    public FileConfiguration getMessages()  { return messages; }
    public FileConfiguration getLeveling()  { return leveling; }
    public FileConfiguration getSeasons()   { return seasons; }
    public FileConfiguration getSocial()    { return social; }
    public FileConfiguration getLore()      { return lore; }
    public FileConfiguration getNicks()     { return nicks; }
    public FileConfiguration getCosmetics() { return cosmetics; }
    public FileConfiguration getStore()     { return store != null ? store : new org.bukkit.configuration.file.YamlConfiguration(); }

    public String getMessage(String path, String fallback) {
        if (messages == null) return fallback;
        String val = messages.getString(path);
        return val != null ? val : fallback;
    }

    public String getMessage(String path) {
        return getMessage(path, "");
    }

    private FileConfiguration loadOrCreate(String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            try {
                plugin.saveResource(filename, false);
            } catch (IllegalArgumentException e) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (Exception ex) {
                    plugin.log("&c[Config] Failed to create " + filename + ": " + ex.getMessage());
                    return new YamlConfiguration();
                }
            }
        }
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(file);
        InputStream defStream = plugin.getResource(filename);
        if (defStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            loaded.setDefaults(defaults);
        }
        return loaded;
    }
}
