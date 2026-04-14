package me.pikashrey.glimzocore.utilities;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ParticleUtil {

    public static void spawn(Player player, EnumParticle particle, Location loc,
                             int count, float offsetX, float offsetY, float offsetZ,
                             float speed, int... data) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particle,
                true,                          // long distance - always show regardless of render distance
                (float) loc.getX(),
                (float) loc.getY(),
                (float) loc.getZ(),
                offsetX, offsetY, offsetZ,
                speed,
                count,
                data
        );
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Sends a particle packet to all players in the given iterable.
     */
    public static void spawnForAll(Iterable<Player> players, EnumParticle particle, Location loc,
                                   int count, float offsetX, float offsetY, float offsetZ,
                                   float speed, int... data) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particle, true,
                (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
                offsetX, offsetY, offsetZ,
                speed, count, data
        );
        for (Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    /**
     * Convenience - spawn for a single player with no spread (exact position).
     */
    public static void spawnExact(Player player, EnumParticle particle, Location loc,
                                  int count, float speed) {
        spawn(player, particle, loc, count, 0f, 0f, 0f, speed);
    }

    // Shorthand helpers used by the cosmetics system

    /** Flame particle - used by Charizard wings */
    public static void flame(Player player, Location loc, int count) {
        spawn(player, EnumParticle.FLAME, loc, count, 0.3f, 0.15f, 0.3f, 0.01f);
    }

    /** Crit particle - used by Falcon wings (white sparkle / wind) */
    public static void crit(Player player, Location loc, int count) {
        spawn(player, EnumParticle.CRIT, loc, count, 0.3f, 0.1f, 0.1f, 0.01f);
    }

    /** Enchantment table runes - used by Energy wings */
    public static void enchant(Player player, Location loc, int count) {
        spawn(player, EnumParticle.ENCHANTMENT_TABLE, loc, count, 0.5f, 0.6f, 0.5f, 0.05f);
    }

    /** Witch spell particles - used by Butterfly wings */
    public static void witchSpell(Player player, Location loc, int count) {
        spawn(player, EnumParticle.SPELL_WITCH, loc, count, 0.2f, 0.2f, 0.2f, 0.01f);
    }

    /** Magic crit - used by Angel wings */
    public static void magicCrit(Player player, Location loc, int count) {
        spawn(player, EnumParticle.CRIT_MAGIC, loc, count, 0.3f, 0.2f, 0.3f, 0.01f);
    }

    /** Smoke - used by Dragon wings */
    public static void smoke(Player player, Location loc, int count) {
        spawn(player, EnumParticle.SMOKE_NORMAL, loc, count, 0.2f, 0.2f, 0.2f, 0.01f);
    }

    // Ally perk particle effects

    /** Fire particles - used by Charizard ally */
    public static void fireParticles(Location loc, int count) {
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distance(loc) <= 50) {
                spawn(p, EnumParticle.FLAME, loc, count, 0.3f, 0.3f, 0.3f, 0.05f);
            }
        }
    }

    /** Water particles - used by Dr. Ducky ally */
    public static void waterParticles(Location loc, int count) {
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distance(loc) <= 50) {
                spawn(p, EnumParticle.WATER_SPLASH, loc, count, 0.4f, 0.2f, 0.4f, 0.05f);
            }
        }
    }

    /** Smoke particles - used by Charizard ally */
    public static void smokeParticles(Location loc, int count) {
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distance(loc) <= 50) {
                spawn(p, EnumParticle.SMOKE_NORMAL, loc, count, 0.3f, 0.4f, 0.3f, 0.03f);
            }
        }
    }

    /** Paw particles - used by Kitty ally */
    public static void pawParticles(Location loc, int count) {
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distance(loc) <= 50) {
                spawn(p, EnumParticle.FOOTSTEP, loc, count, 0.3f, 0.1f, 0.3f, 0.05f);
            }
        }
    }

    /** Heart particles - used by Pug ally */
    public static void heartParticles(Location loc, int count) {
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distance(loc) <= 50) {
                spawn(p, EnumParticle.HEART, loc, count, 0.5f, 0.5f, 0.5f, 0.1f);
            }
        }
    }
}