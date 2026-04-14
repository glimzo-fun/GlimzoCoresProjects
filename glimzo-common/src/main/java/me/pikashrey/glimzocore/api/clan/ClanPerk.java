package me.pikashrey.glimzocore.api.clan;

public enum ClanPerk {

    // Cosmetic perks
    CUSTOM_TAG       ("Custom Clan Tag",        Category.COSMETIC,    "Display a coloured tag in clan chat"),
    CLAN_BANNER      ("Clan Banner",            Category.COSMETIC,    "Decorative banner on clan profile"),
    EXPANDED_MOTD    ("Extended MOTD",          Category.COSMETIC,    "Longer message-of-the-day"),

    // Functional perks
    XP_BOOST         ("XP Boost",               Category.FUNCTIONAL,  "Bonus XP earned by all members"),
    COIN_BOOST       ("Coin Boost",             Category.FUNCTIONAL,  "Bonus coins earned by all members"),
    GEM_BOOST        ("Gem Boost",              Category.FUNCTIONAL,  "Bonus gems earned by all members"),
    INCREASED_CAPACITY("Increased Capacity",    Category.FUNCTIONAL,  "Extra member slots beyond base limit");

    public enum Category { COSMETIC, FUNCTIONAL }

    private final String   displayName;
    private final Category category;
    private final String   description;

    ClanPerk(String displayName, Category category, String description) {
        this.displayName = displayName;
        this.category    = category;
        this.description = description;
    }

    public String   getDisplayName() { return displayName; }
    public Category getCategory()    { return category; }
    public String   getDescription() { return description; }
    public boolean  isFunctional()   { return category == Category.FUNCTIONAL; }
    public boolean  isCosmetic()     { return category == Category.COSMETIC; }

    @Override
    public String toString() { return displayName; }
}

