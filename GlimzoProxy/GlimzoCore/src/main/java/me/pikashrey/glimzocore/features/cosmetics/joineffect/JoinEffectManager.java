package me.pikashrey.glimzocore.features.cosmetics.joineffect;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;

public class JoinEffectManager {

    private static final Random RANDOM = new Random();

    private final GlimzoCore plugin;

    public JoinEffectManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void triggerJoinEffect(Player player, PlayerCosmeticState state,
                                   String rankPrefix, String tagFormatted) {
        String effectId = state.getActiveJoinEffectId();
        if (effectId == null) return;

        JoinEffectType effect = JoinEffectType.fromId(effectId);
        if (effect == null) return;

        if (!player.hasPermission(effect.getPermission())
                && !player.hasPermission("glimzo.joineffect.*")) {
            return;
        }

        String message = resolveJoinMessage(state);

        // Broadcast join message to all players
        broadcastJoinMessage(player, rankPrefix, tagFormatted, message, effect);

        // Play effect 1 tick later so the world is ready
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playEffect(player, effect);
        }, 1L);
    }

    private void playEffect(Player player, JoinEffectType effect) {
        Location loc = player.getLocation();

        switch (effect) {
            case LIGHTNING:
                playLightningEffect(player, loc);
                break;
            case DRAGON:
                playDragonEffect(player, loc);
                break;
            case METEOR:
                playMeteorEffect(player, loc);
                break;
        }
    }

    private void playLightningEffect(Player player, Location loc) {
        // Visual lightning at player location (no damage - just the effect)
        loc.getWorld().strikeLightningEffect(loc);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(loc, Sound.AMBIENCE_THUNDER, 1.0f, 1.0f);
        }
    }

    private void playDragonEffect(Player player, Location loc) {
        // Dragon roar: enderdragon growl sound + explosion effect
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(loc, Sound.ENDERDRAGON_GROWL, 1.0f, 0.8f);
        }
        loc.getWorld().playEffect(loc, Effect.EXPLOSION_LARGE, 0);
        loc.getWorld().playEffect(loc.clone().add(0, 1, 0), Effect.EXPLOSION_LARGE, 0);
    }

    private void playMeteorEffect(Player player, Location loc) {
        // Meteor crash: fireball-like explosion sound + visual effects
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(loc, Sound.EXPLODE, 1.0f, 0.6f);
        }
        loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 0);
        loc.getWorld().playEffect(loc.clone().add(0, 1, 0), Effect.SMOKE, 4);
        loc.getWorld().playEffect(loc.clone().add(0, 2, 0), Effect.SMOKE, 4);
    }

    private void broadcastJoinMessage(Player player, String rankPrefix,
                                       String tagFormatted, String message,
                                       JoinEffectType effect) {
        String playerName = player.getName();

        // Build the name+tag section: "PlayerName [Tag]" or just "PlayerName"
        String nameSection;
        if (tagFormatted != null && !tagFormatted.isEmpty()) {
            nameSection = "&f" + playerName + " " + tagFormatted;
        } else {
            nameSection = "&f" + playerName;
        }

        // Build rank prefix part
        String prefix = (rankPrefix != null && !rankPrefix.isEmpty())
                ? CC.translate(rankPrefix) + " "
                : "";

        String line = CC.translate("&6Kaboom!! &f" + prefix + nameSection + " &7" + message);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(line);
        }
    }

    private String resolveJoinMessage(PlayerCosmeticState state) {
        // If the player has a specific join message pinned, use it
        String pinnedId = state.getActiveJoinMessageId();
        if (pinnedId != null) {
            try {
                int idx = Integer.parseInt(pinnedId);
                if (idx >= 0 && idx < JoinMessage.ALL.size()) {
                    return JoinMessage.ALL.get(idx);
                }
            } catch (NumberFormatException ignored) {}
        }
        // Otherwise random
        return JoinMessage.ALL.get(RANDOM.nextInt(JoinMessage.ALL.size()));
    }

    /**
     * Select an effect for a player. Returns false if no permission or not found.
     */
    public boolean selectEffect(Player player, PlayerCosmeticState state, String effectId) {
        if (effectId == null) {
            state.setActiveJoinEffectId(null);
            return true;
        }
        JoinEffectType effect = JoinEffectType.fromId(effectId);
        if (effect == null) return false;
        if (!player.hasPermission(effect.getPermission())
                && !player.hasPermission("glimzo.joineffect.*")) {
            return false;
        }
        state.setActiveJoinEffectId(effectId);
        return true;
    }

    /**
     * Select a specific join message for a player by its list index.
     */
    public boolean selectJoinMessage(Player player, PlayerCosmeticState state, int index) {
        if (index < 0 || index >= JoinMessage.ALL.size()) return false;
        state.setActiveJoinMessageId(String.valueOf(index));
        return true;
    }

    public void clearEffect(PlayerCosmeticState state) {
        state.setActiveJoinEffectId(null);
    }

    public void clearJoinMessage(PlayerCosmeticState state) {
        state.setActiveJoinMessageId(null);
    }

    public JoinEffectType[] getEffects() {
        return JoinEffectType.values();
    }
}
