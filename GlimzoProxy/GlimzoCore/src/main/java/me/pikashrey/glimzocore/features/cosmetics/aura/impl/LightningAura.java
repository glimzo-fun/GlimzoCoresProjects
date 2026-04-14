package me.pikashrey.glimzocore.features.cosmetics.aura.impl;

import me.pikashrey.glimzocore.features.cosmetics.aura.BaseAura;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LightningAura extends BaseAura {

    private static final Random RANDOM = new Random();

    // Separate int counter per player - NOT stored in AuraState.lastTick
    // because AuraState.lastTick is initialized to System.currentTimeMillis()
    // (a huge number) which breaks >= 60 checks entirely.
    private final Map<UUID, Integer> strikeTick   = new HashMap<>();
    private final Map<UUID, Integer> explosionLeft = new HashMap<>();

    public LightningAura() { super("lightning_aura", "Lightning Aura", null); }

    @Override
    public void equip(Player player) {
        super.equip(player);
        strikeTick.put(player.getUniqueId(), 0);
        explosionLeft.put(player.getUniqueId(), 0);
    }

    @Override
    public void unequip(Player player) {
        super.unequip(player);
        strikeTick.remove(player.getUniqueId());
        explosionLeft.remove(player.getUniqueId());
    }

    @Override
    public void tick(Player player) {
        if (player.isDead() || !player.isOnline()) return;
        Location center = player.getLocation().clone().add(0, 1, 0);
        AuraState state = getState(player);
        UUID uuid = player.getUniqueId();

        int expLeft = explosionLeft.getOrDefault(uuid, 0);

        for (Player viewer : player.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(center) > 2500) continue;

            // Constant FIREWORKS_SPARK ring
            for (int i = 0; i < 10; i++) {
                double a = state.angle + (Math.PI * 2 / 10 * i);
                double x = Math.cos(a) * 1.0;
                double z = Math.sin(a) * 1.0;
                Location loc = center.clone().add(x, 0, z);
                ParticleUtil.spawn(viewer, EnumParticle.FIREWORKS_SPARK, loc, 4,
                        0.08f, 0.15f, 0.08f, 0.05f);
            }

            // Explosion burst for 2s after lightning strike
            if (expLeft > 0) {
                for (int e = 0; e < 8; e++) {
                    double ex = (RANDOM.nextDouble() - 0.5) * 3.0;
                    double ey = RANDOM.nextDouble() * 2.0;
                    double ez = (RANDOM.nextDouble() - 0.5) * 3.0;
                    ParticleUtil.spawn(viewer, EnumParticle.EXPLOSION_NORMAL,
                            player.getLocation().clone().add(ex, ey, ez), 1,
                            0f, 0f, 0f, 0f);
                }
            }
        }

        if (expLeft > 0) explosionLeft.put(uuid, expLeft - 1);

        // Strike every 6 seconds: 6s = 120 ticks / 2 tick interval = 60 cycles
        int tick = strikeTick.getOrDefault(uuid, 0) + 1;
        if (tick >= 60) {
            strikeTick.put(uuid, 0);
            double strikeAngle = RANDOM.nextDouble() * Math.PI * 2;
            double strikeDist  = 2.0 + RANDOM.nextDouble() * 2.0;
            Location strikeLoc = player.getLocation().clone()
                    .add(Math.cos(strikeAngle) * strikeDist, 0, Math.sin(strikeAngle) * strikeDist);
            player.getWorld().strikeLightningEffect(strikeLoc);
            explosionLeft.put(uuid, 20);
        } else {
            strikeTick.put(uuid, tick);
        }

        state.angle = (state.angle + 0.25) % (Math.PI * 2);
    }
}