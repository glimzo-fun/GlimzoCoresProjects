package me.pikashrey.glimzocore.commands.impl.essential.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AlertCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public AlertCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.staff.alert"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/alert <message>"); return; }
        String msg = CC.translate("&c&l[&e&lAlert&c&l] &r&e" + a.join(0));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("glimzo.staff.alert")) p.sendMessage(msg);
        }
    }
}
