package me.pikashrey.glimzocore.features.leveling;

public class LevelReward {

    public enum Type {
        COINS,
        GEMS,
        COSMETIC_UNLOCK,   // cosmeticId in value field
        TITLE_UNLOCK,      // titleId in value field
        NONE
    }

    private final int    level;
    private final Type   type;
    private final long   amount;   // coins/gems amount; 0 for non-currency rewards
    private final String value;    // extra data (cosmetic id, title id, etc.); null if unused

    public LevelReward(int level, Type type, long amount, String value) {
        this.level  = level;
        this.type   = type;
        this.amount = amount;
        this.value  = value;
    }

    public static LevelReward coins(int level, long amount) {
        return new LevelReward(level, Type.COINS, amount, null);
    }

    public static LevelReward gems(int level, long amount) {
        return new LevelReward(level, Type.GEMS, amount, null);
    }

    public static LevelReward cosmetic(int level, String cosmeticId) {
        return new LevelReward(level, Type.COSMETIC_UNLOCK, 0, cosmeticId);
    }

    public static LevelReward none(int level) {
        return new LevelReward(level, Type.NONE, 0, null);
    }

    public int    getLevel()  { return level; }
    public Type   getType()   { return type; }
    public long   getAmount() { return amount; }
    public String getValue()  { return value; }
}

