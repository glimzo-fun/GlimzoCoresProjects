package me.pikashrey.glimzocore.features.rank.security;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.api.rank.RankRef;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class RankSecurityValidator {

    private final GlimzoCore plugin;
    private final RankCooldownManager cooldowns;

    public RankSecurityValidator(GlimzoCore plugin, RankCooldownManager cooldowns) {
        this.plugin = plugin;
        this.cooldowns = cooldowns;
    }

    private int dangerousThreshold() {
        return plugin.getConfig().getInt("ranks.dangerous-threshold-weight", 1500);
    }

    public String validateGrant(CommandSender staff, UUID targetUuid, RankRef targetRank) {
        if (!(staff instanceof Player)) return null;

        Player player = (Player) staff;
        UUID staffUuid = player.getUniqueId();

        if (!cooldowns.canExecute(staffUuid, targetRank.getWeight())) {
            long ladderMs = cooldowns.getLadderCooldownRemaining(staffUuid);
            if (ladderMs > 0) {
                long secs = (ladderMs / 1000) + 1;
                return "Ladder cooldown active. Wait " + secs + "s before granting high-tier ranks again.";
            }
            return "You are granting ranks too quickly (max 4 per minute). Please slow down.";
        }

        if (staffUuid.equals(targetUuid)) {
            return "You cannot grant ranks to yourself.";
        }

        RankRef staffRank = plugin.getRankManager().getActiveRankRef(staffUuid);
        if (staffRank.getId().equalsIgnoreCase(targetRank.getId())) {
            if (!player.hasPermission("glimzocore.rank.grant.samerank")) {
                return "You cannot grant your own rank to another player without glimzocore.rank.grant.samerank.";
            }
        }

        if (staffRank.getWeight() <= targetRank.getWeight()) {
            return "You cannot grant a rank equal to or higher than your own ("
                    + staffRank.getDisplayName() + ", weight " + staffRank.getWeight() + ").";
        }

        Set<String> flattened = plugin.getPermissionCache().getRank(targetRank.getId());
        if (flattened != null && flattened.stream().anyMatch(p -> p.equals("*") || p.endsWith(".*"))) {
            if (!player.hasPermission("glimzocore.rank.grant.wildcard")) {
                return "This rank contains wildcard permissions and requires glimzocore.rank.grant.wildcard.";
            }
        }

        if (targetRank.getWeight() >= dangerousThreshold()
                && !player.hasPermission("glimzocore.rank.grant.dangerous")) {
            return "Granting this high-level rank requires glimzocore.rank.grant.dangerous.";
        }

        return null;
    }

    public String validateGrant(CommandSender staff, UUID targetUuid, Rank targetRank) {
        return validateGrant(staff, targetUuid, RankRef.of(targetRank));
    }
}
