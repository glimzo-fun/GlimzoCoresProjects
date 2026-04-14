package me.pikashrey.glimzocore.api.rank;

public class RankData {

    private final String  id;
    private final String  displayName;
    private final String  colorCode;        // Bukkit color code e.g. "&4"
    private final String  chatPrefix;       // e.g. "&4[Chief]"
    private final String  permission;       // e.g. "glimzo.rank.chief"
    private final int     weight;           // higher = more important; used for priority resolution
    private final boolean isDonor;          // true = purchasable; false = staff-grant only
    private final boolean isDefault;        // true = applied to every player with no grants
    private final int     maxClanSize;      // max clan members when this rank holds the leader slot; 0 = cannot create

    public RankData(String id, String displayName, String colorCode, String chatPrefix,
                    String permission, int weight, boolean isDonor, boolean isDefault,
                    int maxClanSize) {
        this.id          = id;
        this.displayName = displayName;
        this.colorCode   = colorCode;
        this.chatPrefix  = chatPrefix;
        this.permission  = permission;
        this.weight      = weight;
        this.isDonor     = isDonor;
        this.isDefault   = isDefault;
        this.maxClanSize = maxClanSize;
    }

    /** Whether this rank can be granted by staff (all non-default ranks can). */
    public boolean isStaffGrantable() { return !isDefault; }

    /** Whether this rank can be purchased by a player. */
    public boolean isPurchasable() { return isDonor; }

    /** Whether a player with this rank can create a clan (Legendary or above). */
    public boolean canCreateClan() { return maxClanSize > 0; }

    public String  getId()          { return id; }
    public String  getDisplayName() { return displayName; }
    public String  getColorCode()   { return colorCode; }
    public String  getChatPrefix()  { return chatPrefix; }
    public String  getPermission()  { return permission; }
    public int     getWeight()      { return weight; }
    public boolean isDonor()        { return isDonor; }
    public boolean isDefault()      { return isDefault; }
    public int     getMaxClanSize() { return maxClanSize; }

    @Override
    public String toString() { return displayName; }
}
