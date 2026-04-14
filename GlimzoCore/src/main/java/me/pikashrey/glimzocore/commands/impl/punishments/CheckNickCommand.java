package me.pikashrey.glimzocore.commands.impl.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import java.util.ArrayList;
import java.util.List;

public class CheckNickCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public CheckNickCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.punish.check"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) {
            // List all nicked players
            List<String> nicked = new ArrayList<>();
            for (PlayerData data : GlobalPlayer.getAll()) {
                if (data.hasNick()) nicked.add(data.getName() + " &7-> &f" + data.getNick());
            }
            if (nicked.isEmpty()) { a.send("&7No players are currently nicked."); return; }
            a.send("&6Nicked players (" + nicked.size() + "):");
            nicked.forEach(s -> a.send("  &7» &f" + s));
        } else {
            // Check specific nick or name
            String query = a.get(0);
            for (PlayerData data : GlobalPlayer.getAll()) {
                if (data.hasNick() && data.getNick().equalsIgnoreCase(query)) {
                    a.send("&f" + query + " &7is a nick for &f" + data.getName());
                    return;
                }
            }
            a.send("&7No player is using the nick &f" + query + "&7.");
        }
    }
}
