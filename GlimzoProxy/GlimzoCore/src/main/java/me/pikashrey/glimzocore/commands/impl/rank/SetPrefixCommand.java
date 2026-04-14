package me.pikashrey.glimzocore.commands.impl.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SetPrefixCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public SetPrefixCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.rank.setprefix"; }
    @Override public boolean isPlayerOnly() { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/setprefix [player] <prefix | clear>"); return; }

        Player target;
        String rawPrefix;

        if (a.has(1)) {
            target = Bukkit.getPlayer(a.get(0));
            if (target == null) { a.sendError("Player not online."); return; }
            rawPrefix = a.join(1);
        } else {
            if (!a.isPlayer()) { a.sendError("Specify a player when running from console."); return; }
            target = a.getPlayer();
            rawPrefix = a.get(0);
        }

        PlayerData data = GlobalPlayer.get(target);
        if (data == null) { a.sendError("Player data not loaded."); return; }

        if (rawPrefix.equalsIgnoreCase("clear") || rawPrefix.equalsIgnoreCase("reset")) {
            data.setCustomPrefix(null);
            a.sendSuccess("Cleared custom prefix for &f" + target.getName() + "&a.");
            target.sendMessage(CC.translate("&7Your custom prefix has been cleared."));
        } else {
            if (rawPrefix.length() > 16) { a.sendError("Prefix too long (max 16 chars including color codes)."); return; }
            data.setCustomPrefix(rawPrefix);
            a.sendSuccess("Set prefix for &f" + target.getName() + " &ato: " + CC.translate(rawPrefix));
            target.sendMessage(CC.translate("&7Your prefix has been set to: " + CC.translate(rawPrefix)));
        }
    }
}
