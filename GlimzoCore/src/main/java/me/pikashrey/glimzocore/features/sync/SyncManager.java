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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SyncManager implements Listener {

    protected final GlimzoCore plugin;

    // Snapshot cache - skip re-rendering if nothing actually changed
    private final Map<UUID, String> lastSnapshot = new ConcurrentHashMap<>();

    public SyncManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRankChange(PlayerRankChangeEvent e)     { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevelUp(PlayerLevelUpEvent e)           { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrestige(PlayerPrestigeEvent e)         { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCoins(CoinsTransactionEvent e)          { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGems(GemsTransactionEvent e)            { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClanJoin(ClanJoinEvent e)               { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClanLeave(ClanLeaveEvent e)             { sync(e.getPlayerData().getUuid()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSeasonRankUp(PlayerSeasonRankUpEvent e) { sync(e.getPlayerData().getUuid()); }

    public void onJoin(Player player) {
        lastSnapshot.remove(player.getUniqueId());
        syncNow(player);
    }

    public void onQuit(UUID uuid) {
        lastSnapshot.remove(uuid);
    }

    public void sync(UUID uuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) syncNow(player);
        });
    }

    public void syncNow(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null) return;

        RankRef rank = plugin.getRankManager().getActiveRankRef(player);
        me.pikashrey.glimzocore.api.clan.ClanData clan = plugin.getClanManager().getClanByPlayer(uuid);
        PlayerCosmeticState state = plugin.getCosmeticManager() != null
                ? plugin.getCosmeticManager().getState(player) : null;

        String snapshot = buildSnapshot(data, rank, clan, state);
        if (snapshot.equals(lastSnapshot.get(uuid))) return;
        lastSnapshot.put(uuid, snapshot);

        updateScoreboard(player);
        updateTablist(player);
        updateDisplayName(player, rank);
        data.invalidateChatPrefix();
    }

    public void syncAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            lastSnapshot.remove(p.getUniqueId());
            syncNow(p);
        }
    }

    private void updateDisplayName(Player player, RankRef rank) {
        me.pikashrey.glimzocore.features.nick.NickManager nm = plugin.getNickManager();
        if (nm != null && nm.hasNick(player.getUniqueId())) return;

        String translatedPrefix = CC.translate(rank.getChatPrefix());
        StringBuilder leadingCodes = new StringBuilder();
        int i = 0;
        while (i + 1 < translatedPrefix.length() && translatedPrefix.charAt(i) == '\u00a7') {
            leadingCodes.append(translatedPrefix, i, i + 2);
            i += 2;
        }
        String rankColor = leadingCodes.length() > 0 ? leadingCodes.toString() : "\u00a77";

        player.setDisplayName(rankColor + player.getName());
        player.setPlayerListName(null);
    }

    private void updateScoreboard(Player player) {
        if (!plugin.getSettingsManager().hasScoreboardEnabled(player.getUniqueId())) return;
        plugin.getScoreboardManager().update(player);
    }

    private void updateTablist(Player player) {
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
