package me.pikashrey.glimzocore.api.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;

public enum Rank {

    CHIEF     ("chief"),
    ADMIN     ("admin"),
    ARCHITECT ("architect"),
    PIBBLE    ("pibble"),
    GUARDIAN  ("guardian"),
    GUIDE     ("guide"),
    COSMOS    ("cosmos"),
    ASCENDANT ("ascendant"),
    LEGENDARY ("legendary"),
    MYTHIC    ("mythic"),
    WARDEN    ("warden"),
    DEFAULT   ("default"),
    BARON     ("baron");

    private final String id;

    Rank(String id) { this.id = id; }

    public String getId() { return id; }

    private ConfiguredRank configured() {
        GlimzoCore core = GlimzoCore.getInstance();
        if (core == null || core.getRankLoader() == null) return null;
        return core.getRankLoader().get(id);
    }

    public String getDisplayName() {
        ConfiguredRank c = configured();
        if (c != null && c.getPrefix() != null && !c.getPrefix().isEmpty()) return c.getPrefix();
        return id.substring(0, 1).toUpperCase() + id.substring(1);
    }

    public String getColorCode() {
        ConfiguredRank c = configured();
        if (c != null && c.getPrefix() != null) {
            String p = c.getPrefix();
            if (p.length() >= 2 && p.charAt(0) == '&') return p.substring(0, 2);
        }
        return "&7";
    }

    public String getChatPrefix() {
        ConfiguredRank c = configured();
        return (c != null && c.getPrefix() != null) ? c.getPrefix() : "&7";
    }

    public String getPermission() { return "glimzo.rank." + id; }

    public int getWeight() {
        ConfiguredRank c = configured();
        return c != null ? c.getWeight() : 0;
    }

    public boolean isDonor() {
        ConfiguredRank c = configured();
        return c != null && c.isDonor();
    }

    public boolean isDefault() {
        ConfiguredRank c = configured();
        return c != null && c.isDefault();
    }

    public int getMaxClanSize() {
        ConfiguredRank c = configured();
        return c != null ? c.getMaxClanSize() : 0;
    }

    public boolean canCreateClan() { return getMaxClanSize() > 0; }

    public boolean isStaff() { return !isDonor() && !isDefault(); }

    public boolean isHigherThan(Rank other) { return getWeight() > other.getWeight(); }
    public boolean isAtLeast(Rank other)    { return getWeight() >= other.getWeight(); }

    public static Rank fromId(String id) {
        if (id == null) return null;
        for (Rank r : values()) if (r.id.equalsIgnoreCase(id)) return r;
        return null;
    }

    public static Rank getDefault() {
        // Find the rank configured as default=true with lowest weight
        for (Rank r : values()) {
            if (r.isDefault() && !r.isDonor()) return r;
        }
        return DEFAULT;
    }
}
