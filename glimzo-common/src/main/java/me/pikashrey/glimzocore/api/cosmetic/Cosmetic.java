package me.pikashrey.glimzocore.api.cosmetic;

public class Cosmetic {

    public enum Rarity {
        COMMON("&7Common"),
        RARE("&9Rare"),
        EPIC("&5Epic"),
        LEGENDARY("&6Legendary"),
        LIMITED("&cLimited");

        private final String display;
        Rarity(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    private final String       id;
    private final String       displayName;
    private final CosmeticType type;
    private final Rarity       rarity;
    private final String       permission;  // null = no permission needed
    private final long         gemCost;     // 0 = not purchasable
    private final boolean      limited;

    public Cosmetic(String id, String displayName, CosmeticType type, Rarity rarity,
                    String permission, long gemCost, boolean limited) {
        this.id          = id;
        this.displayName = displayName;
        this.type        = type;
        this.rarity      = rarity;
        this.permission  = permission;
        this.gemCost     = gemCost;
        this.limited     = limited;
    }

    public boolean isPurchasable() { return gemCost > 0; }
    public boolean hasPermission() { return permission != null && !permission.isEmpty(); }

    public String       getId()          { return id; }
    public String       getDisplayName() { return displayName; }
    public CosmeticType getType()        { return type; }
    public Rarity       getRarity()      { return rarity; }
    public String       getPermission()  { return permission; }
    public long         getGemCost()     { return gemCost; }
    public boolean      isLimited()      { return limited; }

    @Override
    public String toString() { return displayName; }
}