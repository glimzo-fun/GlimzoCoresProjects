package me.pikashrey.glimzocore.features.cosmetics.morph;

import org.bukkit.entity.EntityType;

public enum MorphType {

    BLAZE    ("blaze",    "Blaze",    "glimzo.morph.blaze",    EntityType.BLAZE),
    SLIME    ("slime",    "Slime",    "glimzo.morph.slime",    EntityType.SLIME),
    CREEPER  ("creeper",  "Creeper",  "glimzo.morph.creeper",  EntityType.CREEPER),
    CHICKEN  ("chicken",  "Chicken",  "glimzo.morph.chicken",  EntityType.CHICKEN),
    BAT      ("bat",      "Bat",      "glimzo.morph.bat",      EntityType.BAT),
    ENDERMAN ("enderman", "Enderman", "glimzo.morph.enderman", EntityType.ENDERMAN);

    private final String     id;
    private final String     displayName;
    private final String     permission;
    private final EntityType entityType;

    MorphType(String id, String displayName, String permission, EntityType entityType) {
        this.id          = id;
        this.displayName = displayName;
        this.permission  = permission;
        this.entityType  = entityType;
    }

    public String     getId()          { return id; }
    public String     getDisplayName() { return displayName; }
    public String     getPermission()  { return permission; }
    public EntityType getEntityType()  { return entityType; }

    public static MorphType fromId(String id) {
        if (id == null) return null;
        for (MorphType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}
