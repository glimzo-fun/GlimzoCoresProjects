package me.pikashrey.glimzocore.tasks;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState;

public class AutoSaveTask implements Runnable {

    protected final GlimzoCore plugin;

    public AutoSaveTask(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int saved = GlobalPlayer.saveAll();

        int cosmeticCount = 0;
        if (plugin.getCosmeticManager() != null) {
            for (me.pikashrey.glimzocore.api.player.PlayerData data : GlobalPlayer.getAll()) {
                PlayerCosmeticState state = plugin.getCosmeticManager().getState(data.getUuid());
                if (state != null && state.isDirty()) {
                    plugin.getCosmeticManager().savePlayer(data.getUuid());
                    state.clearDirty();
                    cosmeticCount++;
                }
            }
        }

        if (saved > 0 || cosmeticCount > 0) {
            plugin.log("&7[AutoSave] Saved " + saved + " dirty player(s), " + cosmeticCount + " dirty cosmetic state(s).");
        }
    }
}
