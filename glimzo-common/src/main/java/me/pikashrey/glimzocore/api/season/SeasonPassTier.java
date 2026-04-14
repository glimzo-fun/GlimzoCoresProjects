package me.pikashrey.glimzocore.api.season;

import java.util.List;

public class SeasonPassTier {

    private final int          tier;
    private final long         xpRequired;
    private final List<String> freeRewards;
    private final List<String> premiumRewards;

    public SeasonPassTier(int tier, long xpRequired, List<String> freeRewards, List<String> premiumRewards) {
        this.tier           = tier;
        this.xpRequired     = xpRequired;
        this.freeRewards    = freeRewards;
        this.premiumRewards = premiumRewards;
    }

    public int          getTier()           { return tier; }
    public long         getXpRequired()     { return xpRequired; }
    public List<String> getFreeRewards()    { return freeRewards; }
    public List<String> getPremiumRewards() { return premiumRewards; }

    public boolean hasFreeRewards()    { return freeRewards    != null && !freeRewards.isEmpty(); }
    public boolean hasPremiumRewards() { return premiumRewards != null && !premiumRewards.isEmpty(); }
}