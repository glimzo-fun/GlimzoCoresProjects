package me.pikashrey.glimzocore.features.settings;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.player.PlayerSettings;

import java.util.UUID;

public class SettingsManager {

    protected final GlimzoCore plugin;

    public SettingsManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    // Toggle helpers - flip the boolean and return the new value

    public boolean togglePrivateMessages(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        s.setPrivateMessagesEnabled(!s.isPrivateMessagesEnabled());
        return s.isPrivateMessagesEnabled();
    }

    public boolean toggleFriendRequests(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        s.setFriendRequestsEnabled(!s.isFriendRequestsEnabled());
        return s.isFriendRequestsEnabled();
    }

    public boolean togglePartyInvites(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        s.setPartyInvitesEnabled(!s.isPartyInvitesEnabled());
        return s.isPartyInvitesEnabled();
    }

    public boolean toggleClanInvites(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        s.setClanInvitesEnabled(!s.isClanInvitesEnabled());
        return s.isClanInvitesEnabled();
    }

    public boolean toggleScoreboard(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        boolean newVal = !s.isScoreboardEnabled();
        s.setScoreboardEnabled(newVal);
        // Apply immediately: show/hide the scoreboard for the online player
        org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(uuid);
        if (online != null) {
            if (newVal) {
                plugin.getScoreboardManager().onJoin(online);
            } else {
                plugin.getScoreboardManager().onQuit(uuid);
                // Restore vanilla scoreboard
                online.setScoreboard(org.bukkit.Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        // Update hotbar visibility slot to reflect new state
        org.bukkit.entity.Player self2 = org.bukkit.Bukkit.getPlayer(uuid);
        if (self2 != null) {
            me.pikashrey.glimzocore.features.hotbar.HotbarManager hm =
                    me.pikashrey.glimzocore.GlimzoCore.getInstance().getHotbarManager();
            if (hm != null) hm.updateVisibilitySlot(self2);
        }
        return newVal;
    }

    public boolean isPlayerVisibilityEnabled(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isPlayerVisibilityEnabled();
    }

    public boolean togglePlayerVisibility(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        boolean newVal = !s.isPlayerVisibilityEnabled();
        s.setPlayerVisibilityEnabled(newVal);
        // Apply immediately - hide or show all other players for this player
        org.bukkit.entity.Player self = org.bukkit.Bukkit.getPlayer(uuid);
        if (self != null) {
            for (org.bukkit.entity.Player other : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (other.getUniqueId().equals(uuid)) continue;
                if (newVal) {
                    self.showPlayer(other);
                } else {
                    self.hidePlayer(other);
                }
            }
        }
        return newVal;
    }

    public boolean toggleJoinMessages(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        s.setJoinMessagesEnabled(!s.isJoinMessagesEnabled());
        return s.isJoinMessagesEnabled();
    }

    public boolean toggleOwnCosmetics(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        s.setCosmeticsEnabled(!s.isCosmeticsEnabled());
        return s.isCosmeticsEnabled();
    }

    public boolean toggleOtherCosmetics(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        if (s == null) return true;
        s.setOtherCosmeticsEnabled(!s.isOtherCosmeticsEnabled());
        return s.isOtherCosmeticsEnabled();
    }

    // Direct query helpers (used by listeners + managers)

    public boolean canReceivePMs(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isPrivateMessagesEnabled();
    }

    public boolean canReceiveFriendRequests(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isFriendRequestsEnabled();
    }

    public boolean canReceivePartyInvites(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isPartyInvitesEnabled();
    }

    public boolean canReceiveClanInvites(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isClanInvitesEnabled();
    }

    public boolean canSeeJoinMessages(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isJoinMessagesEnabled();
    }

    public boolean hasScoreboardEnabled(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isScoreboardEnabled();
    }

    public boolean hasOtherCosmeticsEnabled(UUID uuid) {
        PlayerSettings s = getSettings(uuid);
        return s == null || s.isOtherCosmeticsEnabled();
    }

    private PlayerSettings getSettings(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null ? data.getSettings() : null;
    }
}

