package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.events.impl.*;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.achievements.AchievementManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class AchievementListener implements Listener {

    private final GlimzoCore plugin;

    public AchievementListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    private AchievementManager am() { return plugin.getAchievementManager(); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            am().checkLogin(uuid);
            am().checkLoginStreak(uuid, 1);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevelUp(PlayerLevelUpEvent event) {
        am().checkLevelReached(event.getPlayerData().getUuid(), event.getNewLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrestige(PlayerPrestigeEvent event) {
        am().checkPrestige(event.getPlayerData().getUuid());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClanCreate(ClanCreateEvent event) {
        if (event.getPlayerData() == null) return;
        UUID founder = event.getPlayerData().getUuid();
        am().checkClanCreated(founder);
        am().checkClanJoined(founder);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClanJoin(ClanJoinEvent event) {
        if (event.getPlayerData() == null) return;
        am().checkClanJoined(event.getPlayerData().getUuid());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCoins(CoinsTransactionEvent event) {
        if (event.getTransaction().getType() != me.pikashrey.glimzocore.api.economy.Transaction.Type.ADD) return;
        if (event.getPlayerData() == null) return;
        UUID uuid = event.getPlayerData().getUuid();
        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) am().checkCoinsEarned(uuid, data.getStats().getTotalCoinsEarned());
    }
}
