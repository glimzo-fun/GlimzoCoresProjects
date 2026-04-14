package me.pikashrey.glimzocore.api.clan;

public enum ClanRole {

    MEMBER  ("Member",  "&7", 0),
    OFFICER ("Officer", "&e", 1),
    LEADER  ("Leader",  "&6", 2);

    private final String displayName;
    private final String colorCode;
    private final int    ordinalValue; // explicit, matches DB column value

    ClanRole(String displayName, String colorCode, int ordinalValue) {
        this.displayName  = displayName;
        this.colorCode    = colorCode;
        this.ordinalValue = ordinalValue;
    }

    public String getDisplayName()  { return displayName; }
    public String getColorCode()    { return colorCode; }
    public int    getOrdinalValue() { return ordinalValue; }
    public String getColored()      { return colorCode + displayName; }

    public boolean isAtLeast(ClanRole other) {
        return this.ordinalValue >= other.ordinalValue;
    }

    public boolean isLeader()  { return this == LEADER; }
    public boolean isOfficer() { return this == OFFICER || this == LEADER; }

    public static ClanRole fromOrdinal(int value) {
        for (ClanRole r : values()) {
            if (r.ordinalValue == value) return r;
        }
        return MEMBER;
    }

    @Override
    public String toString() { return displayName; }
}

