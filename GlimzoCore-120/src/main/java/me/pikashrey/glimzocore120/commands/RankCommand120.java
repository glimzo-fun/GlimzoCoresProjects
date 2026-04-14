package me.pikashrey.glimzocore120.commands;

import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore120.GlimzoCore120;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand120 implements CommandExecutor {

    private final GlimzoCore120 plugin;

    public RankCommand120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // /rank set <player> <rank>
        if (args.length == 3 && args[0].equalsIgnoreCase("set")
                && sender.hasPermission("glimzo.rank.set")) {

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return true;
            }

            Rank rank = Rank.fromId(args[2]);
            if (rank == null) {
                sender.sendMessage(Component.text("Invalid rank: " + args[2], NamedTextColor.RED));
                return true;
            }

            plugin.getRankManager().setRank(target, rank);
            sender.sendMessage(Component.text(
                    "Set " + target.getName() + "'s rank to " + rank.getId(), NamedTextColor.GREEN));
            return true;
        }

        // /rank <player> - check rank
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return true;
            }
            Rank rank = plugin.getRankManager().getRank(target);
            sender.sendMessage(Component.text(target.getName() + "'s rank: ", NamedTextColor.GRAY)
                    .append(Component.text(rank.getChatPrefix(), NamedTextColor.WHITE)));
            return true;
        }

        sender.sendMessage("Usage: /rank <player> | /rank set <player> <rank>");
        return true;
    }
}
