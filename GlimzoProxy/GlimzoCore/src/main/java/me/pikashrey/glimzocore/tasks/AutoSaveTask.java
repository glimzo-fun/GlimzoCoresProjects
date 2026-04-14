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
        // PlayerData: dirty-flag check skips unchanged players (~80% reduction in writes).
        int saved = GlobalPlayer.saveAll();

        // Cosmetics: only write states that have actually changed since the last save.
        // Previously every online player's cosmetic row was written every cycle
        // regardless of whether anything changed - this fixes that.
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
            plugin.log("&7[AutoSave] Saved " + saved + " dirty player(s), "
                    + cosmeticCount + " dirty cosmetic state(s).");
        }
    }
}
