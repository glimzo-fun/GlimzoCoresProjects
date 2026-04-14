package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.Ally;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPart;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MrPandaAlly extends Ally {

    private static final String HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTk1ODU5ZWZkYjRmNzYyNmJjMjA1NTM0MWNkMmZhYWIzY2MwNjAyYzhhY2I1YzkxNDg1ZmRiYmFlMzExMzI1NCJ9fX0=";
    private static final String WHITE_SKULL =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2JkMjllNzNmNWFlYzE1OTQ4NmQ4OGVhZWU5MDlhZTAyMjdmMzJiNWYxZjhmNDEzNDJlYjAzMDkwOWRkNjVlIn19fQ==";
    private static final String BLACK_SKULL =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJmNGZhYjQzYWE5NTExY2UwYzM4MGIwNWNhNGVmYTQ1MzFhOWZiNWM5MWEyNDFiOTQ4YWIyMmFhNDY2YSJ9fX0=";

    private double wiggleAngle = 0;

    public MrPandaAlly(Player owner) { super(AllyType.MR_PANDA, owner); }

    @Override public boolean isFloating()      { return false; }
    @Override public double  getHeightOffset() { return 0.0; }
    @Override public double  getSideOffset()   { return 1.2; }
    @Override public double  getBehindOffset() { return 0.5; }

    @Override
    protected List<AllyPart> buildParts(Location root) {
        List<AllyPart> list = new ArrayList<>();

        // [0] Face - panda head skull, grounded
        AllyPart face = new AllyPart(root, 0, -0.700, 0, false, false);
        face.setHelmet(SkullBuilder.fromBase64(HEAD_TEXTURE));
        list.add(face);

        // [1] Body - white skull directly below head
        AllyPart body = new AllyPart(root, 0, -1.300, 0, false, false);
        body.setHelmet(SkullBuilder.fromBase64(WHITE_SKULL));
        list.add(body);

        // [2] Left hand - small skull beside face, same height, leaning forward
        AllyPart leftHand = new AllyPart(root, -0.55, -0.175, 0.10, false, true);
        leftHand.setHelmet(SkullBuilder.fromBase64(BLACK_SKULL));
        leftHand.setHeadPose(30, 0, 10);
        list.add(leftHand);

        // [2] Right hand - mirrored
        AllyPart rightHand = new AllyPart(root, 0.55, -0.175, 0.10, false, true);
        rightHand.setHelmet(SkullBuilder.fromBase64(BLACK_SKULL));
        rightHand.setHeadPose(30, 0, -10);
        list.add(rightHand);

        return list;
    }

    @Override
    public void onTick() {
        wiggleAngle += 0.06;
        float wobble = (float)(Math.sin(wiggleAngle) * 6);
        if (!parts.isEmpty()) {
            parts.get(0).setHeadPose(0, wobble, 0);
            parts.get(0).broadcastMetadata(owner.getLocation());
        }
    }
}