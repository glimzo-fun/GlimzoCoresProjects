package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevel;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import org.bukkit.entity.Player;

/**
 * Mr. Panda Ally Levels
 * L1: Does absolutely nothing
 * L2: Sit/Rest emote - when player does /sit or /lie command they should sit/lie on the ground
 * L3: Does absolutely nothing
 */
public class MrPandaLevel extends AllyLevel {

    public MrPandaLevel(AllyType allyType, Player owner, int level) {
        super(allyType, owner, level);
    }

    @Override
    public void onLevelUp() {
        owner.sendMessage("§0✨ Mr. Panda reached level " + level + "!");
    }

    @Override
    public void onTick() {
        if (!owner.isOnline()) return;

        applyEffects();
        // No active effects for panda
    }

    @Override
    public void onLevelDown() {
        // No effects to remove
    }

    @Override
    public void applyEffects() {
        // Level 2 is handled by /sit and /lie commands
        // No automatic effects
    }

    @Override
    public String getLevelDescription() {
        switch (level) {
            case 1: return "🐼 Does absolutely nothing (passive ally)";
            case 2: return "🐼 Sit/Rest emote: /sit and /lie commands enabled";
            case 3: return "🐼 Does absolutely nothing (passive ally)";
            default: return "Unknown";
        }
    }
}

