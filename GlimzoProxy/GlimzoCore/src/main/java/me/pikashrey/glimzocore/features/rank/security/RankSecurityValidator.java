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
    private final RankCooldownManager cooldownManager;

    /** Weight threshold above which a grant requires the .dangerous permission - read from config. */
    private int getDangerousThreshold() {
        return plugin.getConfig().getInt("ranks.dangerous-threshold-weight", 1500);
    }

    public RankSecurityValidator(GlimzoCore plugin, RankCooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    // Primary validation entry point

    public String validateGrant(CommandSender staff, UUID targetUuid, RankRef targetRank) {

        // Console bypasses every restriction
        if (!(staff instanceof Player)) return null;

        Player player = (Player) staff;
        UUID staffUuid = player.getUniqueId();

        // --- 1. Rate limit + ladder escalation ---
        if (!cooldownManager.canExecute(staffUuid, targetRank.getWeight())) {
            long ladderMs = cooldownManager.getLadderCooldownRemaining(staffUuid);
            if (ladderMs > 0) {
                long secs = (ladderMs / 1000) + 1;
                return "Ladder cooldown active. Wait " + secs + "s before granting high-tier ranks again.";
            }
            return "You are granting ranks too quickly (max 4 per minute). Please slow down.";
        }

        // --- 2. Self-grant: UUID match ---
        if (staffUuid.equals(targetUuid)) {
            return "You cannot grant ranks to yourself.";
        }

        // --- 3. Self-rank bypass: same rank as issuer's active rank ---
        //
        // Prevents: /rank grant <alt_account> admin   (where staff is already admin)
        // Also prevents the edge case: /rank grant admin admin
        // where 'admin' refers to an offline player whose name matches a rank id.
        //
        // If the target rank is the SAME as the staff member's current active rank,
        // they must hold glimzocore.rank.grant.samerank to proceed.
        RankRef staffRank = plugin.getRankManager().getActiveRankRef(staffUuid);
        if (staffRank.getId().equalsIgnoreCase(targetRank.getId())) {
            if (!player.hasPermission("glimzocore.rank.grant.samerank")) {
                return "You cannot grant your own rank to another player without glimzocore.rank.grant.samerank.";
            }
        }

        // --- 4. Hierarchy: staff weight must strictly exceed target rank weight ---
        if (staffRank.getWeight() <= targetRank.getWeight()) {
            return "You cannot grant a rank equal to or higher than your own ("
                    + staffRank.getDisplayName() + ", weight " + staffRank.getWeight() + ").";
        }

        // --- 5. Wildcard node protection ---
        Set<String> flattened = plugin.getPermissionCache().getRank(targetRank.getId());
        if (flattened != null
                && flattened.stream().anyMatch(p -> p.equals("*") || p.endsWith(".*"))) {
            if (!player.hasPermission("glimzocore.rank.grant.wildcard")) {
                return "This rank contains wildcard permissions and requires glimzocore.rank.grant.wildcard.";
            }
        }

        // --- 6. Dangerous rank gate ---
        if (targetRank.getWeight() >= getDangerousThreshold()
                && !player.hasPermission("glimzocore.rank.grant.dangerous")) {
            return "Granting this high-level rank requires glimzocore.rank.grant.dangerous.";
        }

        return null; // all checks passed
    }

    /**
     * Legacy overload accepting the Rank enum - delegates to the RankRef variant.
     */
    public String validateGrant(CommandSender staff, UUID targetUuid, Rank targetRank) {
        return validateGrant(staff, targetUuid, RankRef.of(targetRank));
    }
}
