package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Block dangerous vanilla commands for non-op players. */
public class CommandBlockerListener implements Listener {

    protected final GlimzoCore plugin;

    private static final Set<String> BLOCKED = new HashSet<>(Arrays.asList(
            "op", "deop", "stop", "restart", "reload", "give", "gamemode", "gm",
            "tp", "teleport", "summon", "execute", "kill", "effect", "enchant",
            "experience", "xp", "defaultgamemode", "difficulty", "gamerule",
            "time", "weather", "worldborder", "help", "?"
    ));

    public CommandBlockerListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().isOp()) return;
        if (event.getPlayer().hasPermission("glimzo.staff.mode")) return;

        String cmd = event.getMessage().toLowerCase().split("\\s")[0].replaceAll("^/", "");
        if (BLOCKED.contains(cmd)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                CC.translate("&cThat command is not available."));
        }
    }
}
