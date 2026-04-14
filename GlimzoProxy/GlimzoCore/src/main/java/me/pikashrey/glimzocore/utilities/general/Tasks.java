package me.pikashrey.glimzocore.utilities.general;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Bukkit;

/**
 * Static helpers for scheduling Bukkit tasks without boilerplate.
 */
public final class Tasks {

    private Tasks() {}

    public static void sync(Runnable r) {
        Bukkit.getScheduler().runTask(GlimzoCore.getInstance(), r);
    }

    public static void async(Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(GlimzoCore.getInstance(), r);
    }

    public static void syncLater(Runnable r, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(GlimzoCore.getInstance(), r, delayTicks);
    }

    public static void asyncLater(Runnable r, long delayTicks) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(GlimzoCore.getInstance(), r, delayTicks);
    }

    public static org.bukkit.scheduler.BukkitTask syncTimer(Runnable r, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(GlimzoCore.getInstance(), r, delay, period);
    }

    public static org.bukkit.scheduler.BukkitTask asyncTimer(Runnable r, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(GlimzoCore.getInstance(), r, delay, period);
    }
}

