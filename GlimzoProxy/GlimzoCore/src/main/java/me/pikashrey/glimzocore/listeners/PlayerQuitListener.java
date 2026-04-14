package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    protected final GlimzoCore plugin;

    public PlayerQuitListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Suppress Bukkit's default quit message and broadcast our custom one
        event.setQuitMessage(null);
        plugin.getJoinMessageManager().broadcastQuit(player);

        // Clean up sit/lie armor stands so they don't linger in the world
        me.pikashrey.glimzocore.commands.impl.essential.SitCommand.standUp(player);
        me.pikashrey.glimzocore.commands.impl.essential.LieCommand.getUp(player);

        // Persist fly state before data is unloaded so it's included in the save
        me.pikashrey.glimzocore.api.player.PlayerData _qd = me.pikashrey.glimzocore.api.player.GlobalPlayer.get(player.getUniqueId());
        if (_qd != null) _qd.setFlyEnabled(player.getAllowFlight());

        // --- Main thread: in-memory and packet teardown ---
        // NOTE: cosmeticManager.unloadPlayer is called AFTER savePlayer in the async block
        // to prevent ally levels/meals being wiped before they're persisted.
        plugin.getClanManager().unloadForPlayer(player.getUniqueId());
        plugin.getRankManager().unloadGrants(player.getUniqueId());
        plugin.getPunishmentManager().unloadPunishments(player.getUniqueId());
        plugin.getFriendManager().unloadFriends(player.getUniqueId());
        plugin.getPartyManager().onPlayerQuit(player.getUniqueId());
        plugin.getScoreboardManager().onQuit(player.getUniqueId());
        if (plugin.getSyncManager() != null) plugin.getSyncManager().onQuit(player.getUniqueId());
        plugin.getChatManager().unloadIgnoreList(player.getUniqueId());
        plugin.getAchievementManager().unloadAchievements(player.getUniqueId());
        // Clean up creative inventory slot tracking
        if (plugin.getInventoryClickListener() != null) {
            plugin.getInventoryClickListener().onQuit(player.getUniqueId());
        }
        if (plugin.getChatListenerInstance() != null) {
            plugin.getChatListenerInstance().onQuit(player.getUniqueId());
        }
        if (plugin.getLevelingListener() != null) {
            plugin.getLevelingListener().onQuit(player.getUniqueId());
        }
        plugin.getStaffManager().onQuit(player.getUniqueId());

        if (plugin.getNameTagHandler() != null) {
            plugin.getNameTagHandler().onQuit(player.getUniqueId());
        }

        // Remove permission attachments and invalidate caches
        plugin.getPermissionManager().remove(player);

        // --- Async: persist to MySQL ---
        final java.util.UUID quitUuid = player.getUniqueId();
        // Capture player reference before async - Player object is safe to reference
        // after disconnect for UUID/name lookups (Bukkit retains the object).
        final org.bukkit.entity.Player quitPlayer = player;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getCosmeticManager().savePlayer(quitUuid);   // save first
            plugin.getCosmeticManager().unloadPlayer(quitUuid); // then free memory
            GlobalPlayer.unload(quitPlayer);
        });
    }
}