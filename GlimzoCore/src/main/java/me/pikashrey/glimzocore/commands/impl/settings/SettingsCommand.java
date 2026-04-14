package me.pikashrey.glimzocore.commands.impl.settings;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.entity.Player;
import java.util.*;

public class SettingsCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public SettingsCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        if (!a.has(0)) {
            new me.pikashrey.glimzocore.menus.SettingsMenu(plugin, player).open();
            return;
        }
        boolean val;
        switch (a.get(0).toLowerCase()) {
            case "pm":             val = plugin.getSettingsManager().togglePrivateMessages(player.getUniqueId()); break;
            case "friend-requests":val = plugin.getSettingsManager().toggleFriendRequests(player.getUniqueId()); break;
            case "party-invites":  val = plugin.getSettingsManager().togglePartyInvites(player.getUniqueId()); break;
            case "clan-invites":   val = plugin.getSettingsManager().toggleClanInvites(player.getUniqueId()); break;
            case "scoreboard":     val = plugin.getSettingsManager().toggleScoreboard(player.getUniqueId()); break;
            case "visibility":     val = plugin.getSettingsManager().togglePlayerVisibility(player.getUniqueId()); break;
            case "join-messages":  val = plugin.getSettingsManager().toggleJoinMessages(player.getUniqueId()); break;
            case "cosmetics":      val = plugin.getSettingsManager().toggleOwnCosmetics(player.getUniqueId()); break;
            case "other-cosmetics":val = plugin.getSettingsManager().toggleOtherCosmetics(player.getUniqueId()); break;
            default: a.sendError("Unknown setting. Use /settings for a list."); return;
        }
        a.sendSuccess(a.get(0) + " " + (val ? "&aenabled" : "&cdisabled") + "&a.");
    }
    @Override public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1) return Arrays.asList("pm","friend-requests","party-invites","clan-invites","scoreboard","visibility","join-messages","cosmetics","other-cosmetics");
        return Collections.emptyList();
    }
}
