package me.pikashrey.glimzocore.features.cosmetics.morph.impl;

import me.pikashrey.glimzocore.features.cosmetics.morph.BaseMorph;
import me.pikashrey.glimzocore.features.cosmetics.morph.MorphType;
import org.bukkit.entity.Player;

public class ChickenMorph extends BaseMorph {

    public ChickenMorph() {
        super(MorphType.CHICKEN);
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
