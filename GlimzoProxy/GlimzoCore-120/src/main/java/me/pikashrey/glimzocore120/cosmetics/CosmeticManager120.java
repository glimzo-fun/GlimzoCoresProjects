package me.pikashrey.glimzocore120.cosmetics;

import me.pikashrey.glimzocore120.GlimzoCore120;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticManager120 {

    private final GlimzoCore120 plugin;

    // Active aura tasks per player
    private final Map<UUID, BukkitTask> auraTasks    = new HashMap<>();
    // Active floating name displays per player
    private final Map<UUID, TextDisplay> nameDisplays = new HashMap<>();

    public CosmeticManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }


    public void startAura(Player player, AuraType type) {
        stopAura(player); // stop any existing aura first
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stopAura(player);
                return;
            }
            spawnAuraParticles(player, type);
        }, 0L, 5L);
        auraTasks.put(player.getUniqueId(), task);
    }

    public void stopAura(Player player) {
        BukkitTask task = auraTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    private void spawnAuraParticles(Player player, AuraType type) {
        Location loc = player.getLocation().add(0, 0.1, 0);
        switch (type) {
            case FLAME -> player.getWorld().spawnParticle(
                    Particle.FLAME, loc, 5, 0.3, 0.1, 0.3, 0.02);
            case ENCHANT -> player.getWorld().spawnParticle(
                    Particle.ENCHANTMENT_TABLE, loc, 10, 0.5, 0.5, 0.5, 0.1);
            case HEART -> player.getWorld().spawnParticle(
                    Particle.HEART, loc, 3, 0.3, 0.3, 0.3, 0);
            case SNOWFLAKE -> player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE, loc, 8, 0.3, 0.3, 0.3, 0.05);
            case DRAGON -> player.getWorld().spawnParticle(
                    Particle.DRAGON_BREATH, loc, 5, 0.3, 0.3, 0.3, 0.02);
        }
    }


    public void spawnNameDisplay(Player player) {
        removeNameDisplay(player);

        String rankPrefix = plugin.getRankManager().getPrefix(player);
        Location loc      = player.getLocation().add(0, 2.2, 0);

        TextDisplay display = player.getWorld().spawn(loc, TextDisplay.class, d -> {
            d.text(Component.text(rankPrefix + " ")
                    .append(Component.text(player.getName(), NamedTextColor.WHITE)));
            d.setBillboard(Display.Billboard.CENTER);
            d.setSeeThrough(false);
            d.setDefaultBackground(false);
        });

        nameDisplays.put(player.getUniqueId(), display);

        // Make display follow player every tick
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || !nameDisplays.containsKey(player.getUniqueId())) {
                display.remove();
                task.cancel();
                return;
            }
            display.teleport(player.getLocation().add(0, 2.2, 0));
        }, 1L, 1L);
    }

    public void removeNameDisplay(Player player) {
        TextDisplay display = nameDisplays.remove(player.getUniqueId());
        if (display != null) display.remove();
    }

    public void removeAll(Player player) {
        stopAura(player);
        removeNameDisplay(player);
    }

    public enum AuraType {
        FLAME, ENCHANT, HEART, SNOWFLAKE, DRAGON
    }
}