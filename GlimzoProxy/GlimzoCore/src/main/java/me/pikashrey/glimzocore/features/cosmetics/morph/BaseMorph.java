package me.pikashrey.glimzocore.features.cosmetics.morph;

import org.bukkit.entity.Player;

public abstract class BaseMorph {

    protected final MorphType type;

    public BaseMorph(MorphType type) {
        this.type = type;
    }

    public void onEquip(Player player) {}

    public void onUnequip(Player player) {}

    public void onTick(Player player) {}

    public MorphType getType()        { return type; }
    public String    getId()          { return type.getId(); }
    public String    getDisplayName() { return type.getDisplayName(); }
}
