package me.pikashrey.glimzocore.features.achievements;
public enum AchievementCategory {
    SOCIAL("Social", "&a"),
    ECONOMY("Economy", "&6"),
    LEVELING("Leveling", "&b"),
    SEASONS("Seasons", "&d"),
    PUNISHMENTS("Punishments", "&c"),
    CLANS("Clans", "&e"),
    GENERAL("General", "&7");
    private final String displayName;
    private final String color;
    AchievementCategory(String d, String c) { this.displayName = d; this.color = c; }
    public String getDisplayName() { return displayName; }
    public String getColor()       { return color; }
}
