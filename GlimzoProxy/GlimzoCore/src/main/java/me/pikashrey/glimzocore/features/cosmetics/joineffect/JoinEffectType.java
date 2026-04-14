package me.pikashrey.glimzocore.features.cosmetics.joineffect;

public enum JoinEffectType {

    LIGHTNING ("lightning", "Lightning Strike", "glimzo.joineffect.lightning"),
    DRAGON    ("dragon",    "Dragon Roar",      "glimzo.joineffect.dragon"),
    METEOR    ("meteor",    "Meteor Crash",     "glimzo.joineffect.meteor");

    private final String id;
    private final String displayName;
    private final String permission;

    JoinEffectType(String id, String displayName, String permission) {
        this.id          = id;
        this.displayName = displayName;
        this.permission  = permission;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getPermission()  { return permission; }

    public static JoinEffectType fromId(String id) {
        if (id == null) return null;
        for (JoinEffectType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}
