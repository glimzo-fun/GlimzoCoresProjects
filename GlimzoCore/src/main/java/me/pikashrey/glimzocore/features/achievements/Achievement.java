package me.pikashrey.glimzocore.features.achievements;

public class Achievement {
    private final String              id;
    private final String              displayName;
    private final String              description;
    private final AchievementCategory category;
    private final AchievementTrigger  trigger;
    private final int                 xpReward;
    private final long                coinReward;
    private final int                 targetLevel;
    private final int                 targetCount;
    private final long                targetAmount;
    private final int                 loginStreakDays;

    public Achievement(String id, String displayName, String description,
                       AchievementCategory category, AchievementTrigger trigger,
                       int xpReward, long coinReward,
                       int targetLevel, int targetCount, long targetAmount, int loginStreakDays) {
        this.id             = id;
        this.displayName    = displayName;
        this.description    = description;
        this.category       = category;
        this.trigger        = trigger;
        this.xpReward       = xpReward;
        this.coinReward     = coinReward;
        this.targetLevel    = targetLevel;
        this.targetCount    = targetCount;
        this.targetAmount   = targetAmount;
        this.loginStreakDays = loginStreakDays;
    }

    public String              getId()              { return id; }
    public String              getDisplayName()     { return displayName; }
    public String              getDescription()     { return description; }
    public AchievementCategory getCategory()        { return category; }
    public AchievementTrigger  getTrigger()         { return trigger; }
    public int                 getXpReward()        { return xpReward; }
    public long                getCoinReward()      { return coinReward; }
    public int                 getTargetLevel()     { return targetLevel; }
    public int                 getTargetCount()     { return targetCount; }
    public long                getTargetAmount()    { return targetAmount; }
    public int                 getLoginStreakDays() { return loginStreakDays; }
}
