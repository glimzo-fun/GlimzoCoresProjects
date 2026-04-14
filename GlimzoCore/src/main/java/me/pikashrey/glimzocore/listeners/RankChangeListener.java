package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.api.events.impl.PlayerRankChangeEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RankChangeListener implements Listener {

    protected final GlimzoCore plugin;

    public RankChangeListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRankChange(PlayerRankChangeEvent event) {
        PlayerData data = event.getPlayerData();
        Player online = Bukkit.getPlayer(data.getUuid());

        // Invalidate stale chat prefix cache so next message uses the new rank
        invalidateChatCache(data.getUuid());

        // Refresh nametag on all boards
        if (online != null && plugin.getNameTagHandler() != null) {
            plugin.getNameTagHandler().refresh(online);
        }

        // Notify the player if they are online
        if (online != null && event.getNewRankRef() != null) {
            RankRef newRef = event.getNewRankRef();
            if (event.isUpgrade()) {
                online.sendMessage(CC.translate(
                        "&aYour rank has been upgraded to "
                        + newRef.getColorCode() + newRef.getDisplayName() + "&a!"));
            } else {
                online.sendMessage(CC.translate(
                        "&7Your rank has been updated to "
                        + newRef.getColorCode() + newRef.getDisplayName() + "&7."));
            }
        }
    }

    private void invalidateChatCache(java.util.UUID uuid) {
        if (plugin.getChatListenerInstance() != null) {
            plugin.getChatListenerInstance().invalidatePrefix(uuid);
        }
    }
}
