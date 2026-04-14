package me.pikashrey.glimzocore.features.leveling;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.events.impl.PlayerPrestigeEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PrestigeManager {

    protected final GlimzoCore plugin;

    public PrestigeManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public boolean prestige(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null) return false;
        if (data.getLevel() < LevelManager.MAX_LEVEL) return false;

        PlayerPrestigeEvent event = new PlayerPrestigeEvent(data, data.getPrestige(), data.getPrestige() + 1);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        data.setLevel(1);
        data.setExperience(0);
        data.setPrestige(data.getPrestige() + 1);
        data.getStats().incrementPrestiges();

        // Gem reward from leveling.yml: prestige.gem-reward-base + prestige.gem-reward-scaling * prestige
        long base    = plugin.getConfigManager().getLeveling().getLong("prestige.gem-reward-base",    100L);
        long scaling = plugin.getConfigManager().getLeveling().getLong("prestige.gem-reward-scaling", 50L);
        long gemReward = base + scaling * data.getPrestige();
        plugin.getGemManager().addGems(uuid, gemReward, "Prestige " + data.getPrestige() + " reward");

        return true;
    }

    public boolean canPrestige(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null && data.getLevel() >= LevelManager.MAX_LEVEL;
    }

    public String getPrestigeDisplay(int prestige) {
        if (prestige <= 0) return "";
        org.bukkit.configuration.file.FileConfiguration cfg =
                plugin.getConfigManager().getLeveling();
        int    maxStars = cfg.getInt("prestige.max-stars", 10);
        String star     = cfg.getString("prestige.star-symbol", "\u2736");
        String color    = cfg.getString("prestige.star-color", "&6");
        if (prestige > maxStars) return color + "[P" + prestige + "]";
        StringBuilder sb = new StringBuilder(color);
        for (int i = 0; i < prestige; i++) sb.append(star);
        return sb.toString();
    }
}

