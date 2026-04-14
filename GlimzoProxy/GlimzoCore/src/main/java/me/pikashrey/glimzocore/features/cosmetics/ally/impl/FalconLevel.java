package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevel;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Falcon Ally Levels
 * L1: /fly permission (setAllowFlight)
 * L2: Speed II
 * L3: Right-click Falcon Item Dash Forward (handled by InteractListener)
 */
public class FalconLevel extends AllyLevel {

    // BUG #11 FIX: applyEffects() was called every single tick (20/s),
    // re-applying potion effects and setAllowFlight constantly.
    // Use a tick throttle - only re-check every 40 ticks (2s).
    private int tickCounter = 0;

    public FalconLevel(AllyType allyType, Player owner, int level) {
        super(allyType, owner, level);
    }

    @Override
    public void onLevelUp() {
        applyEffects(); // apply immediately on level-up
        owner.sendMessage("§f✨ Falcon reached level " + level + "!");
    }

    @Override
    public void onTick() {
        if (!owner.isOnline()) return;
        tickCounter++;
        if (tickCounter >= 40) {
            applyEffects();
            tickCounter = 0;
        }
    }

    @Override
    public void onLevelDown() {
        // BUG #12 FIX: Old code had a comment saying "fly permission removal
        // should be handled by permission system" and never actually revoked fly.
        // Players would keep /fly permanently after losing the perk.
        owner.removePotionEffect(PotionEffectType.SPEED);
        // Only revoke flight if no other source grants it
        if (!owner.hasPermission("glimzocore.fly") && !owner.hasPermission("glimzo.rank.baron")) {
            owner.setAllowFlight(false);
            owner.setFlying(false);
        }
    }

    @Override
    public void applyEffects() {
        if (!owner.isOnline()) return;

        // L1: grant flight
        if (level >= 1) {
            if (!owner.getAllowFlight()) {
                owner.setAllowFlight(true);
            }
        }

        // L2: Speed II
        if (level >= 2) {
            if (!owner.hasPotionEffect(PotionEffectType.SPEED)) {
                owner.addPotionEffect(
                        new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
            }
        } else {
            owner.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    @Override
    public String getLevelDescription() {
        switch (level) {
            case 1: return "🔥 Player gets /fly at level 1";
            case 2: return "⚡ Speed II";
            case 3: return "🦅 Right-click Dash Forward";
            default: return "Unknown";
        }
    }
}