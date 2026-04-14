package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

/** Lobby: prevent right-click block interactions for non-staff. */
public class PlayerInteractListener implements Listener {

    protected final GlimzoCore plugin;

    public PlayerInteractListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().isOp()) return;
        if (plugin.getStaffManager().isInStaffMode(event.getPlayer().getUniqueId())) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        // Allow use of chests, doors, buttons for all players
        Material type = event.getClickedBlock().getType();
        if (isInteractable(type)) return;

        event.setCancelled(true);
    }

    private boolean isInteractable(Material m) {
        String n = m.name();
        return n.contains("DOOR")   || n.contains("GATE")    || n.contains("BUTTON")
            || n.contains("LEVER")  || n.contains("PLATE")   || n.contains("CHEST")
            || n.contains("FENCE")  || n.contains("TRAPDOOR")
            || m == Material.WORKBENCH || m == Material.ENCHANTMENT_TABLE
            || m == Material.ANVIL;
    }
}
