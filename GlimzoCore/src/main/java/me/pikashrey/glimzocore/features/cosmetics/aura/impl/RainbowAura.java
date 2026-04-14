package me.pikashrey.glimzocore.features.cosmetics.aura.impl;

import me.pikashrey.glimzocore.features.cosmetics.aura.BaseAura;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RainbowAura extends BaseAura {

    public RainbowAura() { super("rainbow_aura", "Rainbow Aura", null); }

    @Override
    public void tick(Player player) {
        if (player.isDead() || !player.isOnline()) return;
        Location center = player.getLocation().clone().add(0, 1, 0);
        AuraState state = getState(player);
        for (Player viewer : player.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(center) > 2500) continue;
            for (int i = 0; i < 8; i++) {
                double a = state.angle + (Math.PI * 2 / 8 * i);
                double x = Math.cos(a) * 0.9;
                double z = Math.sin(a) * 0.9;
                Location loc = center.clone().add(x, 0, z);
                int type = (i + (int)(state.angle * 2)) % 3;
                if (type == 0)      ParticleUtil.crit(viewer, loc, 2);
                else if (type == 1) ParticleUtil.enchant(viewer, loc, 2);
                else                ParticleUtil.magicCrit(viewer, loc, 2);
            }
        }
        state.angle = (state.angle + 0.18) % (Math.PI * 2);
    }
}
