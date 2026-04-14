package me.pikashrey.glimzobedwars.config;

import me.pikashrey.glimzobedwars.GlimzoBedWars;
import org.bukkit.configuration.file.FileConfiguration;

public class BWConfig {

    private final GlimzoBedWars plugin;

    // Coin rewards
    private int coinsPerKill;
    private int coinsPerFinalKill;
    private int coinsPerBedBreak;
    private int coinsPerWin;
    private int coinsPerLoss;

    // XP rewards
    private int xpPerKill;
    private int xpPerFinalKill;
    private int xpPerBedBreak;
    private int xpPerWin;
    private int xpPerLoss;

    public BWConfig(GlimzoBedWars plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        coinsPerKill      = cfg.getInt("rewards.coins.kill", 5);
        coinsPerFinalKill = cfg.getInt("rewards.coins.final-kill", 15);
        coinsPerBedBreak  = cfg.getInt("rewards.coins.bed-break", 20);
        coinsPerWin       = cfg.getInt("rewards.coins.win", 50);
        coinsPerLoss      = cfg.getInt("rewards.coins.loss", 5);

        xpPerKill         = cfg.getInt("rewards.xp.kill", 10);
        xpPerFinalKill    = cfg.getInt("rewards.xp.final-kill", 30);
        xpPerBedBreak     = cfg.getInt("rewards.xp.bed-break", 40);
        xpPerWin          = cfg.getInt("rewards.xp.win", 100);
        xpPerLoss         = cfg.getInt("rewards.xp.loss", 10);
    }

    public int getCoinsPerKill()      { return coinsPerKill; }
    public int getCoinsPerFinalKill() { return coinsPerFinalKill; }
    public int getCoinsPerBedBreak()  { return coinsPerBedBreak; }
    public int getCoinsPerWin()       { return coinsPerWin; }
    public int getCoinsPerLoss()      { return coinsPerLoss; }

    public int getXpPerKill()         { return xpPerKill; }
    public int getXpPerFinalKill()    { return xpPerFinalKill; }
    public int getXpPerBedBreak()     { return xpPerBedBreak; }
    public int getXpPerWin()          { return xpPerWin; }
    public int getXpPerLoss()         { return xpPerLoss; }
}
