package me.pikashrey.glimzocore.commands.impl.essential;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
public class ForumsCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public ForumsCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        String link = plugin.getConfig().getString("forums-link", "forums.glimzo.net");
        a.send("&7Visit our forums: &b" + link);
    }
}
