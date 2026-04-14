package me.pikashrey.glimzocore.api.season;

public class SeasonRank {

    private final int    index;        // 0-based position in the ladder
    private final String id;           // internal id e.g. "bronze_1"
    private final String displayName;  // e.g. "Bronze I"
    private final String colorCode;    // e.g. "&6" for gold
    private final long   xpRequired;   // total season XP needed to reach this rank

    public SeasonRank(int index, String id, String displayName, String colorCode, long xpRequired) {
        this.index       = index;
        this.id          = id;
        this.displayName = displayName;
        this.colorCode   = colorCode;
        this.xpRequired  = xpRequired;
    }

    public int    getIndex()       { return index; }
    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getColorCode()   { return colorCode; }
    public long   getXpRequired()  { return xpRequired; }

    public String getColoredName() { return colorCode + displayName; }

    @Override
    public String toString() { return displayName; }
}