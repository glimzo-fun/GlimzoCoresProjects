package me.pikashrey.glimzocore.features.cosmetics.aura.impl;

import me.pikashrey.glimzocore.features.cosmetics.aura.BaseAura;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InfernoAura extends BaseAura {

    public InfernoAura() { super("inferno_aura", "Inferno Aura", null); }

    @Override
    public void tick(Player player) {
        if (player.isDead() || !player.isOnline()) return;
        Location center = player.getLocation().clone().add(0, 0.3, 0);
        AuraState state = getState(player);
        for (Player viewer : player.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(center) > 2500) continue;
            for (int i = 0; i < 10; i++) {
                double a = state.angle + (Math.PI * 2 / 10 * i);
                double x = Math.cos(a) * 0.8;
                double z = Math.sin(a) * 0.8;
                Location loc = center.clone().add(x, 0, z);
                // angry_villager (red X marks)
                ParticleUtil.spawn(viewer, EnumParticle.VILLAGER_ANGRY, loc, 1,
                        0.1f, 0.1f, 0.1f, 0.0f);
                // dripping_lava
                ParticleUtil.spawn(viewer, EnumParticle.DRIP_LAVA, loc, 2,
                        0.1f, 0.2f, 0.1f, 0.0f);
                // flash = FIREWORKS_SPARK
                ParticleUtil.spawn(viewer, EnumParticle.FIREWORKS_SPARK, loc, 2,
                        0.15f, 0.1f, 0.15f, 0.02f);
            }
        }
        state.angle = (state.angle + 0.20) % (Math.PI * 2);
    }
}
