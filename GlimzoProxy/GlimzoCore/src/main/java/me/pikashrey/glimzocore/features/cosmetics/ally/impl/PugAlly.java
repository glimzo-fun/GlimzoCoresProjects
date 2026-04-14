package me.pikashrey.glimzocore.features.cosmetics.ally.impl;

import me.pikashrey.glimzocore.features.cosmetics.ally.Ally;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPart;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.features.cosmetics.ally.SkullBuilder;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PugAlly extends Ally {

    private static final String HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmViZjdiNmUxZTY1MzRkODU4ZDRhZjA4MTM1OGM4ZTNjZGE3ZDQ4NzYxM2FiYjY1ODBhZmYzZjE4NTE0M2EifX19";

    private double bobAngle    = 0;

    // Backflip state
    private int    ticksSinceFlip   = 0;
    private static final int FLIP_INTERVAL = 60; // 3 seconds at 20 ticks/s
    private boolean flipping        = false;
    private double  flipAngle       = 0;   // 0→360 during flip
    private double  flipBaseY       = 0;
    private static final int FLIP_DURATION = 2; // 0.1 second to complete
    private int     flipTick        = 0;

    public PugAlly(Player owner) { super(AllyType.PUG, owner); }

    @Override public boolean isFloating()      { return false; }
    @Override public double  getHeightOffset() { return -0.5; }
    @Override public double  getSideOffset()   { return 0.9; }
    @Override public double  getBehindOffset() { return 0.3; }

    private int getLevel() {
        try {
            AllyPerkManager pm = GlimzoCore.getInstance().getCosmeticManager().getAllyPerkManager();
            return pm.getAllyLevel(owner, AllyType.PUG);
        } catch (Exception e) { return 1; }
    }

    @Override
    protected List<AllyPart> buildParts(Location root) {
        List<AllyPart> list = new ArrayList<>();
        AllyPart head = new AllyPart(root, 0, 0, 0, false, true);
        head.setHelmet(SkullBuilder.fromBase64(HEAD_TEXTURE));
        list.add(head);
        return list;
    }

    @Override
    public void onTick() {
        if (parts.isEmpty()) return;
        AllyPart head = parts.get(0);

        if (getLevel() >= 3) {
            ticksSinceFlip++;

            if (!flipping && ticksSinceFlip >= FLIP_INTERVAL) {
                // Start backflip
                flipping      = true;
                flipTick      = 0;
                flipAngle     = 0;
                flipBaseY     = currentY;
                ticksSinceFlip = 0;
            }

            if (flipping) {
                flipTick++;
                // Rotate pitch full 360 over FLIP_DURATION ticks
                float pitch = (float)((360.0 / FLIP_DURATION) * flipTick);
                // Arc: rise then fall using a sine curve
                double arc = Math.sin(Math.PI * flipTick / FLIP_DURATION) * 0.6;
                head.setHeadPose(pitch, 0, 0);
                head.broadcastMetadata(owner.getLocation());
                // Temporarily offset Y upward for the arc
                currentY = flipBaseY + arc;

                if (flipTick >= FLIP_DURATION) {
                    flipping  = false;
                    currentY  = flipBaseY;
                    head.setHeadPose(0, 0, 0);
                    head.broadcastMetadata(owner.getLocation());
                }
                return;
            }
        }

        // Normal idle wiggle
        bobAngle += 0.12;
        float wiggle = (float)(Math.sin(bobAngle) * 7);
        head.setHeadPose(0, wiggle, 0);
        head.broadcastMetadata(owner.getLocation());
    }
}