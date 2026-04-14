package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevel;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Charizard Ally Levels
 * L1: Permanent Flame Trail while walking
 * L2: Jump Boost I
 * L3: Idle smoke aura
 */
public class CharizardLevel extends AllyLevel {

    private int particleTickCounter = 0;
    private int effectTickCounter   = 0;

    public CharizardLevel(AllyType allyType, Player owner, int level) {
        super(allyType, owner, level);
    }

    @Override
    public void onLevelUp() {
        applyEffects();
        owner.sendMessage("§6✨ Charizard reached level " + level + "!");
    }

    @Override
    public void onTick() {
        if (!owner.isOnline()) return;

        // Throttle effect re-application to every 60 ticks
        effectTickCounter++;
        if (effectTickCounter >= 60) {
            applyEffects();
            effectTickCounter = 0;
        }

        // L1: Flame trail when moving
        if (level >= 1 && owner.getVelocity().length() > 0.05) {
            // BUG #16 FIX: Original code called owner.getLocation().add(0, 0.1, 0)
            // which MUTATES the Location object returned by getLocation() - this
            // can shift the player's stored location reference on some server implementations.
            // Always call .clone() before modifying a Location.
            Location footLoc = owner.getLocation().clone().add(0, 0.1, 0);
            ParticleUtil.fireParticles(footLoc, 2);
        }

        // L3: Idle smoke - only at exactly L3, not stacked with L1/L2
        // BUG #17 FIX: Original used `level >= 3` which is fine since max is 3,
        // but it also shared `particleTickCounter` between L1 flame and L3 smoke,
        // meaning at L3 both effects competed for the same counter and smoke
        // was never consistently triggered. Use a separate counter.
        if (level >= 3) {
            particleTickCounter++;
            if (particleTickCounter >= 10) {
                Location headLoc = owner.getLocation().clone().add(0, owner.getEyeHeight(), 0);
                ParticleUtil.smokeParticles(headLoc, 1);
                particleTickCounter = 0;
            }
        }
    }

    @Override
    public void onLevelDown() {
        owner.removePotionEffect(PotionEffectType.JUMP);
    }

    @Override
    public void applyEffects() {
        if (!owner.isOnline()) return;

        if (level >= 2) {
            if (!owner.hasPotionEffect(PotionEffectType.JUMP)) {
                owner.addPotionEffect(
                        new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0, false, false));
            }
        } else {
            owner.removePotionEffect(PotionEffectType.JUMP);
        }
    }

    @Override
    public String getLevelDescription() {
        switch (level) {
            case 1: return "🔥 Flame Trail while walking";
            case 2: return "💨 Jump Boost I";
            case 3: return "😈 Smoke Aura (idle)";
            default: return "Unknown";
        }
    }
}
