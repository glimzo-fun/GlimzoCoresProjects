package me.pikashrey.glimzocore.commands.impl.essential;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import org.bukkit.entity.Player;
public class PlayerVisibilityCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public PlayerVisibilityCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        boolean val = plugin.getSettingsManager().togglePlayerVisibility(player.getUniqueId());
        a.sendSuccess("Player visibility " + (val ? "enabled" : "disabled") + ".");
    }
}
