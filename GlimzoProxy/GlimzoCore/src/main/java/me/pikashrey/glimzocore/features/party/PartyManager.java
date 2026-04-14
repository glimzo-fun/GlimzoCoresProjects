package me.pikashrey.glimzocore.features.party;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyManager {

    // Invite TTL read from social.yml at call-time

    protected final GlimzoCore plugin;

    // Leader UUID -> Party
    private final Map<UUID, Party>   parties    = new java.util.concurrent.ConcurrentHashMap<>();
    // Member UUID -> leader UUID (for reverse lookup)
    private final Map<UUID, UUID>    memberIndex = new java.util.concurrent.ConcurrentHashMap<>();
    // Invitee UUID -> (inviter UUID, expiry)
    private final Map<UUID, Invite>  invites     = new java.util.concurrent.ConcurrentHashMap<>();
    // Players with party chat toggled on
    // Players with party chat toggled on - accessed from async chat thread, must be thread-safe
    private final java.util.Set<UUID> partyChatOn = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public PartyManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    // Party lifecycle

    /** Create a party with the given player as leader. */
    public Party createParty(UUID leaderUuid, String leaderName) {
        Party party = new Party(leaderUuid, leaderName);
        parties.put(leaderUuid, party);
        memberIndex.put(leaderUuid, leaderUuid);
        return party;
    }

    /** Disband the party - remove all members and clean up indexes. */
    public void disbandParty(UUID leaderUuid) {
        Party party = parties.remove(leaderUuid);
        if (party == null) return;
        for (UUID member : party.getMembers()) {
            memberIndex.remove(member);
            partyChatOn.remove(member);
        }
        // Notify ALL members including the leader (unless they just disconnected - isOnline handles that).
        String msg = me.pikashrey.glimzocore.utilities.chat.CC.translate(
                "&8[&dParty&8] &cThe party has been disbanded.");
        for (UUID member : party.getMembers()) {
            org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(member);
            if (online != null) online.sendMessage(msg);
        }
    }

    public void onPlayerQuit(UUID uuid) {
        partyChatOn.remove(uuid);
        invites.remove(uuid);

        Party party = getParty(uuid);
        if (party == null) return;

        if (party.isLeader(uuid)) {
            disbandParty(uuid);
        } else {
            leaveParty(uuid);
        }
    }

    // Invites

    public void invite(UUID inviterUuid, UUID inviteeUuid) {
        long ttlMs = plugin.getConfigManager().getSocial().getLong("party.invite-ttl-seconds", 60L) * 1000L;
        invites.put(inviteeUuid, new Invite(inviterUuid, System.currentTimeMillis() + ttlMs));
    }

    public boolean hasPendingInvite(UUID uuid) {
        Invite inv = invites.get(uuid);
        if (inv == null) return false;
        if (System.currentTimeMillis() > inv.expiryMs) { invites.remove(uuid); return false; }
        return true;
    }

    public UUID getInviterUuid(UUID uuid) {
        Invite inv = invites.get(uuid);
        return inv != null ? inv.inviterUuid : null;
    }

    /** Decline and remove a pending party invite without joining. */
    public void declineInvite(UUID uuid) {
        invites.remove(uuid);
    }

    // Join / leave

    /** Accept an invite and join the inviter's party. */
    public boolean joinParty(UUID uuid, String name) {
        UUID inviterUuid = getInviterUuid(uuid);
        if (inviterUuid == null) return false;
        invites.remove(uuid);

        // Ensure a party exists for the inviter
        Party party = getPartyByLeader(inviterUuid);
        if (party == null) {
            PlayerData inviterData = GlobalPlayer.get(inviterUuid);
            String inviterName = inviterData != null ? inviterData.getName() : "Unknown";
            party = createParty(inviterUuid, inviterName);
        }

        int maxSize = plugin.getConfigManager().getSocial().getInt("party.max-size", 16);
        if (party.isFull(maxSize)) return false;

        party.addMember(uuid);
        memberIndex.put(uuid, party.getLeaderUuid());

        PlayerData joinerData = GlobalPlayer.get(uuid);
        String displayName = joinerData != null ? joinerData.getDisplayName() : name;
        broadcast(party, "&e" + displayName + " &ajoined the party.");

        PlayerData leaderData = GlobalPlayer.get(party.getLeaderUuid());
        if (leaderData != null) {
            leaderData.getStats().incrementPartiesCreated();
        }

        return true;
    }

    public void leaveParty(UUID uuid) {
        UUID leaderUuid = memberIndex.remove(uuid);
        if (leaderUuid == null) return;
        Party party = parties.get(leaderUuid);
        if (party == null) return;

        party.removeMember(uuid);
        partyChatOn.remove(uuid);

        PlayerData data = GlobalPlayer.get(uuid);
        String displayName = data != null ? data.getDisplayName() : uuid.toString();
        broadcast(party, "&e" + displayName + " &cleft the party.");

        if (party.getSize() <= 1) disbandParty(leaderUuid);
    }

    // Party chat

    public void togglePartyChat(UUID uuid) {
        if (partyChatOn.contains(uuid)) partyChatOn.remove(uuid);
        else partyChatOn.add(uuid);
    }

    public boolean hasPartyChatEnabled(UUID uuid) { return partyChatOn.contains(uuid); }

    public void sendPartyChat(UUID senderUuid, String message) {
        Party party = getParty(senderUuid);
        if (party == null) return;
        PlayerData data = GlobalPlayer.get(senderUuid);
        String name = data != null ? data.getDisplayName() : "?";
        String rankPrefix = plugin.getRankManager().getChatPrefix(senderUuid);
        String formatted = CC.translate("&8[&dParty&8] &8>> &r" + rankPrefix + " " + name + "&7: &f" + message);
        for (UUID member : party.getMembers()) {
            Player online = Bukkit.getPlayer(member);
            if (online != null) online.sendMessage(formatted);
        }
    }

    // Broadcast

    // Cross-server party chat - called by NetworkSocialSync when PARTY_MSG arrives
    public void deliverCrossServerChat(UUID leaderUuid, String message) {
        Party party = getPartyByLeader(leaderUuid);
        if (party == null) return;
        for (UUID member : party.getMembers()) {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(member);
            if (p != null) p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate(message));
        }
    }

        public void broadcast(Party party, String message) {
        String formatted = CC.translate("&8[&dParty&8] &r" + message);
        for (UUID member : party.getMembers()) {
            Player online = Bukkit.getPlayer(member);
            if (online != null) online.sendMessage(formatted);
        }
    }

    // Lookups

    /** Get the party a player belongs to (leader or member). */
    public Party getParty(UUID uuid) {
        UUID leaderUuid = memberIndex.get(uuid);
        return leaderUuid != null ? parties.get(leaderUuid) : null;
    }

    public Party getPartyByLeader(UUID leaderUuid) { return parties.get(leaderUuid); }
    public boolean isInParty(UUID uuid)             { return memberIndex.containsKey(uuid); }
    public boolean isLeader(UUID uuid)              { return parties.containsKey(uuid); }

    private static class Invite {
        final UUID inviterUuid;
        final long expiryMs;
        Invite(UUID inviterUuid, long expiryMs) {
            this.inviterUuid = inviterUuid;
            this.expiryMs    = expiryMs;
        }
    }
}

