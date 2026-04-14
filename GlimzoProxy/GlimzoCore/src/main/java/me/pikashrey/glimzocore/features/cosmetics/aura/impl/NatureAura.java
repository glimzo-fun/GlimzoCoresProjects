package me.pikashrey.glimzocore.features.cosmetics.aura.impl;

import me.pikashrey.glimzocore.features.cosmetics.aura.BaseAura;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NatureAura extends BaseAura {

    public NatureAura() { super("nature_aura", "Nature Aura", null); }

    @Override
    public void tick(Player player) {
        if (player.isDead() || !player.isOnline()) return;
        Location center = player.getLocation().clone().add(0, 1, 0);
        AuraState state = getState(player);
        for (Player viewer : player.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(center) > 2500) continue;
            for (int i = 0; i < 12; i++) {
                double a = state.angle + (Math.PI * 2 / 12 * i);
                double x = Math.cos(a) * 0.9;
                double z = Math.sin(a) * 0.9;
                Location loc = center.clone().add(x, 0, z);
                ParticleUtil.enchant(viewer, loc, 2);
            }
        }
        state.angle = (state.angle + 0.10) % (Math.PI * 2);
    }
}
