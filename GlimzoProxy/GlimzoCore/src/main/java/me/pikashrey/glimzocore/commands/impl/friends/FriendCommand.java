package me.pikashrey.glimzocore.commands.impl.friends;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.FriendsMenu;
import me.pikashrey.glimzocore.utilities.chat.CC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /friend (aliases: /friends, /fl, /f)
 *
 * Subcommands: add, accept, deny/decline, remove/unfriend, list
 *
 * When a friend request is sent, the recipient gets a clickable
 * Adventure Component with [Accept] / [Deny] buttons that run the
 * command automatically - no more manual typing required.
 */
public class FriendCommand implements GlimzoCommand {

    private final GlimzoCore plugin;

    public FriendCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        if (!a.has(0)) {
            // No args → open friends GUI
            new FriendsMenu(plugin, player).open();
            return;
        }

        switch (a.get(0).toLowerCase()) {

            case "add": {
                if (!a.has(1)) { a.usage("/friend add <player>"); return; }
                Player target = Bukkit.getPlayer(a.get(1));
                if (target == null || target.equals(player)) {
                    a.sendError("That player is not online."); return;
                }
                if (plugin.getFriendManager().areFriends(player.getUniqueId(), target.getUniqueId())) {
                    a.sendError("You are already friends with &f" + target.getName() + "&c."); return;
                }
                if (!plugin.getSettingsManager().canReceiveFriendRequests(target.getUniqueId())) {
                    a.sendError(target.getName() + " has friend requests disabled."); return;
                }
                int max = plugin.getConfigManager().getSocial().getInt("friends.max-friends", 100);
                if (plugin.getFriendManager().getFriendCount(player.getUniqueId()) >= max) {
                    a.sendError("You have reached the max friends limit (&f" + max + "&c)."); return;
                }
                if (plugin.getFriendManager().getFriendCount(target.getUniqueId()) >= max) {
                    a.sendError(target.getName() + " has reached their friend limit."); return;
                }
                if (plugin.getFriendManager().hasPendingRequest(target.getUniqueId())) {
                    a.sendError("You already sent a pending request to &f" + target.getName() + "&c."); return;
                }

                // Send the request and notify the recipient with a CLICKABLE message
                plugin.getFriendManager().sendRequest(player.getUniqueId(), target.getUniqueId());
                a.sendSuccess("Friend request sent to &f" + target.getName() + "&a.");

                // Clickable notification to recipient (replaces old plain-text hint)
                FriendsMenu.sendFriendRequestNotification(target, player.getName());
                break;
            }

            case "accept": {
                if (!a.has(1)) { a.usage("/friend accept <player>"); return; }
                UUID requesterUuid = plugin.getFriendManager()
                        .findRequesterByName(player.getUniqueId(), a.get(1));
                if (requesterUuid == null) {
                    a.sendError("No pending friend request from &f" + a.get(1) + "&c."); return;
                }
                plugin.getFriendManager().addFriend(player.getUniqueId(), requesterUuid);
                a.sendSuccess("You are now friends with &f" + a.get(1) + "&a.");
                break;
            }

            case "deny":
            case "decline": {
                if (!a.has(1)) { a.usage("/friend deny <player>"); return; }
                UUID requesterUuid = plugin.getFriendManager()
                        .findRequesterByName(player.getUniqueId(), a.get(1));
                if (requesterUuid == null) {
                    a.sendError("No pending friend request from &f" + a.get(1) + "&c."); return;
                }
                plugin.getFriendManager().denyRequest(player.getUniqueId(), requesterUuid);
                a.sendSuccess("Denied friend request from &f" + a.get(1) + "&a.");
                break;
            }

            case "remove":
            case "unfriend": {
                if (!a.has(1)) { a.usage("/friend remove <player>"); return; }
                UUID targetUuid = resolveUuid(a.get(1));
                if (targetUuid == null) {
                    a.sendError("Player &f" + a.get(1) + "&c not found."); return;
                }
                if (!plugin.getFriendManager().areFriends(player.getUniqueId(), targetUuid)) {
                    a.sendError("You are not friends with &f" + a.get(1) + "&c."); return;
                }
                plugin.getFriendManager().removeFriend(player.getUniqueId(), targetUuid);
                a.sendSuccess("Removed &f" + a.get(1) + " &afrom your friends.");
                break;
            }

            case "list":
                handleList(player);
                break;

            default:
                showHelp(player);
        }
    }

    private void handleList(Player player) {
        Set<UUID> friendUuids = plugin.getFriendManager().getFriends(player.getUniqueId());
        if (friendUuids == null || friendUuids.isEmpty()) {
            player.sendMessage(CC.translate("&7You have no friends yet. Use &a/friend add <player> &7to add some."));
            return;
        }
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate(" &a&lFriends &8(&7" + friendUuids.size() + "&8)"));
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        for (UUID uuid : friendUuids) {
            Player online = Bukkit.getPlayer(uuid);
            String status = online != null && online.isOnline() ? "&a● " : "&8● ";
            PlayerData pd = GlobalPlayer.get(uuid);
            String name = pd != null ? pd.getDisplayName()
                    : (online != null ? online.getName()
                    : Optional.ofNullable(Bukkit.getOfflinePlayer(uuid).getName()).orElse("Unknown"));
            player.sendMessage(CC.translate("  " + status + "&f" + name));
        }
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void showHelp(Player player) {
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("  &a&lFriend Commands"));
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("  &a/friend add &f<player>    &8» &7Send a friend request"));
        player.sendMessage(CC.translate("  &a/friend accept &f<player> &8» &7Accept a request"));
        player.sendMessage(CC.translate("  &a/friend deny &f<player>   &8» &7Deny a request"));
        player.sendMessage(CC.translate("  &a/friend remove &f<player> &8» &7Remove a friend"));
        player.sendMessage(CC.translate("  &a/friend list              &8» &7View your friends"));
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    @SuppressWarnings("deprecation")
    private UUID resolveUuid(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) return online.getUniqueId();
        OfflinePlayer off = Bukkit.getOfflinePlayer(name);
        return (off != null && off.hasPlayedBefore()) ? off.getUniqueId() : null;
    }

    @Override
    public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1)
            return Arrays.asList("add", "accept", "deny", "decline", "remove", "unfriend", "list");
        return Collections.emptyList();
    }
}
