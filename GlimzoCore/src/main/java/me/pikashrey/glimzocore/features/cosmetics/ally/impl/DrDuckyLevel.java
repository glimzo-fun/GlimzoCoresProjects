package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevel;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Dr. Ducky Ally Levels
 * L1: Water splash particles when moving
 * L2: Random quack sounds occasionally
 * L3: Double jump (10-second cooldown)
 */
public class DrDuckyLevel extends AllyLevel {

    // BUG #15 FIX: Double jump was COMPLETELY UNIMPLEMENTED - the entire L3 block
    // was a comment stub with no actual logic. It also can't work via onTick()
    // alone - double jump requires detecting a jump input while already airborne,
    // which needs an event listener. We track "used double jump" state here and
    // expose a method that the PlayerMoveListener (or an InteractListener) calls.
    //
    // Approach: track when player leaves ground (isOnGround goes false) and allow
    // one boost within the cooldown window. We detect the second jump by watching
    // the player's Y velocity spike while already airborne.

    private static final Set<UUID> doubleJumpReady = new HashSet<>();

    private int  soundTickCounter = 0;
    private long lastDoubleJumpTime = 0L;
    private boolean wasOnGround = true;

    public DrDuckyLevel(AllyType allyType, Player owner, int level) {
        super(allyType, owner, level);
    }

    @Override
    public void onLevelUp() {
        applyEffects();
        owner.sendMessage("§9✨ Dr. Ducky reached level " + level + "!");
        if (level >= 3) doubleJumpReady.add(owner.getUniqueId());
    }

    @Override
    public void onTick() {
        if (!owner.isOnline()) return;

        // L1: Water particles when moving
        if (level >= 1 && owner.getVelocity().length() > 0.05) {
            Location footLoc = owner.getLocation().clone().add(0, 0.1, 0);
            ParticleUtil.waterParticles(footLoc, 2);
        }

        // L2: Random quack
        if (level >= 2) {
            soundTickCounter++;
            if (soundTickCounter >= 100) {
                if (Math.random() < 0.3) {
                    owner.getWorld().playSound(owner.getLocation(), Sound.CHICKEN_IDLE, 1.0f, 1.0f);
                }
                soundTickCounter = 0;
            }
        }

        // L3: Double jump state tracking
        if (level >= 3) {
            boolean onGround = owner.isOnGround();

            // Player just landed → reset double jump availability
            if (!wasOnGround && onGround) {
                long cooldownMs = 10_000L;
                if (System.currentTimeMillis() - lastDoubleJumpTime >= cooldownMs) {
                    doubleJumpReady.add(owner.getUniqueId());
                }
            }

            // Detect second jump: player is airborne, moving upward,
            // and we haven't used the double jump yet this airtime
            if (!onGround && wasOnGround == false) {
                double vy = owner.getVelocity().getY();
                if (vy > 0.3 && doubleJumpReady.contains(owner.getUniqueId())) {
                    performDoubleJump();
                }
            }

            wasOnGround = onGround;
        }
    }

    /** Called when a valid double-jump input is detected. */
    private void performDoubleJump() {
        long now = System.currentTimeMillis();
        if (now - lastDoubleJumpTime < 10_000L) return; // still on cooldown

        doubleJumpReady.remove(owner.getUniqueId());
        lastDoubleJumpTime = now;

        // Launch the player upward
        Vector vel = owner.getVelocity();
        vel.setY(0.7);
        owner.setVelocity(vel);

        owner.getWorld().playSound(owner.getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 1.2f);
        owner.sendMessage("§9Double Jump!");
    }

    @Override
    public void onLevelDown() {
        doubleJumpReady.remove(owner.getUniqueId());
    }

    @Override
    public void applyEffects() {
        // No persistent potion effects for Ducky
    }

    public static boolean hasDoubleJumpReady(UUID uuid) {
        return doubleJumpReady.contains(uuid);
    }

    @Override
    public String getLevelDescription() {
        switch (level) {
            case 1: return "💦 Water Splash Particles";
            case 2: return "🎵 Random Quack Sounds";
            case 3: return "🕊️ Double Jump (10s cooldown)";
            default: return "Unknown";
        }
    }
}
