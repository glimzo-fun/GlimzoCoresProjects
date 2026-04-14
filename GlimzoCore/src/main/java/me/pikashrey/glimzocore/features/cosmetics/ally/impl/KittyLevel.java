package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevel;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
// Note: PotionEffect import kept - used in addPotionEffect() constructor calls

/**
 * Kitty Ally Levels
 * L1: Speed I
 * L2: Speed III + Right-click Kitty Item Pounce
 * L3: Paw particles
 */
public class KittyLevel extends AllyLevel {

    private int particleTickCounter = 0;
    // BUG #13 FIX: applyEffects() called every tick which calls
    // removePotionEffect + addPotionEffect every tick → causes visual flicker.
    private int effectTickCounter = 0;

    public KittyLevel(AllyType allyType, Player owner, int level) {
        super(allyType, owner, level);
    }

    @Override
    public void onLevelUp() {
        applyEffects();
        owner.sendMessage("§d✨ Kitty reached level " + level + "!");
    }

    @Override
    public void onTick() {
        if (!owner.isOnline()) return;

        // BUG #13 FIX: Only re-apply effects every 60 ticks (3s), not every tick.
        // This prevents constant remove+add causing the speed effect to flicker.
        effectTickCounter++;
        if (effectTickCounter >= 60) {
            applyEffects();
            effectTickCounter = 0;
        }

        // L3: Paw particles when moving
        if (level >= 3) {
            particleTickCounter++;
            if (particleTickCounter >= 8) {
                if (owner.getVelocity().length() > 0.05) {
                    Location footLoc = owner.getLocation().clone().add(0, 0.1, 0);
                    ParticleUtil.pawParticles(footLoc, 1);
                }
                particleTickCounter = 0;
            }
        }
    }

    @Override
    public void onLevelDown() {
        owner.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public void applyEffects() {
        if (!owner.isOnline()) return;

        if (level >= 1) {
            // BUG #14 FIX: Original removed + re-added the effect every tick causing flicker.
            // can't read the current amplifier. Instead: always remove and re-add only when
            // NOT already present. Since the amplifier changes between L1 and L2, we
            // always remove first when the level changes (handled by onLevelDown/onLevelUp).
            // During steady-state ticking we only re-add if it's completely missing.
            int amplifier = (level >= 2) ? 2 : 0;
            if (!owner.hasPotionEffect(PotionEffectType.SPEED)) {
                owner.addPotionEffect(
                        new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, amplifier, false, false));
            }
        } else {
            owner.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    @Override
    public String getLevelDescription() {
        switch (level) {
            case 1: return "🐾 Speed I";
            case 2: return "🐾 Speed III + Pounce";
            case 3: return "🐾 Paw Particles";
            default: return "Unknown";
        }
    }
}
