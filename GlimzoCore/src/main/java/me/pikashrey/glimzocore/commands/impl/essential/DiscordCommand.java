package me.pikashrey.glimzocore.commands.impl.essential;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
public class DiscordCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public DiscordCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        String link = plugin.getConfig().getString("discord-link", "discord.gg/glimzo");
        a.send("&7Join our Discord: &bdiscord.gg/" + link.replaceAll(".*discord\\.gg/", ""));
    }
}
