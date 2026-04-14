package me.pikashrey.glimzocore.commands.impl.clan;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.clan.ClanMember;
import me.pikashrey.glimzocore.api.clan.ClanRole;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.RankRef;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;

public class ClanCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public ClanCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return;

        if (!a.has(0)) {
            ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan == null) { showHelp(a); return; }
            new me.pikashrey.glimzocore.menus.ClanMenu(plugin, player).open();
            return;
        }
        String sub = a.get(0).toLowerCase();
        switch (sub) {
            case "create":   handleCreate(a, player, data); break;
            case "invite":   handleInvite(a, player, data); break;
            case "accept":   handleAccept(a, player, data); break;
            case "leave":    handleLeave(a, player, data);  break;
            case "disband":  handleDisband(a, player, data);break;
            case "kick":     handleKick(a, player, data);   break;
            case "promote":  handleRole(a, player, data, ClanRole.OFFICER); break;
            case "demote":   handleRole(a, player, data, ClanRole.MEMBER);  break;
            case "chat": case "cc":
                handleChat(a, player, data); break;
            case "menu":
                new me.pikashrey.glimzocore.menus.ClanMenu(plugin, player).open(); break;
            case "top": case "leaderboard":
                new me.pikashrey.glimzocore.menus.ClanLeaderboardMenu(plugin, player).open(); break;
            case "perks":
                new me.pikashrey.glimzocore.menus.ClanPerksMenu(plugin, player).open(); break;
            default: showHelp(a);
        }
    }

    private void handleCreate(CommandArgs a, Player player, PlayerData data) {
        if (data.isInClan()) { a.sendError("You are already in a clan."); return; }
        if (!a.has(2)) { a.usage("/clan create <name> <tag>"); return; }
        String name = a.get(1), tag = a.get(2);
        if (tag.length() > 8) { a.sendError("Tag must be 8 characters or less."); return; }
        RankRef rank = plugin.getRankManager().getActiveRankRef(player);
        ClanData clan = plugin.getClanManager().createClan(data, rank, name, tag);
        if (clan == null) { a.sendError("Could not create clan. You need Legendary or above rank, or the name/tag is already taken."); return; }
        a.sendSuccess("Clan &f" + name + " &a[" + tag + "] created!");
    }

    private void handleInvite(CommandArgs a, Player player, PlayerData data) {
        if (!data.isInClan()) { a.sendError("You are not in a clan."); return; }
        ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        ClanMember member = clan.getMember(player.getUniqueId());
        if (member == null || !member.isOfficer()) { a.sendError("Only officers and the leader can invite."); return; }
        if (!a.has(1)) { a.usage("/clan invite <player>"); return; }
        Player target = Bukkit.getPlayer(a.get(1));
        if (target == null) { a.sendError("Player not online."); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) { a.sendError("You can't invite yourself."); return; }
        if (!plugin.getSettingsManager().canReceiveClanInvites(target.getUniqueId())) {
            a.sendError(target.getName() + " has clan invites disabled."); return;
        }
        plugin.getClanManager().invitePlayer(target.getUniqueId(), clan.getId());
        a.sendSuccess("Invited &f" + target.getName() + " &ato the clan.");
        target.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate(
                "&a" + player.getName() + " &7invited you to &f[" + clan.getTag() + "] "
                + clan.getName() + "&7! Type &a/clan accept&7."));
    }

    private void handleAccept(CommandArgs a, Player player, PlayerData data) {
        if (data.isInClan()) { a.sendError("You are already in a clan."); return; }
        if (!plugin.getClanManager().hasPendingInvite(player.getUniqueId())) {
            a.sendError("You have no pending clan invite."); return;
        }
        String clanId = plugin.getClanManager().getPendingInviteClanId(player.getUniqueId());
        ClanData clan = plugin.getClanManager().getClan(clanId);
        if (clan == null) { a.sendError("That clan no longer exists."); return; }
        RankRef leaderRank = plugin.getRankManager().getActiveRankRef(clan.getLeaderUuid());
        boolean joined = plugin.getClanManager().joinClan(data, clan, leaderRank);
        if (!joined) { a.sendError("Could not join the clan (it may be full)."); return; }
        data.getStats().incrementClansJoined();
        plugin.getClanManager().clearInvite(player.getUniqueId());
        a.sendSuccess("You joined clan &f[" + clan.getTag() + "] " + clan.getName() + "&a!");
    }

    private void handleLeave(CommandArgs a, Player player, PlayerData data) {
        if (!data.isInClan()) { a.sendError("You are not in a clan."); return; }
        ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        if (clan.isLeader(player.getUniqueId())) {
            a.sendError("You are the leader. Transfer leadership with /clan promote or /clan disband."); return;
        }
        plugin.getClanManager().leaveClan(data, clan, me.pikashrey.glimzocore.api.events.impl.ClanLeaveEvent.Reason.LEFT);
        a.sendSuccess("You left clan &f" + clan.getName() + "&a.");
    }

    private void handleDisband(CommandArgs a, Player player, PlayerData data) {
        if (!data.isInClan()) { a.sendError("You are not in a clan."); return; }
        ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        boolean isAdmin = player.hasPermission("glimzo.clan.admin");
        if (!clan.isLeader(player.getUniqueId()) && !isAdmin) {
            a.sendError("Only the clan leader can disband."); return;
        }
        if (!a.has(1) || !a.get(1).equalsIgnoreCase("confirm")) {
            a.send("&cThis will permanently delete your clan. Type &f/clan disband confirm &cto proceed.");
            return;
        }
        plugin.getClanManager().disbandClan(clan, player.getUniqueId(), player.getName(), isAdmin);
        a.sendSuccess("Clan disbanded.");
    }

    private void handleKick(CommandArgs a, Player player, PlayerData data) {
        if (!a.has(1)) { a.usage("/clan kick <player>"); return; }
        ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) { a.sendError("You are not in a clan."); return; }
        ClanMember selfMember = clan.getMember(player.getUniqueId());
        if (selfMember == null || !selfMember.isOfficer()) { a.sendError("Only officers+ can kick."); return; }
        Player target = Bukkit.getPlayer(a.get(1));
        if (target == null) { a.sendError("Player not online."); return; }
        ClanMember targetMember = clan.getMember(target.getUniqueId());
        if (targetMember == null) { a.sendError("That player is not in your clan."); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) { a.sendError("You can't kick yourself."); return; }
        if (targetMember.getRole().getOrdinalValue() >= selfMember.getRole().getOrdinalValue()) {
            a.sendError("You can't kick someone of equal or higher rank."); return;
        }
        PlayerData targetData = GlobalPlayer.get(target);
        if (targetData != null) {
            plugin.getClanManager().leaveClan(targetData, clan, me.pikashrey.glimzocore.api.events.impl.ClanLeaveEvent.Reason.KICKED);
        }
        a.sendSuccess("Kicked &f" + target.getName() + " &afrom the clan.");
    }

    private void handleRole(CommandArgs a, Player player, PlayerData data, ClanRole newRole) {
        if (!a.has(1)) { a.usage("/clan " + (newRole == ClanRole.OFFICER ? "promote" : "demote") + " <player>"); return; }
        ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.isLeader(player.getUniqueId())) { a.sendError("Only the leader can change roles."); return; }
        Player target = Bukkit.getPlayer(a.get(1));
        if (target == null || !clan.isMember(target.getUniqueId())) { a.sendError("Player not in your clan."); return; }
        plugin.getClanManager().setMemberRole(clan, target.getUniqueId(), newRole);
        a.sendSuccess("Set &f" + target.getName() + "&a's role to &f" + newRole.getDisplayName() + "&a.");
    }

    private void handleChat(CommandArgs a, Player player, PlayerData data) {
        if (!data.isInClan()) { a.sendError("You are not in a clan."); return; }
        if (a.has(1)) {
            ClanData clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan != null) {
                String rankPrefix = plugin.getRankManager().getChatPrefix(player.getUniqueId());
                plugin.getClanManager().sendClanChat(clan, rankPrefix, player.getName(), a.join(1));
            }
        } else {
            plugin.getClanManager().toggleClanChat(player.getUniqueId());
            boolean on = plugin.getClanManager().hasClanChatEnabled(player.getUniqueId());
            a.sendSuccess("Clan chat " + (on ? "enabled" : "disabled") + ".");
        }
    }

    private void showClanInfo(CommandArgs a, ClanData clan, Player player) {
        a.send("&8&m---------------------------");
        a.send("  &f[" + clan.getTag() + "] &6" + clan.getName() + " &7- Lv." + clan.getLevel());
        a.send("  &7Members: &f" + clan.getMemberCount());
        a.send("  &7Leader: &f" + clan.getLeaderName());
        a.send("  &7XP: &f" + clan.getXp() + (clan.isMaxLevel() ? " &a(MAX)" : "/" + me.pikashrey.glimzocore.api.clan.ClanLevel.forLevel(clan.getLevel()+1).getXpRequired()));
        a.send("&8&m---------------------------");
    }

    private void showHelp(CommandArgs a) {
        a.send("&6/clan create <name> <tag> &7- Create a clan (Legendary+)");
        a.send("&6/clan invite <player> &7- Invite a player");
        a.send("&6/clan accept &7- Accept an invite");
        a.send("&6/clan leave &7- Leave your clan");
        a.send("&6/clan disband confirm &7- Disband the clan");
        a.send("&6/clan kick <player> &7- Kick a member");
        a.send("&6/clan promote/demote <player> &7- Change role");
        a.send("&6/clan chat [msg] &7- Toggle/send clan chat");
    }

    @Override public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1)
            return Arrays.asList("create","invite","accept","leave","disband","kick","promote","demote","chat");
        return Collections.emptyList();
    }
}
