package me.pikashrey.glimzobedwars.listeners;

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.economy.coins.CoinManager;
import me.pikashrey.glimzocore.features.leveling.LevelManager;
import me.pikashrey.glimzobedwars.GlimzoBedWars;
import me.pikashrey.glimzobedwars.config.BWConfig;
import me.pikashrey.glimzobedwars.stats.BWPlayerStats;
import me.pikashrey.glimzobedwars.stats.BWStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class BedWarsListener implements Listener {

    private final GlimzoBedWars plugin;
    private final BWStatsManager statsManager;
    private final BWConfig cfg;
    private final CoinManager coinManager;
    private final LevelManager levelManager;

    public BedWarsListener(GlimzoBedWars plugin) {
        this.plugin       = plugin;
        this.statsManager = plugin.getStatsManager();
        this.cfg          = plugin.getBWConfig();

        GlimzoCore core  = (GlimzoCore) Bukkit.getPluginManager().getPlugin("GlimzoCore");
        this.coinManager  = core.getCoinManager();
        this.levelManager = core.getLevelManager();
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        statsManager.loadAsync(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        statsManager.unload(e.getPlayer().getUniqueId());
    }


    @EventHandler
    public void onKill(PlayerKillEvent e) {
        Player killer = e.getKiller();
        if (killer == null) return;

        UUID killerUuid = killer.getUniqueId();
        UUID victimUuid = e.getVictim().getUniqueId();

        boolean isFinal = e.getCause().isFinalKill();

        // Stats
        BWPlayerStats killerStats = statsManager.get(killerUuid);
        if (isFinal) {
            killerStats.addFinalKill();
        } else {
            killerStats.addKill();
        }

        BWPlayerStats victimStats = statsManager.get(victimUuid);
        if (isFinal) {
            victimStats.addFinalDeath();
        } else {
            victimStats.addDeath();
        }

        // Rewards to killer
        int coins = isFinal ? cfg.getCoinsPerFinalKill() : cfg.getCoinsPerKill();
        int xp    = isFinal ? cfg.getXpPerFinalKill()    : cfg.getXpPerKill();

        coinManager.addCoins(killerUuid, coins, "bedwars-kill");
        levelManager.addXp(killerUuid, xp);
    }


    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent e) {
        Player player = e.getPlayer();
        UUID uuid     = player.getUniqueId();

        statsManager.get(uuid).addBedBreak();

        coinManager.addCoins(uuid, cfg.getCoinsPerBedBreak(), "bedwars-bed-break");
        levelManager.addXp(uuid, cfg.getXpPerBedBreak());
    }


    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        // Winners
        for (UUID uuid : e.getWinners()) {
            Player p = Bukkit.getPlayer(uuid);

            statsManager.get(uuid).addWin();
            coinManager.addCoins(uuid, cfg.getCoinsPerWin(), "bedwars-win");

            if (p != null) {
                levelManager.addXp(uuid, cfg.getXpPerWin());
            }

            statsManager.saveAsync(uuid);
        }

        // Losers
        for (UUID uuid : e.getLosers()) {
            Player p = Bukkit.getPlayer(uuid);

            statsManager.get(uuid).addLoss();
            coinManager.addCoins(uuid, cfg.getCoinsPerLoss(), "bedwars-loss");

            if (p != null) {
                levelManager.addXp(uuid, cfg.getXpPerLoss());
            }

            statsManager.saveAsync(uuid);
        }
    }
}