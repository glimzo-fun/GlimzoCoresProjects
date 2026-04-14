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

public class CharizardAlly extends Ally {

    private static final String HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkzN2ZiYTBiMWU5ODg1ZmI0YTg0YzkxNTA1MTNkZWU4YjIxN2NkMDRmMTQwZDI1MDVjYWI4YWUzOWI1ZDQifX19";

    private static final ItemStack WING_SLAB   = new ItemStack(Material.STEP, 1, (short) 6);
    private static final ItemStack BODY_HELMET = LeatherColorUtil.dyedHelmet(232, 101, 10);
    private static final ItemStack TAIL_HELMET = LeatherColorUtil.dyedHelmet(200, 80, 10);

    // Part indices
    private static final int LEFT_WING        = 2;
    private static final int RIGHT_WING       = 3;
    private static final int LEFT_WING_INNER  = 4;
    private static final int RIGHT_WING_INNER = 5;

    private double wingAngle = 0;
    private double bobAngle  = 0;
    private int level = 1;

    public CharizardAlly(Player owner) {
        super(AllyType.CHARIZARD, owner);
    }

    public void setLevel(int level) { this.level = Math.max(1, Math.min(3, level)); }

    @Override public boolean isFloating()      { return true; }
    @Override public double  getSideOffset()   { return 0.7; }
    @Override public double  getBehindOffset() { return 0.2; }
    @Override public double  getHeightOffset() {
        return 1.5 + Math.sin(bobAngle) * 0.06;
    }

    // Level scaling: parts stay TIGHT, level just adds extra wing rows
    private float  flapAmp()   { return level >= 3 ? 32f : level == 2 ? 30f : 28f; }
    private double wingSpeed() { return level >= 3 ? 0.30 : level == 2 ? 0.25 : 0.20; }

    @Override
    protected List<AllyPart> buildParts(Location root) {
        List<AllyPart> list = new ArrayList<>();

        // [0] Head skull - on top of body
        AllyPart head = new AllyPart(root, 0, 0.40, 0, false, true);
        head.setHelmet(SkullBuilder.fromBase64(HEAD_TEXTURE));
        list.add(head);

        // [1] Body - orange leather helmet center
        AllyPart body = new AllyPart(root, 0, 0, 0, false, true);
        body.setHelmet(BODY_HELMET);
        list.add(body);

        // [2] Left wing OUTER slab - close to body, angled up
        AllyPart leftWing = new AllyPart(root, 0.42, 0.16, 0, false, true);
        leftWing.setHelmet(WING_SLAB);
        leftWing.setHeadPose(0, 0, 40);
        list.add(leftWing);

        // [3] Right wing OUTER slab - mirrored
        AllyPart rightWing = new AllyPart(root, -0.42, 0.16, 0, false, true);
        rightWing.setHelmet(WING_SLAB);
        rightWing.setHeadPose(0, 0, -40);
        list.add(rightWing);

        // [4] Left wing INNER slab - between body and outer, fills gap = looks wider
        AllyPart leftInner = new AllyPart(root, 0.18, 0.06, 0, false, true);
        leftInner.setHelmet(WING_SLAB);
        leftInner.setHeadPose(0, 0, 35);
        list.add(leftInner);

        // [5] Right wing INNER slab - mirrored
        AllyPart rightInner = new AllyPart(root, -0.18, 0.06, 0, false, true);
        rightInner.setHelmet(WING_SLAB);
        rightInner.setHeadPose(0, 0, -35);
        list.add(rightInner);

        // [6] Tail - behind and below
        AllyPart tail = new AllyPart(root, 0, -0.24, -0.28, false, true);
        tail.setHelmet(TAIL_HELMET);
        tail.setHeadPose(20, 0, 0);
        list.add(tail);

        return list;
    }

    @Override
    public void onTick() {
        bobAngle  += 0.10;
        wingAngle += wingSpeed();
        float flap = (float)(Math.sin(wingAngle) * flapAmp());

        if (parts.size() > RIGHT_WING_INNER) {
            parts.get(LEFT_WING).setHeadPose(0, 0, 40 + flap);
            parts.get(LEFT_WING).broadcastMetadata(owner.getLocation());
            parts.get(RIGHT_WING).setHeadPose(0, 0, -40 - flap);
            parts.get(RIGHT_WING).broadcastMetadata(owner.getLocation());
            parts.get(LEFT_WING_INNER).setHeadPose(0, 0, 35 + flap);
            parts.get(LEFT_WING_INNER).broadcastMetadata(owner.getLocation());
            parts.get(RIGHT_WING_INNER).setHeadPose(0, 0, -35 - flap);
            parts.get(RIGHT_WING_INNER).broadcastMetadata(owner.getLocation());
        }

        if (((int)(wingAngle * 5)) % 2 == 0) {
            Location wc = owner.getLocation().clone().add(0, getHeightOffset() + 0.1, 0);
            ParticleUtil.flame(owner, wc, 3);
        }
    }
}