package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;

/**
 * Lobby world protection: no block breaking or placing by anyone
 * regardless of gamemode, unless they have build mode enabled via /glimzo build.
 */
public class BlockListener implements Listener {

    protected final GlimzoCore plugin;

    public BlockListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    /** Breaking blocks - cancelled for everyone except build-mode admins. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (isBuildMode(event.getPlayer()) || plugin.getHelpers().isLobbyWorld(event.getPlayer().getWorld())) return;
        event.setCancelled(true);
        // Drop nothing even in survival - prevents players getting items from grass etc.
        event.setExpToDrop(0);
    }

    /** Placing blocks - cancelled for everyone except build-mode admins. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (isBuildMode(event.getPlayer()) || plugin.getHelpers().isLobbyWorld(event.getPlayer().getWorld())) return;
        event.setCancelled(true);
    }

    /** No fire spreading in lobby. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    /** No block burning. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    /** No natural block growth (crops, vines, etc.). */
    @EventHandler(priority = EventPriority.HIGH)
    public void onGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    /** No block spread (mushrooms, fire). */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    /** No block form (snow/ice forming naturally). */
    @EventHandler(priority = EventPriority.HIGH)
    public void onForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    /** No ice/snow melting. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFade(BlockFadeEvent event) {
        event.setCancelled(true);
    }

    /** No leaf decay. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    private boolean isBuildMode(Player player) {
        return plugin.getBuildModeManager().isBuilder(player.getUniqueId());
    }
}
