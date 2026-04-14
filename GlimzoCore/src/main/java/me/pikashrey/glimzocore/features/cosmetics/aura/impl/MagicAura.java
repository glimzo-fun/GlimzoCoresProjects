package me.pikashrey.glimzocore.features.cosmetics.aura.impl;

import me.pikashrey.glimzocore.features.cosmetics.aura.BaseAura;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MagicAura extends BaseAura {

    public MagicAura() { super("magic_aura", "Magic Aura", null); }

    @Override
    public void tick(Player player) {
        if (player.isDead() || !player.isOnline()) return;
        Location center = player.getLocation().clone().add(0, 1, 0);
        AuraState state = getState(player);
        for (Player viewer : player.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(center) > 2500) continue;
            for (int i = 0; i < 10; i++) {
                double a = state.angle + (Math.PI * 2 / 10 * i);
                double x = Math.cos(a) * 0.9;
                double z = Math.sin(a) * 0.9;
                Location loc = center.clone().add(x, 0, z);
                // SPELL_MOB = colored potion swirls, very visible
                // speed=0 with offsetX/Y/Z as color RGB (0-1 range) in 1.8.8
                ParticleUtil.spawn(viewer, EnumParticle.SPELL_MOB, loc, 0,
                        0.44f, 0.13f, 0.93f, 1.0f); // purple magic color
            }
        }
        state.angle = (state.angle + 0.15) % (Math.PI * 2);
    }
}
