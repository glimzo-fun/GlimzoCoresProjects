package me.pikashrey.glimzocore.tasks;

import me.pikashrey.glimzocore.GlimzoCore;

/** Ticks the SeasonManager to check expiry and award passive XP. */
public class SeasonTickTask implements Runnable {

    protected final GlimzoCore plugin;

    public SeasonTickTask(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getSeasonManager() != null) {
            plugin.getSeasonManager().tick();
        }
    }
}

