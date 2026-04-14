package me.pikashrey.glimzocore.features.sync;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.events.impl.*;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState;
import me.pikashrey.glimzocore.features.cosmetics.chattag.ChatTagManager;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SyncManager - single source of truth for real-time display sync.
 *
 * Any change to a player's rank, level, coins, gems, prestige, or clan
 * fires one of the GlimzoCore custom events. SyncManager listens to all
 * of them and fans out to scoreboard + tablist + chat prefix in one pass,
 * on the same tick the event fires.
 *
 * Chat format is rebuilt here so the scoreboard prefix, tablist footer,
 * and chat all show identical rank/tag information at all times.
 */
public class SyncManager implements Listener {

    protected final GlimzoCore plugin;

    // Snapshot cache: UUID → last rendered state string.
    // Scoreboard and tablist skip re-rendering if nothing changed.
    private final Map<UUID, String> lastSnapshot = new ConcurrentHashMap<>();

    public SyncManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRankChange(PlayerRankChangeEvent e)       { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevelUp(PlayerLevelUpEvent e)             { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrestige(PlayerPrestigeEvent e)           { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCoins(CoinsTransactionEvent e)            { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGems(GemsTransactionEvent e)              { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClanJoin(ClanJoinEvent e)                 { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClanLeave(ClanLeaveEvent e)               { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSeasonRankUp(PlayerSeasonRankUpEvent e)   { sync(e.getPlayerData().getUuid()); }

    /** Full sync on join (called after data is loaded - from PlayerJoinListener). */
    public void onJoin(Player player) {
        lastSnapshot.remove(player.getUniqueId());
        syncNow(player);
    }

    public void onQuit(UUID uuid) {
        lastSnapshot.remove(uuid);
    }


    /**
     * Schedule a sync for next tick.  Safe to call from any thread -
     * all Bukkit API (scoreboard, tablist) runs on the main thread.
     */
    public void sync(UUID uuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) syncNow(player);
        });
    }

    /**
     * Immediately sync scoreboard + tablist for this player.
     * Must be called on the main thread.
     */
    public void syncNow(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null) return;

        RankRef rank = plugin.getRankManager().getActiveRankRef(player);
        me.pikashrey.glimzocore.api.clan.ClanData clan =
                plugin.getClanManager().getClanByPlayer(uuid);
        PlayerCosmeticState state = plugin.getCosmeticManager() != null
                ? plugin.getCosmeticManager().getState(player) : null;

        // Build snapshot string - skip full re-render if nothing changed
        String snapshot = buildSnapshot(data, rank, clan, state);
        if (snapshot.equals(lastSnapshot.get(uuid))) return;
        lastSnapshot.put(uuid, snapshot);

        // Fan out to all three displays
        updateScoreboard(player, data, rank, clan);
        updateTablist(player, data, rank, clan);
        updateDisplayName(player, data, rank);
        // Chat prefix is rebuilt per-message in ChatListener (it reads rank live),
        // but we invalidate the cached prefix here so the next message is correct.
        data.invalidateChatPrefix();
    }

    /** Force-sync ALL online players (used on /glimzo reload ranks). */
    public void syncAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            lastSnapshot.remove(p.getUniqueId());
            syncNow(p);
        }
    }


    private void updateDisplayName(Player player, PlayerData data, RankRef rank) {
        // Nick system overrides display name - don't touch it if nicked
        me.pikashrey.glimzocore.features.nick.NickManager nm = plugin.getNickManager();
        if (nm != null && nm.hasNick(player.getUniqueId())) return;

        // Extract leading colour/format codes from the translated prefix
        // e.g. "§4§l[CHIEF] " → leadingCodes = "§4§l"
        String translatedPrefix = CC.translate(rank.getChatPrefix());
        StringBuilder leadingCodes = new StringBuilder();
        int i = 0;
        while (i + 1 < translatedPrefix.length() && translatedPrefix.charAt(i) == '§') {
            leadingCodes.append(translatedPrefix, i, i + 2);
            i += 2;
        }
        String rankColor = leadingCodes.length() > 0 ? leadingCodes.toString() : "§7";

        // setDisplayName: controls the name shown above head and in chat (correct to colour it)
        player.setDisplayName(rankColor + player.getName());

        // DO NOT call setPlayerListName - in 1.8.8 it overrides the scoreboard team prefix
        // in the tab list entirely, hiding the rank tag. Tab colouring is handled by
        // NameTagBoard scoreboard teams (team prefix = "§4§l[CHIEF] " etc.)
        // Resetting it to null restores team-controlled tab display.
        player.setPlayerListName(null);
    }


    private void updateScoreboard(Player player, PlayerData data, RankRef rank,
                                   me.pikashrey.glimzocore.api.clan.ClanData clan) {
        if (!plugin.getSettingsManager().hasScoreboardEnabled(player.getUniqueId())) return;
        plugin.getScoreboardManager().update(player);
    }


    private void updateTablist(Player player, PlayerData data, RankRef rank,
                                me.pikashrey.glimzocore.api.clan.ClanData clan) {
        plugin.getTablistManager().updateTablist(player);
    }


    private String buildSnapshot(PlayerData data, RankRef rank,
                                  me.pikashrey.glimzocore.api.clan.ClanData clan,
                                  PlayerCosmeticState state) {
        return rank.getId()
                + "|" + data.getLevel()
                + "|" + data.getPrestige()
                + "|" + data.getCoins()
                + "|" + data.getGems()
                + "|" + data.getSeasonRank()
                + "|" + (clan != null ? clan.getTag() : "")
                + "|" + (state != null ? state.getActiveChatTagId() : "");
    }
}