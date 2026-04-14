package me.pikashrey.glimzocore.commands.impl.gems;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GemsCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public GemsCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) {
            if (!a.isPlayer()) { a.usage("/gems <give|take|set|check> <player> [amount]"); return; }
            PlayerData data = GlobalPlayer.get(a.getPlayer());
            a.send("&5You have &d" + (data != null ? data.getGems() : 0) + " &5gems.");
            return;
        }
        if (!a.getSender().hasPermission("glimzo.gems.admin")) { a.noPermission(); return; }
        if (!a.has(2)) { a.usage("/gems <give|take|set|check> <player> [amount]"); return; }

        String sub = a.get(0).toLowerCase();
        Player target = Bukkit.getPlayer(a.get(1));
        if (target == null) { a.sendError("Player not online."); return; }

        switch (sub) {
            case "check":
                PlayerData d = GlobalPlayer.get(target);
                a.send("&f" + target.getName() + " &5has &d" + (d != null ? d.getGems() : 0) + " &5gems.");
                break;
            case "give":
                long give = parseLong(a.get(2));
                if (give <= 0) { a.sendError("Amount must be positive."); return; }
                plugin.getGemManager().addGems(target.getUniqueId(), give, "Admin grant");
                a.sendSuccess("Gave &d" + give + " &agems to &f" + target.getName());
                break;
            case "take":
                long take = parseLong(a.get(2));
                plugin.getGemManager().removeGems(target.getUniqueId(), take, "Admin remove");
                a.sendSuccess("Removed &d" + take + " &agems from &f" + target.getName());
                break;
            case "set":
                long set = parseLong(a.get(2));
                plugin.getGemManager().setGems(target.getUniqueId(), set);
                a.sendSuccess("Set &f" + target.getName() + "&a's gems to &d" + set);
                break;
            default:
                a.usage("/gems <give|take|set|check> <player> <amount>");
        }
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return -1; }
    }
}
