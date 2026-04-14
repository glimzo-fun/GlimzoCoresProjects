package me.pikashrey.glimzocore.features.cosmetics.morph.impl;

import me.pikashrey.glimzocore.features.cosmetics.morph.BaseMorph;
import me.pikashrey.glimzocore.features.cosmetics.morph.MorphType;
import org.bukkit.entity.Player;

public class CreeperMorph extends BaseMorph {

    public CreeperMorph() {
        super(MorphType.CREEPER);
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
