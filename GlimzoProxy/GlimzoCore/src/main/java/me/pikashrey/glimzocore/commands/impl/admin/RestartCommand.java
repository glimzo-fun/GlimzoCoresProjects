package me.pikashrey.glimzocore.commands.impl.admin;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;

public class RestartCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public RestartCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.admin.restart"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        String[] args = a.getArgs();

        if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
            if (!plugin.getRestartManager().isRestartScheduled()) {
                a.send("&7No restart is scheduled.");
                return;
            }
            plugin.getRestartManager().cancelRestart();
            a.send("&aRestart cancelled.");
            return;
        }

        int seconds = 60;
        if (args.length > 0) {
            try {
                seconds = Integer.parseInt(args[0]);
                if (seconds < 5) { a.send("&cMinimum delay is 5 seconds."); return; }
            } catch (NumberFormatException e) {
                a.send("&cUsage: /restart [seconds|cancel]");
                return;
            }
        }

        plugin.getRestartManager().scheduleRestart(seconds);
        a.send("&aRestart scheduled in &f" + seconds + " &aseconds.");
    }
}
