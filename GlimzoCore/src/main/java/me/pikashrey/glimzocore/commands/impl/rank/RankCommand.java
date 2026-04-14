package me.pikashrey.glimzocore.commands.impl.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.api.rank.grant.Grant;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RankCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public RankCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) {
            if (!a.isPlayer()) { showHelp(a); return; }
            RankRef rank = plugin.getRankManager().getActiveRankRef(a.getPlayer());
            a.send("&7Your rank: " + rank.getColorCode() + rank.getDisplayName());
            return;
        }

        switch (a.get(0).toLowerCase()) {
            case "grant":  handleGrant(a);  break;
            case "revoke": handleRevoke(a); break;
            case "check":  handleCheck(a);  break;
            case "info":   handleInfo(a);   break;
            default:       showHelp(a);
        }
    }

    private void handleGrant(CommandArgs a) {
        if (!a.getSender().hasPermission("glimzo.rank.grant")) { a.noPermission(); return; }
        if (!a.has(2)) { a.usage("/rank grant <player> <rank> [duration]"); return; }

        OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(1));
        if (target == null || target.getName() == null) { a.sendError("Player not found."); return; }

        String rankId = a.get(2).toLowerCase();
        RankRef rankRef = RankRef.of(rankId);
        if (!rankRef.isValid()) {
            // Build list from config (authoritative) + enum fallback
            String available = plugin.getRankLoader().getAll().stream()
                    .map(ConfiguredRank::getId).sorted().collect(Collectors.joining(", "));
            a.sendError("Unknown rank '" + rankId + "'. Available: " + available);
            return;
        }

        String error = plugin.getRankSecurityValidator()
                .validateGrant(a.getSender(), target.getUniqueId(), rankRef);
        if (error != null) { a.sendError(error); return; }

        long duration = a.has(3) ? TimeFormatUtils.parse(a.get(3)) : -1;
        long expires  = (duration == -1) ? -1 : System.currentTimeMillis() + duration;

        Grant grant = Grant.create(
                target.getUniqueId(), target.getName(),
                a.isPlayer() ? a.getPlayer().getUniqueId() : null,
                a.getSender().getName(), rankRef, expires, true);

        plugin.getRankManager().addGrant(grant);
        a.sendSuccess("Granted " + rankRef.getColorCode() + rankRef.getDisplayName()
                + " &ato &f" + target.getName()
                + (duration == -1 ? " &a(permanent)" : " &afor &f" + a.get(3)));
    }

    private void handleRevoke(CommandArgs a) {
        if (!a.getSender().hasPermission("glimzo.rank.revoke")) { a.noPermission(); return; }
        if (!a.has(2)) { a.usage("/rank revoke <player> <rank>"); return; }

        OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(1));
        if (target == null || target.getName() == null) { a.sendError("Player not found."); return; }

        String rankId = a.get(2).toLowerCase();
        RankRef rankRef = RankRef.of(rankId);
        if (!rankRef.isValid()) {
            String available = plugin.getRankLoader().getAll().stream()
                    .map(me.pikashrey.glimzocore.features.rank.ConfiguredRank::getId)
                    .sorted().collect(Collectors.joining(", "));
            a.sendError("Unknown rank '" + rankId + "'. Available: " + available);
            return;
        }

        // Find the most recent active grant for this player+rank and revoke it
        List<Grant> grants = plugin.getRankManager().getAllGrants(target.getUniqueId());
        Grant toRevoke = null;
        for (Grant g : grants) {
            if (g.isActive() && g.getRankRef().getId().equalsIgnoreCase(rankId)) {
                toRevoke = g;
                break;
            }
        }

        if (toRevoke == null) {
            a.sendError(target.getName() + " does not have an active " + rankRef.getDisplayName() + " grant.");
            return;
        }

        plugin.getRankManager().revokeGrant(target.getUniqueId(), toRevoke.getId());
        a.sendSuccess("Revoked " + rankRef.getColorCode() + rankRef.getDisplayName()
                + " &afrom &f" + target.getName() + "&a.");
    }

    private void handleCheck(CommandArgs a) {
        if (!a.getSender().hasPermission("glimzo.rank.history")) { a.noPermission(); return; }
        if (!a.has(1)) { a.usage("/rank check <player>"); return; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(a.get(1));
        if (target == null) { a.sendError("Player not found."); return; }

        // Online player + staff is a player → open GrantsMenu GUI
        if (a.isPlayer()) {
            org.bukkit.entity.Player online = Bukkit.getPlayer(target.getUniqueId());
            if (online != null) {
                new me.pikashrey.glimzocore.menus.grant.GrantsMenu(
                        plugin, a.getPlayer(), target.getUniqueId(), target.getName()).open();
                return;
            }
        }

        // Console or offline target - text fallback
        List<Grant> grants = plugin.getRankManager().getAllGrants(target.getUniqueId());
        if (grants.isEmpty()) { a.send("&7" + target.getName() + " has no grants."); return; }
        a.send("&7Grants for &f" + target.getName() + "&7:");
        for (Grant g : grants) {
            String status = g.isActive() ? "&a[ACTIVE]" : "&c[INACTIVE]";
            a.send("  " + status + " &7#" + g.getId()
                    + " " + g.getRankRef().getColorCode() + g.getRankRef().getDisplayName()
                    + " &7by &f" + g.getIssuedByName()
                    + " &7(" + (g.isPermanent() ? "permanent"
                                               : TimeFormatUtils.formatExpiry(g.getExpiresAt())) + ")");
        }
    }

    private void handleInfo(CommandArgs a) {
        if (!a.getSender().hasPermission("glimzo.rank.info")) { a.noPermission(); return; }
        if (!a.has(1)) { a.usage("/rank info <rank>"); return; }

        String id = a.get(1).toLowerCase();
        ConfiguredRank cfg = plugin.getRankLoader().get(id);
        if (cfg == null) {
            a.sendError("Unknown rank '" + id + "'. Use /rank info <rank> where rank is in ranks.yml.");
            return;
        }

        Set<String> perms = plugin.getPermissionCache().getRank(id);

        a.send("&8&m---------------------------");
        a.send("&6Rank Info &8» &f" + cfg.getId());
        a.send("&7Prefix:     &r" + cfg.getPrefix());
        a.send("&7Weight:     &f" + cfg.getWeight());
        a.send("&7Staff:      " + (cfg.isStaff() ? "&atrue" : "&cfalse"));
        a.send("&7Donor:      " + (cfg.isDonor() ? "&atrue" : "&cfalse"));
        if (cfg.getInheritance().isEmpty()) {
            a.send("&7Inherits:   &8(none)");
        } else {
            a.send("&7Inherits:   &f" + String.join(" &8-> &f", cfg.getInheritance()));
        }
        a.send("&7Own perms (" + cfg.getPermissions().size() + "): &f"
                + String.join(", ", cfg.getPermissions()));
        a.send("&7Flattened (" + (perms != null ? perms.size() : 0) + " total):");
        if (perms != null) {
            perms.stream().sorted().forEach(p -> a.send("  &8- &f" + p));
        }
        a.send("&8&m---------------------------");
    }

    private void showHelp(CommandArgs a) {
        a.send("&6/rank &7- Show your rank");
        a.send("&6/rank check <player> &7- View grants");
        a.send("&6/rank grant <player> <rank> [duration] &7- Grant a rank");
        a.send("&6/rank revoke <player> <rank> &7- Revoke a rank grant");
        a.send("&6/rank info <rank> &7- Show rank metadata and permissions");
    }

    @Override
    public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1) return Arrays.asList("grant", "revoke", "check", "info");
        if (a.length() == 3 && a.get(0).equalsIgnoreCase("grant")) {
            // Tab-complete from config (all ranks), not just the enum
            return plugin.getRankLoader().getAll().stream()
                    .map(ConfiguredRank::getId).sorted().collect(Collectors.toList());
        }
        if (a.length() == 3 && a.get(0).equalsIgnoreCase("revoke")) {
            return plugin.getRankLoader().getAll().stream()
                    .map(ConfiguredRank::getId).sorted().collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
