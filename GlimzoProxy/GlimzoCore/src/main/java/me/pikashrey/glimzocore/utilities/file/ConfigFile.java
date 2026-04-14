package me.pikashrey.glimzocore.utilities.file;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigFile {

    private final GlimzoCore      plugin;
    private final String          fileName;
    private       File            file;
    private       FileConfiguration config;

    public ConfigFile(GlimzoCore plugin, String fileName) {
        this.plugin   = plugin;
        this.fileName = fileName;
    }

    /** Load (or create) the file. Copies the default from resources if absent. */
    public void load() {
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            // Copy from JAR resources if available, otherwise create blank
            InputStream in = plugin.getResource(fileName);
            if (in != null) {
                plugin.saveResource(fileName, false);
            } else {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    plugin.log("&c[ConfigFile] Could not create " + fileName + ": " + e.getMessage());
                }
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from resources
        InputStream defStream = plugin.getResource(fileName);
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
        }
    }

    /** Save the in-memory config back to disk. */
    public void save() {
        if (config == null || file == null) return;
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.log("&c[ConfigFile] Could not save " + fileName + ": " + e.getMessage());
        }
    }

    /** Reload from disk, discarding in-memory changes. */
    public void reload() {
        if (file == null) load();
        else config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() { return config; }
    public File              getFile()   { return file; }
    public String            getName()   { return fileName; }
}

