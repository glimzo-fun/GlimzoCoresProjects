package me.pikashrey.glimzocore.commands.impl.essential.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StaffChatCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public StaffChatCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.staff.chat"; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        if (!a.has(0)) { a.usage("/sc <message>"); return; }
        String msg = CC.translate("&2[&a&lStaffChat&2] &7" + player.getName() + "&8: &f" + a.join(0));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("glimzo.staff.chat")) p.sendMessage(msg);
        }
    }
}
