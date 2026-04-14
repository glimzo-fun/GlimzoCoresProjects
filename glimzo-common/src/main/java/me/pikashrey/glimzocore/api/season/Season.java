package me.pikashrey.glimzocore.api.season;

import java.util.List;

public class Season {

    private final int                  number;
    private final String               displayName;
    private final long                 startTime;
    private final long                 endTime;
    private final List<SeasonRank>     ranks;
    private final List<SeasonPassTier> passTiers;

    public Season(int number, String displayName, long startTime, long endTime,
                  List<SeasonRank> ranks, List<SeasonPassTier> passTiers) {
        this.number      = number;
        this.displayName = displayName;
        this.startTime   = startTime;
        this.endTime     = endTime;
        this.ranks       = ranks;
        this.passTiers   = passTiers;
    }

    public boolean isActive() {
        long now = System.currentTimeMillis();
        return now >= startTime && now < endTime;
    }

    public boolean hasEnded() {
        return System.currentTimeMillis() >= endTime;
    }

    public SeasonRank getRank(int index) {
        if (index < 0 || index >= ranks.size()) return null;
        return ranks.get(index);
    }

    public int getMaxRankIndex() {
        return ranks.isEmpty() ? 0 : ranks.size() - 1;
    }

    public long getRemainingMs() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public int                  getNumber()      { return number; }
    public String               getDisplayName() { return displayName; }
    public long                 getStartTime()   { return startTime; }
    public long                 getEndTime()     { return endTime; }
    public List<SeasonRank>     getRanks()       { return ranks; }
    public List<SeasonPassTier> getPassTiers()   { return passTiers; }

    public SeasonPassTier getPassTierForRank(int rankIndex) {
        if (passTiers == null || rankIndex < 0 || rankIndex >= passTiers.size()) return null;
        return passTiers.get(rankIndex);
    }

    @Override
    public String toString() { return displayName; }
}