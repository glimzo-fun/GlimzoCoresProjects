package me.pikashrey.glimzocore.commands.debug;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Set;

public class PermCheckCommand implements CommandExecutor {

    private final GlimzoCore plugin;

    public PermCheckCommand(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("glimzocore.debug.permcheck")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /permcheck <player> <permission>");
            sender.sendMessage("§eUsage: /permcheck info <rank>");
            return true;
        }

        // /permcheck info <rank>
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                sender.sendMessage("§eUsage: /permcheck info <rank>");
                return true;
            }
            showRankInfo(sender, args[1]);
            return true;
        }

        // /permcheck <player> <permission>
        if (args.length < 2) {
            sender.sendMessage("§eUsage: /permcheck <player> <permission>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer '" + args[0] + "' is not online.");
            return true;
        }

        String permission = args[1];
        boolean bukkit  = target.hasPermission(permission);
        Set<String> resolved = plugin.getPermissionCache().getPlayer(target.getUniqueId());
        boolean cached = resolved != null && resolved.contains(permission);

        RankRef rank = plugin.getRankManager().getActiveRankRef(target.getUniqueId());

        sender.sendMessage("§8§m----------------------------------");
        sender.sendMessage("§6PermCheck §7for §f" + target.getName());
        sender.sendMessage("§7Active rank: §f" + rank.getColorCode() + rank.getDisplayName());
        sender.sendMessage("§7Permission: §f" + permission);
        sender.sendMessage("§7Bukkit hasPermission: " + (bukkit ? "§atrue" : "§cfalse"));
        sender.sendMessage("§7In resolved cache:    " + (cached ? "§atrue" : "§cfalse"));
        sender.sendMessage("§7Total cached perms:   §f" + (resolved != null ? resolved.size() : "§c(not cached)"));
        sender.sendMessage("§8§m----------------------------------");
        return true;
    }

    private void showRankInfo(CommandSender sender, String rankId) {
        ConfiguredRank cfg = plugin.getRankLoader().get(rankId.toLowerCase());
        if (cfg == null) {
            sender.sendMessage("§cUnknown rank: " + rankId);
            sender.sendMessage("§7Available: " + String.join(", ",
                    plugin.getRankLoader().getAll().stream()
                            .map(ConfiguredRank::getId)
                            .sorted()
                            .toArray(String[]::new)));
            return;
        }

        Set<String> flattened = plugin.getPermissionCache().getRank(cfg.getId());

        sender.sendMessage("§8§m----------------------------------");
        sender.sendMessage("§6Rank Info §8» §f" + cfg.getId());
        sender.sendMessage("§7Prefix:     §r" + cfg.getPrefix());
        sender.sendMessage("§7Weight:     §f" + cfg.getWeight());
        sender.sendMessage("§7Staff:      " + (cfg.isStaff() ? "§atrue" : "§cfalse"));
        sender.sendMessage("§7Donor:      " + (cfg.isDonor() ? "§atrue" : "§cfalse"));

        // Inheritance chain
        if (cfg.getInheritance().isEmpty()) {
            sender.sendMessage("§7Inherits:   §8(none)");
        } else {
            sender.sendMessage("§7Inherits:   §f" + String.join(" §8-> §f", cfg.getInheritance()));
        }

        // Own permissions (not inherited)
        if (cfg.getPermissions().isEmpty()) {
            sender.sendMessage("§7Own perms:  §8(none)");
        } else {
            sender.sendMessage("§7Own perms (" + cfg.getPermissions().size() + "):");
            for (String p : cfg.getPermissions()) {
                sender.sendMessage("  §8- §f" + p);
            }
        }

        // Flattened (including inherited)
        if (flattened == null || flattened.isEmpty()) {
            sender.sendMessage("§7Flattened:  §8(empty or not yet cached)");
        } else {
            sender.sendMessage("§7Flattened perms (" + flattened.size() + " total):");
            flattened.stream().sorted().forEach(p -> sender.sendMessage("  §8- §a" + p));
        }
        sender.sendMessage("§8§m----------------------------------");
    }
}
