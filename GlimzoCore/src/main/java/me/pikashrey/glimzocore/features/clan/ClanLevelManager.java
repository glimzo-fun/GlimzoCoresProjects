package me.pikashrey.glimzocore.features.clan;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.clan.ClanLevel;
import me.pikashrey.glimzocore.api.clan.ClanPerk;
import me.pikashrey.glimzocore.api.events.impl.ClanLevelUpEvent;
import me.pikashrey.glimzocore.api.events.impl.ClanXpGainEvent;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanLevelManager {

    protected final GlimzoCore plugin;

    public ClanLevelManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    /** Returns the active XP boost multiplier for a player's clan (0.0 if none). */
    public double getXpBoost(UUID playerUuid) {
        ClanData clan = plugin.getClanManager().getClanByPlayer(playerUuid);
        if (clan == null) return 0.0;
        return clan.getClanLevel().getPerkValue(ClanPerk.XP_BOOST);
    }

    /** Returns the active coin boost multiplier (0.0 if none). */
    public double getCoinBoost(UUID playerUuid) {
        ClanData clan = plugin.getClanManager().getClanByPlayer(playerUuid);
        if (clan == null) return 0.0;
        return clan.getClanLevel().getPerkValue(ClanPerk.COIN_BOOST);
    }

    /** Returns the active gem boost multiplier (0.0 if none). */
    public double getGemBoost(UUID playerUuid) {
        ClanData clan = plugin.getClanManager().getClanByPlayer(playerUuid);
        if (clan == null) return 0.0;
        return clan.getClanLevel().getPerkValue(ClanPerk.GEM_BOOST);
    }

    public long applyXpBoost(UUID playerUuid, long base) {
        double boost = getXpBoost(playerUuid);
        return base + (long) (base * boost);
    }

    public long applyCoinBoost(UUID playerUuid, long base) {
        double boost = getCoinBoost(playerUuid);
        return base + (long) (base * boost);
    }

    public long applyGemBoost(UUID playerUuid, long base) {
        double boost = getGemBoost(playerUuid);
        return base + (long) (base * boost);
    }

    // Level-up broadcast

    public void broadcastLevelUp(ClanData clan, int newLevel) {
        ClanLevel level = ClanLevel.forLevel(newLevel);
        String msg = CC.translate(
                "&8[&aClan&8] &f[" + clan.getTag() + "] " + clan.getName()
                + " &ahas reached &6Level " + newLevel + "&a!");

        String perksMsg = buildPerkUnlockMessage(level);

        for (me.pikashrey.glimzocore.api.clan.ClanMember member : clan.getMembers()) {
            Player online = Bukkit.getPlayer(member.getUuid());
            if (online != null) {
                online.sendMessage(msg);
                if (perksMsg != null) online.sendMessage(perksMsg);
            }
        }
    }

    private String buildPerkUnlockMessage(ClanLevel level) {
        StringBuilder sb = new StringBuilder();
        for (ClanPerk perk : ClanPerk.values()) {
            if (level.hasPerk(perk) && level.getPerkValue(perk) > 0) {
                if (sb.length() > 0) sb.append(", ");
                double val = level.getPerkValue(perk);
                if (perk == ClanPerk.INCREASED_CAPACITY) {
                    sb.append(perk.getDisplayName()).append(" +").append((int) val);
                } else {
                    sb.append(perk.getDisplayName()).append(" +").append((int)(val*100)).append("%");
                }
            }
        }
        if (sb.length() == 0) return null;
        return CC.translate("  &7New perks: &a" + sb);
    }
}

