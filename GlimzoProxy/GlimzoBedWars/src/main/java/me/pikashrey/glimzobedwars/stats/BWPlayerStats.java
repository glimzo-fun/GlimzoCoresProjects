package me.pikashrey.glimzobedwars.stats;

import java.util.UUID;

/**
 * BedWars-specific stats for a player.
 * Stored separately from GlimzoCore global stats.
 */
public class BWPlayerStats {

    private final UUID uuid;

    private int wins;
    private int losses;
    private int kills;
    private int finalKills;
    private int deaths;
    private int finalDeaths;
    private int bedsBreoken;
    private int gamesPlayed;

    public BWPlayerStats(UUID uuid) {
        this.uuid = uuid;
    }

    // --- Mutators called during gameplay ---

    public void addWin()        { wins++;        gamesPlayed++; }
    public void addLoss()       { losses++;      gamesPlayed++; }
    public void addKill()       { kills++; }
    public void addFinalKill()  { finalKills++;  kills++; }
    public void addDeath()      { deaths++; }
    public void addFinalDeath() { finalDeaths++; deaths++; }
    public void addBedBreak()   { bedsBreoken++; }

    // --- Computed stats ---

    public double getKDR() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public double getWLR() {
        return losses == 0 ? wins : (double) wins / losses;
    }

    public double getFKDR() {
        return finalDeaths == 0 ? finalKills : (double) finalKills / finalDeaths;
    }

    // --- Getters ---

    public UUID getUuid()       { return uuid; }
    public int getWins()        { return wins; }
    public int getLosses()      { return losses; }
    public int getKills()       { return kills; }
    public int getFinalKills()  { return finalKills; }
    public int getDeaths()      { return deaths; }
    public int getFinalDeaths() { return finalDeaths; }
    public int getBedsBreoken() { return bedsBreoken; }
    public int getGamesPlayed() { return gamesPlayed; }

    // --- Setters for loading from DB ---

    public void setWins(int v)        { wins = v; }
    public void setLosses(int v)      { losses = v; }
    public void setKills(int v)       { kills = v; }
    public void setFinalKills(int v)  { finalKills = v; }
    public void setDeaths(int v)      { deaths = v; }
    public void setFinalDeaths(int v) { finalDeaths = v; }
    public void setBedsBreoken(int v) { bedsBreoken = v; }
    public void setGamesPlayed(int v) { gamesPlayed = v; }
}
