package me.pikashrey.glimzocore.features.cosmetics.ally;

import org.bukkit.entity.Player;

/**
 * Represents a level within an Ally that provides perks to the player.
 * Each Ally has 3 levels, and perks apply to the player owning the ally.
 */
public abstract class AllyLevel {

    protected final int level;
    protected final AllyType allyType;
    protected final Player owner;

    public AllyLevel(AllyType allyType, Player owner, int level) {
        this.allyType = allyType;
        this.owner = owner;
        this.level = level;
    }

    /** Initialize perks when reaching this level */
    public abstract void onLevelUp();

    /** Called every tick to update perks */
    public abstract void onTick();

    /** Clean up perks when level is lost */
    public abstract void onLevelDown();

    /** Apply passive effects that stay on the player */
    public abstract void applyEffects();

    public int getLevel() { return level; }
    public AllyType getAllyType() { return allyType; }
    public Player getOwner() { return owner; }

    /**
     * Get the display name of this level's perks
     */
    public abstract String getLevelDescription();
}

