package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.Ally;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPart;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DrDuckyAlly extends Ally {

    private static final String HEAD_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM1N2ZiOGUzMjQyOWI3MWM2NjhkYjg2NjI4YTZkMWM0MDg2MzJiZDgzNWJmYWZhYTdlOTliOTQ0MGRjYTgifX19";
    private static final String BODY_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWNiYWM2ZmI4ZmRmYTY1Yzk1NGJiYjk0OTJiOWY2NWJmMTBiZWM1MTY3YTMzOTMwNzI4YTQ5ZTAyZjllODM0NyJ9fX0=";

    private double bobAngle = 0;

    public DrDuckyAlly(Player owner) { super(AllyType.DR_DUCKY, owner); }

    @Override public boolean isFloating()      { return false; }
    // Push the root DOWN so small-stand skulls sit on the ground.
    // Small armor stand head slot is ~0.84 above its feet.
    // Skull half-height ~0.31. So skull bottom = standFeetY + 0.53.
    // To put skull bottom at groundY: standFeetY = groundY - 0.53.
    // getHeightOffset adds to playerFeetY (= groundY), so offset = -0.53.
    @Override public double  getHeightOffset() { return -0.5; }
    @Override public double  getSideOffset()   { return 0.9; }
    @Override public double  getBehindOffset() { return 0.3; }

    @Override
    protected List<AllyPart> buildParts(Location root) {
        List<AllyPart> list = new ArrayList<>();

        // [0] Body skull - at dy=0 relative to already-lowered root
        AllyPart body = new AllyPart(root, 0, 0, 0, false, true);
        body.setHelmet(SkullBuilder.fromBase64(BODY_TEXTURE));
        list.add(body);

        // [1] Head skull - 0.38 above body (skulls touching)
        AllyPart head = new AllyPart(root, 0, 0.38, 0, false, true);
        head.setHelmet(SkullBuilder.fromBase64(HEAD_TEXTURE));
        list.add(head);

        return list;
    }

    @Override
    public void onTick() {
        bobAngle += 0.12;
        float headBob = (float)(Math.sin(bobAngle) * 5);
        if (parts.size() > 1) {
            parts.get(1).setHeadPose(headBob, 0, 0);
            parts.get(1).broadcastMetadata(owner.getLocation());
        }
    }
}