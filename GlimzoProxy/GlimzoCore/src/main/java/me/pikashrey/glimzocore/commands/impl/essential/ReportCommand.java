package me.pikashrey.glimzocore.commands.impl.essential;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;

public class ReportCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public ReportCommand(GlimzoCore p) { this.plugin = p; }
    
    @Override public String getPermission() { return null; }
    
    @Override public void execute(CommandArgs a) {
        Player player = a.getPlayer(); 
        if (player == null) return;
        // Reports are handled by a separate plugin - this command is disabled
        player.sendMessage(CC.translate("&cReport system is managed by a separate plugin."));
    }
}

