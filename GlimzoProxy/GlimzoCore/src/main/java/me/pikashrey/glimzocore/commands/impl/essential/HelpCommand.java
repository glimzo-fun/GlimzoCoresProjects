package me.pikashrey.glimzocore.commands.impl.essential;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
public class HelpCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public HelpCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        a.send("&8&m--------- &6&lGlimzo Help &8&m---------");
        a.send("&6/profile &7- View your profile");
        a.send("&6/level &7- View your level progress");
        a.send("&6/prestige &7- Prestige at level 100");
        a.send("&6/coins &7- View your coins");
        a.send("&6/gems &7- View your gems");
        a.send("&6/friend &7- Friend system");
        a.send("&6/party &7- Party system");
        a.send("&6/clan &7- Clan system");
        a.send("&6/rank &7- View your rank");
        a.send("&6/nick &7- Set a nickname");
        a.send("&6/settings &7- Toggle settings");
        a.send("&6/msg <player> <msg> &7- Private message");
        a.send("&6/discord &7- Discord link");
        a.send("&6/store &7- Store link");
        a.send("&8&m------------------------------");
    }
}
