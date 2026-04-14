package me.pikashrey.glimzocore.features.cosmetics.chattag;

public class ChatTag {

    private final String id;
    private final int    order;
    private final String tag;          // e.g. "&c[Savage]"
    private final String description;
    private final String permission;   // null = no permission required

    public ChatTag(String id, int order, String tag, String description, String permission) {
        this.id          = id;
        this.order       = order;
        this.tag         = tag;
        this.description = description;
        this.permission  = permission;
    }

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    public boolean canUse(org.bukkit.entity.Player player) {
        if (!hasPermission()) return true;
        return player.hasPermission(permission) || player.hasPermission("glimzo.chattag.*");
    }

    public String getId()          { return id; }
    public int    getOrder()       { return order; }
    public String getTag()         { return tag; }
    public String getDescription() { return description; }
    public String getPermission()  { return permission; }

    @Override
    public String toString() { return tag; }
}
