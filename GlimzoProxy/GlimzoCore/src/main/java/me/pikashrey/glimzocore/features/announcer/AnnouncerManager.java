package me.pikashrey.glimzocore.features.announcer;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Config-driven announcer - reads from announcer.yml.
 * Each announcement is a block of lines broadcast together.
 * Supports random or sequential rotation.
 */
public class AnnouncerManager {

    protected final GlimzoCore plugin;

    // List of announcements; each is a list of lines
    private final List<List<String>> announcements = new ArrayList<>();
    private int     currentIndex  = 0;
    private boolean random        = false;
    private int     intervalTicks = 1200;
    private BukkitTask task;
    private final Random rng = new Random();

    public AnnouncerManager(GlimzoCore plugin) {
        this.plugin = plugin;
        load();
    }


    public void load() {
        announcements.clear();
        currentIndex = 0;

        File file = new File(plugin.getDataFolder(), "announcer.yml");
        if (!file.exists()) plugin.saveResource("announcer.yml", false);

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        intervalTicks = cfg.getInt("interval-ticks", 1200);
        random        = cfg.getBoolean("random", false);

        List<?> list = cfg.getList("announcements");
        if (list != null) {
            for (Object entry : list) {
                if (!(entry instanceof ConfigurationSection)) {
                    // Also handle raw List<String> entries (flat format)
                    continue;
                }
                ConfigurationSection sec = (ConfigurationSection) entry;
                List<String> lines = sec.getStringList("lines");
                if (!lines.isEmpty()) announcements.add(lines);
            }
        }

        // Fallback: try reading as a list of ConfigurationSection via getConfigurationSection keys
        if (announcements.isEmpty()) {
            ConfigurationSection sec = cfg.getConfigurationSection("announcements");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    List<String> lines = sec.getStringList(key + ".lines");
                    if (!lines.isEmpty()) announcements.add(lines);
                }
            }
        }

        // Last resort default
        if (announcements.isEmpty()) {
            List<String> def = new ArrayList<>();
            def.add("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            def.add("    &a&lGlimzo Network &7- play.glimzo.fun");
            def.add("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            announcements.add(def);
        }

        plugin.log("&a[Announcer] Loaded " + announcements.size() + " announcements (interval: "
                + intervalTicks + " ticks, random: " + random + ").");
    }


    public void start() {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastNext, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public void reload() {
        stop();
        load();
        start();
    }


    public void broadcastNext() {
        if (announcements.isEmpty()) return;

        List<String> lines;
        if (random) {
            lines = announcements.get(rng.nextInt(announcements.size()));
        } else {
            lines = announcements.get(currentIndex % announcements.size());
            currentIndex = (currentIndex + 1) % announcements.size();
        }

        String translated = buildMessage(lines);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(translated);
        }
    }

    private String buildMessage(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(CC.translate(lines.get(i)));
        }
        return sb.toString();
    }

    public List<List<String>> getAnnouncements() {
        return Collections.unmodifiableList(announcements);
    }
}
