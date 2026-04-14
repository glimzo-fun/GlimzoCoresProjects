package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevel;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Pug Ally Levels
 * L1: Heart particles randomly
 * L2: Right-click Pug Item - Bone Throw (handled by InteractListener)
 * L3: Passive companion
 */
public class PugLevel extends AllyLevel {

    private int particleTickCounter = 0;

    public PugLevel(AllyType allyType, Player owner, int level) {
        super(allyType, owner, level);
    }

    @Override
    public void onLevelUp() {
        owner.sendMessage("§c✨ Pug reached level " + level + "!");
    }

    @Override
    public void onTick() {
        if (!owner.isOnline()) return;

        if (level >= 1) {
            particleTickCounter++;
            if (particleTickCounter >= 20) {
                if (Math.random() < 0.4) {
                    // BUG #18 FIX: Original called owner.getLocation().add(...) without .clone()
                    // which mutates the player's location object. Always clone first.
                    // Also, eye height + 0.5 put particles inside the player's head.
                    // Spawn them slightly above and around the player instead.
                    Location heartLoc = owner.getLocation().clone().add(0, owner.getEyeHeight() + 0.3, 0);
                    ParticleUtil.heartParticles(heartLoc, 3);
                }
                particleTickCounter = 0;
            }
        }
    }

    @Override
    public void onLevelDown() {
        // No persistent effects
    }

    @Override
    public void applyEffects() {
        // No persistent potion effects
    }

    @Override
    public String getLevelDescription() {
        switch (level) {
            case 1: return "💖 Heart Particles";
            case 2: return "🦴 Right-click Bone Throw";
            case 3: return "🐶 Passive Companion";
            default: return "Unknown";
        }
    }
}
