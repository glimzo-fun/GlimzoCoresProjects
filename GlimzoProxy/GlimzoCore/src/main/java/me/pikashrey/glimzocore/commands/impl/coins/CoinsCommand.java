package me.pikashrey.glimzocore.commands.impl.coins;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CoinsCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public CoinsCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) {
            // Show own coins
            if (!a.isPlayer()) { a.usage("/coins <give|take|set> <player> <amount>"); return; }
            PlayerData data = GlobalPlayer.get(a.getPlayer());
            a.send("&6You have &e" + (data != null ? data.getCoins() : 0) + " &6coins.");
            return;
        }

        if (!a.getSender().hasPermission("glimzo.coins.admin")) { a.noPermission(); return; }

        String sub = a.get(0).toLowerCase();
        if (!a.has(2)) { a.usage("/coins <give|take|set|check> <player> [amount]"); return; }
        Player target = Bukkit.getPlayer(a.get(1));
        if (target == null) { a.sendError("Player not online."); return; }

        switch (sub) {
            case "check":
                PlayerData d = GlobalPlayer.get(target);
                a.send("&f" + target.getName() + " &6has &e" + (d != null ? d.getCoins() : 0) + " &6coins.");
                break;
            case "give":
                long give = parseLong(a.get(2));
                if (give <= 0) { a.sendError("Amount must be positive."); return; }
                plugin.getCoinManager().addCoins(target.getUniqueId(), give, "Admin grant");
                a.sendSuccess("Gave &e" + give + " &acoins to &f" + target.getName());
                break;
            case "take":
                long take = parseLong(a.get(2));
                if (take <= 0) { a.sendError("Amount must be positive."); return; }
                plugin.getCoinManager().removeCoins(target.getUniqueId(), take, "Admin remove");
                a.sendSuccess("Removed &e" + take + " &acoins from &f" + target.getName());
                break;
            case "set":
                long set = parseLong(a.get(2));
                plugin.getCoinManager().setCoins(target.getUniqueId(), set);
                a.sendSuccess("Set &f" + target.getName() + "&a's coins to &e" + set);
                break;
            default:
                a.usage("/coins <give|take|set|check> <player> <amount>");
        }
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return -1; }
    }
}
