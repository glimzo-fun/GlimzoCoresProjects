package me.pikashrey.glimzocore120.commands;

import me.pikashrey.glimzocore120.GlimzoCore120;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinsCommand120 implements CommandExecutor {

    private final GlimzoCore120 plugin;

    public CoinsCommand120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            // Show own coins
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /coins <player>");
                return true;
            }
            long coins = plugin.getCoinManager().getCoins(player.getUniqueId());
            player.sendMessage(Component.text("You have ", NamedTextColor.GRAY)
                    .append(Component.text(coins + " coins", NamedTextColor.GOLD)));
            return true;
        }

        // /coins add/remove/set <player> <amount> - staff only
        if (args.length == 3 && sender.hasPermission("glimzo.coins.admin")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return true;
            }
            long amount;
            try { amount = Long.parseLong(args[2]); }
            catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "add"    -> plugin.getCoinManager().addCoins(target.getUniqueId(), amount, "admin");
                case "remove" -> plugin.getCoinManager().removeCoins(target.getUniqueId(), amount, "admin");
                case "set"    -> plugin.getCoinManager().setCoins(target.getUniqueId(), amount);
                default       -> sender.sendMessage("Usage: /coins <add|remove|set> <player> <amount>");
            }
            sender.sendMessage(Component.text("Done!", NamedTextColor.GREEN));
        }
        return true;
    }
}
