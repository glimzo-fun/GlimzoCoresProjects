package me.pikashrey.glimzocore.features.cosmetics.aura.impl;

import me.pikashrey.glimzocore.features.cosmetics.aura.BaseAura;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IceAura extends BaseAura {

    public IceAura() { super("ice_aura", "Ice Aura", null); }

    @Override
    public void tick(Player player) {
        if (player.isDead() || !player.isOnline()) return;
        Location center = player.getLocation().clone().add(0, 1, 0);
        AuraState state = getState(player);
        for (Player viewer : player.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(center) > 2500) continue;
            for (int i = 0; i < 10; i++) {
                double a = state.angle + (Math.PI * 2 / 10 * i);
                double x = Math.cos(a) * 0.85;
                double z = Math.sin(a) * 0.85;
                Location loc = center.clone().add(x, 0, z);
                // BLOCK_DUST with beacon block (ID=138, data=0) = cyan/blue sparkly dust
                ParticleUtil.spawn(viewer, EnumParticle.BLOCK_DUST, loc, 3,
                        0.1f, 0.1f, 0.1f, 0.0f, 138, 0);
            }
        }
        state.angle = (state.angle + 0.12) % (Math.PI * 2);
    }
}
