package me.pikashrey.glimzocore.tasks;

import me.pikashrey.glimzocore.GlimzoCore;

/**
* this class is js shit, no use of this one basically
 */
public class AnnouncerTask implements Runnable {

    protected final GlimzoCore plugin;

    public AnnouncerTask(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // No-op: AnnouncerManager handles its own scheduling internally.
    }
}
