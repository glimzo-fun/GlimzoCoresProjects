package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.Ally;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPart;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.features.cosmetics.ally.LeatherColorUtil;
import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import me.pikashrey.glimzocore.utilities.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FalconAlly extends Ally {

    private static final String HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDE2ZjU5NWU4ZjY3OTFiYzE1NDY1OWE4OTc2ZjZhOGZmZDk4NDdjZjc1YTJiZjYzOTkyZTNhNjU1ZTAifX19";

    // Jungle wood slab - warm brown wing panel ye
    private static final ItemStack WING_SLAB   = new ItemStack(Material.WOOD_STEP, 1, (short) 3);
    private static final ItemStack BODY_HELMET = LeatherColorUtil.dyedHelmet(123, 90, 46);

    private static final int LEFT_WING  = 2;
    private static final int RIGHT_WING = 3;

    private double wingAngle = 0;
    private double bobAngle  = 0;
    private int level = 1;

    public FalconAlly(Player owner) {
        super(AllyType.FALCON, owner);
    }

    public void setLevel(int level) { this.level = Math.max(1, Math.min(3, level)); }

    @Override public boolean isFloating()      { return true; }
    @Override public double  getSideOffset()   { return 0.7; }
    @Override public double  getBehindOffset() { return 0.2; }
    @Override public double  getHeightOffset() {
        return 1.35 + Math.sin(bobAngle) * 0.05; // same as Charizard - just above head
    }

    private double scale() {
        switch (level) { case 2: return 1.2; case 3: return 1.6; default: return 1.0; }
    }
    private float  flapAmp()   { return level >= 3 ? 24f : level == 2 ? 22f : 20f; }
    private double wingSpeed() { return level >= 3 ? 0.35 : level == 2 ? 0.30 : 0.25; }

    @Override
    protected List<AllyPart> buildParts(Location root) {
        List<AllyPart> list = new ArrayList<>();
        double s = scale();

        // [0] Head skull
        AllyPart head = new AllyPart(root, 0, 0.32 * s, 0, false, true);
        head.setHelmet(SkullBuilder.fromBase64(HEAD_TEXTURE));
        list.add(head);

        // [1] Body
        AllyPart body = new AllyPart(root, 0, 0, 0, false, true);
        body.setHelmet(BODY_HELMET);
        list.add(body);

        // [2] Left wing slab - close to body, barely above body center
        AllyPart leftWing = new AllyPart(root, 0.34 * s, 0.04 * s, 0, false, true);
        leftWing.setHelmet(WING_SLAB);
        leftWing.setHeadPose(0, 0, 35);
        list.add(leftWing);

        // [3] Right wing - mirrored
        AllyPart rightWing = new AllyPart(root, -0.34 * s, 0.04 * s, 0, false, true);
        rightWing.setHelmet(WING_SLAB);
        rightWing.setHeadPose(0, 0, -35);
        list.add(rightWing);

        return list;
    }

    @Override
    public void onTick() {
        bobAngle  += 0.10;
        wingAngle += wingSpeed();
        float flap = (float)(Math.sin(wingAngle) * flapAmp());

        if (parts.size() > RIGHT_WING) {
            parts.get(LEFT_WING).setHeadPose(0, 0, 35 + flap);
            parts.get(LEFT_WING).updateMetadata(owner);
            parts.get(RIGHT_WING).setHeadPose(0, 0, -35 - flap);
            parts.get(RIGHT_WING).updateMetadata(owner);
        }

        if (((int)(wingAngle * 4)) % 2 == 0) {
            Location wc = owner.getLocation().clone().add(0, getHeightOffset() + 0.1, 0);
            ParticleUtil.crit(owner, wc, 2);
        }
    }
}
