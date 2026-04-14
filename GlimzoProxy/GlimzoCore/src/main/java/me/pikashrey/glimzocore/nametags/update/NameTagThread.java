package me.pikashrey.glimzocore.nametags.update;

import me.pikashrey.glimzocore.GlimzoCore;

public class NameTagThread implements Runnable {

    private final GlimzoCore plugin;

    public NameTagThread(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getNameTagHandler() != null) {
            plugin.getNameTagHandler().refreshAll();
        }
    }
}

