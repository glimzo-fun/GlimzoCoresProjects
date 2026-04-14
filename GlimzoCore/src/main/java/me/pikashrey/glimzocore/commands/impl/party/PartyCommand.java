package me.pikashrey.glimzocore.commands.impl.party;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.party.Party;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.menus.PartyMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /party (aliases: /p)
 *
 * Subcommands:
 *   invite <player>   - Invite someone to your party
 *   accept            - Accept a pending invite
 *   decline           - Decline a pending invite (alias: deny)
 *   leave             - Leave the party (or disband if leader)
 *   disband           - Force disband (leader only)
 *   chat [msg]        - Toggle party chat, or send a party message
 *   list              - List party members
 *   (no args)         - Open party GUI
 *
 * When sending an invite, the target receives a clickable [Accept] / [Decline]
 * notification via Adventure Component instead of a plain-text hint.
 */
public class PartyCommand implements GlimzoCommand {

    private final GlimzoCore plugin;

    public PartyCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        if (!a.has(0)) {
            new PartyMenu(plugin, player).open();
            return;
        }

        switch (a.get(0).toLowerCase()) {

            case "invite": {
                if (!a.has(1)) { a.usage("/party invite <player>"); return; }
                Player target = Bukkit.getPlayer(a.get(1));
                if (target == null || target.equals(player)) {
                    a.sendError("Player not online."); return;
                }
                if (plugin.getPartyManager().isInParty(target.getUniqueId())) {
                    a.sendError(target.getName() + " is already in a party."); return;
                }
                if (!plugin.getSettingsManager().canReceivePartyInvites(target.getUniqueId())) {
                    a.sendError(target.getName() + " has party invites disabled."); return;
                }
                plugin.getPartyManager().invite(player.getUniqueId(), target.getUniqueId());
                a.sendSuccess("Party invite sent to &f" + target.getName() + "&a.");

                // Clickable invite notification (replaces old plain-text hint)
                PartyMenu.sendPartyInviteNotification(target, player.getName());
                break;
            }

            case "accept": {
                if (!plugin.getPartyManager().hasPendingInvite(player.getUniqueId())) {
                    a.sendError("You have no pending party invite."); return;
                }
                boolean joined = plugin.getPartyManager().joinParty(player.getUniqueId(), player.getName());
                if (joined) a.sendSuccess("You joined the party!");
                else a.sendError("Could not join the party (may be full or disbanded).");
                break;
            }

            case "decline":
            case "deny": {
                if (!plugin.getPartyManager().hasPendingInvite(player.getUniqueId())) {
                    a.sendError("You have no pending party invite."); return;
                }
                plugin.getPartyManager().declineInvite(player.getUniqueId());
                a.sendSuccess("You declined the party invite.");
                break;
            }

            case "leave": {
                Party party = plugin.getPartyManager().getParty(player.getUniqueId());
                if (party == null) { a.sendError("You are not in a party."); return; }
                if (party.isLeader(player.getUniqueId())) {
                    plugin.getPartyManager().disbandParty(player.getUniqueId());
                } else {
                    plugin.getPartyManager().leaveParty(player.getUniqueId());
                    a.sendSuccess("You left the party.");
                }
                break;
            }

            case "disband": {
                if (!plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    a.sendError("Only the party leader can disband."); return;
                }
                plugin.getPartyManager().disbandParty(player.getUniqueId());
                break;
            }

            case "chat":
            case "c": {
                if (!plugin.getPartyManager().isInParty(player.getUniqueId())) {
                    a.sendError("You are not in a party."); return;
                }
                if (a.has(1)) {
                    plugin.getPartyManager().sendPartyChat(player.getUniqueId(), a.join(1));
                } else {
                    plugin.getPartyManager().togglePartyChat(player.getUniqueId());
                    boolean on = plugin.getPartyManager().hasPartyChatEnabled(player.getUniqueId());
                    a.sendSuccess("Party chat " + (on ? "enabled" : "disabled") + ".");
                }
                break;
            }

            case "list": {
                Party party = plugin.getPartyManager().getParty(player.getUniqueId());
                if (party == null) { a.sendError("You are not in a party."); return; }
                a.send("&dParty &7(" + party.getSize() + "/" + Party.DEFAULT_MAX_SIZE + "):");
                for (UUID member : party.getMembers()) {
                    Player mp    = Bukkit.getPlayer(member);
                    String role  = party.isLeader(member) ? "&6[Leader] " : "";
                    String mname = mp != null ? mp.getName() : member.toString().substring(0, 8);
                    a.send("  &7» " + role + "&f" + mname);
                }
                break;
            }

            default: showHelp(a);
        }
    }

    private void showHelp(CommandArgs a) {
        a.send("&d&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        a.send("  &d&lParty Commands");
        a.send("&d&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        a.send("  &d/party invite &f<player> &8» &7Invite a player");
        a.send("  &d/party accept           &8» &7Accept an invite");
        a.send("  &d/party decline          &8» &7Decline an invite");
        a.send("  &d/party leave            &8» &7Leave the party");
        a.send("  &d/party disband          &8» &7Disband your party");
        a.send("  &d/party chat [msg]       &8» &7Toggle/send party chat");
        a.send("  &d/party list             &8» &7View party members");
        a.send("&d&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1)
            return Arrays.asList("invite", "accept", "decline", "deny", "leave", "disband", "chat", "list");
        return Collections.emptyList();
    }
}
