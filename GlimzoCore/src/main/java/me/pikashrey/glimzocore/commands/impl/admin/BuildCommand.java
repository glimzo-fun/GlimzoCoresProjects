package me.pikashrey.glimzocore.commands.impl.admin;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;

public class BuildCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public BuildCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.admin.build"; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        boolean enabled = plugin.getBuildModeManager().toggle(player.getUniqueId());

        if (enabled) {
            player.sendMessage(CC.translate(
                    "&2&l🌿 Build Mode &aENABLED &7- you can now place and break blocks.\n" +
                    "&7Run &a/glimzo build &7again to disable."));
        } else {
            player.sendMessage(CC.translate(
                    "&7&l🍂 Build Mode &cDISABLED &7- lobby protection restored."));
        }
    }
}
