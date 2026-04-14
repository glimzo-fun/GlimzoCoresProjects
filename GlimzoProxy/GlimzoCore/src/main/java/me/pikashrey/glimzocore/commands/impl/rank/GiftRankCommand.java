package me.pikashrey.glimzocore.commands.impl.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.api.rank.grant.Grant;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class GiftRankCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public GiftRankCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.rank.gift"; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (!a.has(1)) { a.usage("/giftrank <player> <rank>"); return; }

        OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(0));
        if (target == null || target.getName() == null) { a.sendError("Player not found."); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            a.sendError("You cannot gift a rank to yourself."); return;
        }

        String rankId = a.get(1).toLowerCase();
        RankRef rankRef = RankRef.of(rankId);
        if (!rankRef.isValid() || !rankRef.isDonor()) {
            String donorRanks = plugin.getRankLoader().getAll().stream()
                    .filter(ConfiguredRank::isDonor)
                    .map(ConfiguredRank::getId).sorted().collect(Collectors.joining(", "));
            a.sendError("Invalid donor rank. Valid donor ranks: " + donorRanks);
            return;
        }

        // Verify sender has an active non-staff grant of this rank
        boolean senderHasRank = plugin.getRankManager()
                .getActiveGrants(player.getUniqueId()).stream()
                .anyMatch(g -> g.getRankRef().getId().equals(rankRef.getId()) && !g.isStaffGrant());
        if (!senderHasRank) {
            a.sendError("You do not hold the &f" + rankRef.getDisplayName() + " &crank to gift.");
            return;
        }

        Grant grant = Grant.create(
                target.getUniqueId(), target.getName(),
                player.getUniqueId(), player.getName(),
                rankRef, -1L, false);

        boolean applied = plugin.getRankManager().addGrant(grant);
        if (!applied) { a.sendError("Could not gift rank (event cancelled)."); return; }

        a.sendSuccess("Gifted " + rankRef.getColorCode() + rankRef.getDisplayName()
                + " &ato &f" + target.getName() + "&a.");

        Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());
        if (onlineTarget != null) {
            onlineTarget.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate(
                    "&a" + player.getName() + " gifted you "
                    + rankRef.getColorCode() + rankRef.getDisplayName() + "&a!"));
        }
    }

    @Override
    public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 2) {
            return plugin.getRankLoader().getAll().stream()
                    .filter(ConfiguredRank::isDonor)
                    .map(ConfiguredRank::getId).sorted().collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
