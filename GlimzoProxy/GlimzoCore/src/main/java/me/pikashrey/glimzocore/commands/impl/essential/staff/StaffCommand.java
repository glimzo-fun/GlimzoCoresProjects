package me.pikashrey.glimzocore.commands.impl.essential.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;

public class StaffCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public StaffCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.staff.mode"; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); 
        if (player == null) return;
        
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) {
            player.sendMessage(CC.translate("&cPlayer data not loaded."));
            return;
        }
        
        boolean enabled = plugin.getStaffManager().toggleStaffMode(player);
        
        if (enabled) {
            player.sendMessage(CC.translate("&a&l🌿 Staff mode &2enabled&a."));
        } else {
            player.sendMessage(CC.translate("&7&l🍂 Staff mode &cdisabled&7."));
        }
    }
}
