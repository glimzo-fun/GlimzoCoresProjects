package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState;
import me.pikashrey.glimzocore.features.cosmetics.ally.Ally;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyManager;
import me.pikashrey.glimzocore.features.cosmetics.chattag.ChatTagManager;
import me.pikashrey.glimzocore.features.cosmetics.joineffect.JoinEffectManager;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    protected final GlimzoCore plugin;

    public PlayerJoinListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Load core player data (players/settings/stats - 1 connection, 3 queries)
            GlobalPlayer.load(player);
            PlayerData data = GlobalPlayer.get(player.getUniqueId());

            if (data == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        String msg = plugin.getConfigManager().getMessage("db-load-error", "&cFailed to load your data. Please reconnect.");
                        player.kickPlayer(CC.translate(msg));
                    }
                });
                return;
            }

            // Batched join loader: grants + punishments + friends + achievements
            // + ignore list + cosmetics - all on ONE connection instead of 6 separate checkouts.
            me.pikashrey.glimzocore.database.mysql.MySQLManager.JoinData jd =
                    plugin.getMysqlManager().loadJoinData(player.getUniqueId());

            plugin.getRankManager().loadGrantsFromData(player.getUniqueId(), jd.grants);

            if (data.isInClan()) {
                plugin.getClanManager().loadForPlayer(player.getUniqueId(), data.getClanId());
            }

            plugin.getPunishmentManager().loadPunishmentsFromData(player.getUniqueId(), jd.punishments);
            plugin.getFriendManager().loadFriendsFromData(player.getUniqueId(), jd.friendUuids);
            plugin.getCosmeticManager().loadPlayerFromData(player.getUniqueId(), jd.cosmeticState);
            plugin.getAchievementManager().loadAchievementsFromData(player.getUniqueId(), jd.achievementIds);
            plugin.getChatManager().loadIgnoreListFromData(player.getUniqueId(), jd.ignoredUuids);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                if (plugin.getNameTagHandler() != null) plugin.getNameTagHandler().onJoin(player);

                // Restore fly state from last session
                me.pikashrey.glimzocore.api.player.PlayerData flyData = me.pikashrey.glimzocore.api.player.GlobalPlayer.get(player.getUniqueId());
                if (flyData != null && flyData.isFlyEnabled() && player.hasPermission("glimzo.fly")) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
                plugin.getScoreboardManager().onJoin(player);
                plugin.getTablistManager().updateTablist(player);
                plugin.getStaffManager().applyVanishOnJoin(player);
                plugin.getPermissionManager().applyPermissions(player, plugin.getRankManager().resolvePermissions(player.getUniqueId()));
                // Sync all three displays on join
                if (plugin.getSyncManager() != null) plugin.getSyncManager().onJoin(player);
                plugin.getJoinMessageManager().broadcastJoin(player);
                // Give hotbar items (clear inv + set slots)
                // Don't overwrite inventory if player re-joins while in staff mode
                boolean inStaff = plugin.getStaffManager().isInStaffMode(player.getUniqueId());
                if (!inStaff && plugin.getHotbarManager() != null) {
                    plugin.getHotbarManager().giveHotbar(player);
                }

                if (plugin.getDiscordManager().isEnabled()) {
                    plugin.getDiscordManager().sendJoinLog(player.getName(), plugin.getRankManager().getActiveRankRef(player).getDisplayName());
                }
            });

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                PlayerCosmeticState state = plugin.getCosmeticManager().getState(player);
                if (state == null || !state.hasJoinEffect()) return;
                JoinEffectManager jem = plugin.getCosmeticManager().getJoinEffectManager();
                if (jem != null) jem.triggerJoinEffect(player, state,
                        plugin.getRankManager().getChatPrefix(player.getUniqueId()), resolveTag(state));
            }, 20L);

            // Cosmetics visibility fix: send spawn packets for all active allies belonging
            // to players already on the server. Without this, a newly joined player never
            // receives the initial spawn packet for allies that are already ticking -
            // the tick loop only sends teleport packets, not spawn packets.
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                AllyManager allyManager = plugin.getCosmeticManager() != null
                        ? plugin.getCosmeticManager().getAllyManager() : null;
                if (allyManager == null) return;
                for (org.bukkit.entity.Player other : player.getWorld().getPlayers()) {
                    if (other.equals(player)) continue;
                    Ally otherAlly = allyManager.getAlly(other);
                    if (otherAlly != null) {
                        otherAlly.spawnForViewer(player);
                    }
                }
            }, 10L); // 10-tick delay ensures the player's client is fully ready
        });
    }

    private String resolveTag(PlayerCosmeticState state) {
        if (plugin.getCosmeticManager() == null) return null;
        ChatTagManager ctm = plugin.getCosmeticManager().getChatTagManager();
        return ctm != null ? ctm.getFormattedTag(state) : null;
    }
}
