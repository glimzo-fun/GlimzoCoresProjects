package me.pikashrey.glimzocore.api.player;

public class PlayerStats {

    private volatile boolean dirty = false;

    // General
    private int  totalLogins;
    private long totalPlaytimeMinutes;

    // Economy
    private long totalCoinsEarned;
    private long totalCoinsSpent;
    private long totalGemsEarned;
    private long totalGemsSpent;

    // Leveling
    private int  highestLevel;
    private int  totalPrestiges;
    private long totalXPEarned;

    // Season
    private int  highestSeasonRank;
    private int  seasonsCompleted;
    private long totalSeasonXPEarned;

    // Social
    private int totalFriendsAdded;
    private int totalPartiesCreated;
    private int totalClansJoined;

    // Achievements & Cosmetics
    private int totalAchievementsUnlocked;
    private int totalCosmeticsUnlocked;

    // Punishments
    private int totalBans;
    private int totalMutes;
    private int totalWarnings;

    // Dirty flag
    public boolean isDirty()  { return dirty; }
    public void markDirty()   { dirty = true; }
    public void clearDirty()  { dirty = false; }

    // Increment helpers - each marks dirty
    public void incrementLogins()               { totalLogins++;         dirty = true; }
    public void addPlaytime(long minutes)        { totalPlaytimeMinutes += minutes; dirty = true; }

    public void addCoinsEarned(long v)           { totalCoinsEarned += v; dirty = true; }
    public void addCoinsSpent(long v)            { totalCoinsSpent  += v; dirty = true; }
    public void addGemsEarned(long v)            { totalGemsEarned  += v; dirty = true; }
    public void addGemsSpent(long v)             { totalGemsSpent   += v; dirty = true; }

    public void addXP(long v)                    { totalXPEarned    += v; dirty = true; }
    public void incrementPrestiges()             { totalPrestiges++;      dirty = true; }
    public void updateHighestLevel(int level)    { if (level > highestLevel)       { highestLevel = level;       dirty = true; } }

    public void addSeasonXP(long v)              { totalSeasonXPEarned += v; dirty = true; }
    public void updateHighestSeasonRank(int rank) { if (rank > highestSeasonRank) { highestSeasonRank = rank;    dirty = true; } }
    public void incrementSeasonsCompleted()      { seasonsCompleted++;    dirty = true; }

    public void incrementFriendsAdded()          { totalFriendsAdded++;  dirty = true; }
    public void incrementPartiesCreated()        { totalPartiesCreated++; dirty = true; }
    public void incrementClansJoined()           { totalClansJoined++;   dirty = true; }

    public void incrementAchievementsUnlocked()  { totalAchievementsUnlocked++; dirty = true; }
    public void incrementCosmeticsUnlocked()     { totalCosmeticsUnlocked++;    dirty = true; }

    public void incrementBans()                  { totalBans++;     dirty = true; }
    public void incrementMutes()                 { totalMutes++;    dirty = true; }
    public void incrementWarnings()              { totalWarnings++; dirty = true; }

    // Getters + setters (setters used only for MySQL load - do NOT mark dirty)
    public int  getTotalLogins()                  { return totalLogins; }
    public void setTotalLogins(int v)             { this.totalLogins = v; }

    public long getTotalPlaytimeMinutes()         { return totalPlaytimeMinutes; }
    public void setTotalPlaytimeMinutes(long v)   { this.totalPlaytimeMinutes = v; }

    public long getTotalCoinsEarned()             { return totalCoinsEarned; }
    public void setTotalCoinsEarned(long v)       { this.totalCoinsEarned = v; }

    public long getTotalCoinsSpent()              { return totalCoinsSpent; }
    public void setTotalCoinsSpent(long v)        { this.totalCoinsSpent = v; }

    public long getTotalGemsEarned()              { return totalGemsEarned; }
    public void setTotalGemsEarned(long v)        { this.totalGemsEarned = v; }

    public long getTotalGemsSpent()               { return totalGemsSpent; }
    public void setTotalGemsSpent(long v)         { this.totalGemsSpent = v; }

    public int  getHighestLevel()                 { return highestLevel; }
    public void setHighestLevel(int v)            { this.highestLevel = v; }

    public int  getTotalPrestiges()               { return totalPrestiges; }
    public void setTotalPrestiges(int v)          { this.totalPrestiges = v; }

    public long getTotalXPEarned()                { return totalXPEarned; }
    public void setTotalXPEarned(long v)          { this.totalXPEarned = v; }

    public int  getHighestSeasonRank()            { return highestSeasonRank; }
    public void setHighestSeasonRank(int v)       { this.highestSeasonRank = v; }

    public int  getSeasonsCompleted()             { return seasonsCompleted; }
    public void setSeasonsCompleted(int v)        { this.seasonsCompleted = v; }

    public long getTotalSeasonXPEarned()          { return totalSeasonXPEarned; }
    public void setTotalSeasonXPEarned(long v)    { this.totalSeasonXPEarned = v; }

    public int  getTotalFriendsAdded()            { return totalFriendsAdded; }
    public void setTotalFriendsAdded(int v)       { this.totalFriendsAdded = v; }

    public int  getTotalPartiesCreated()          { return totalPartiesCreated; }
    public void setTotalPartiesCreated(int v)     { this.totalPartiesCreated = v; }

    public int  getTotalClansJoined()             { return totalClansJoined; }
    public void setTotalClansJoined(int v)        { this.totalClansJoined = v; }

    public int  getTotalAchievementsUnlocked()    { return totalAchievementsUnlocked; }
    public void setTotalAchievementsUnlocked(int v){ this.totalAchievementsUnlocked = v; }

    public int  getTotalCosmeticsUnlocked()       { return totalCosmeticsUnlocked; }
    public void setTotalCosmeticsUnlocked(int v)  { this.totalCosmeticsUnlocked = v; }

    public int  getTotalBans()                    { return totalBans; }
    public void setTotalBans(int v)               { this.totalBans = v; }

    public int  getTotalMutes()                   { return totalMutes; }
    public void setTotalMutes(int v)              { this.totalMutes = v; }

    public int  getTotalWarnings()                { return totalWarnings; }
    public void setTotalWarnings(int v)           { this.totalWarnings = v; }
}
