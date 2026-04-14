package me.pikashrey.glimzocore.features.cosmetics.morph.impl;

import me.pikashrey.glimzocore.features.cosmetics.morph.BaseMorph;
import me.pikashrey.glimzocore.features.cosmetics.morph.MorphType;
import org.bukkit.entity.Player;

public class EndermanMorph extends BaseMorph {

    public EndermanMorph() {
        super(MorphType.ENDERMAN);
    }

    @Override
    public void onEquip(Player player) {}

    @Override
    public void onUnequip(Player player) {}
}
