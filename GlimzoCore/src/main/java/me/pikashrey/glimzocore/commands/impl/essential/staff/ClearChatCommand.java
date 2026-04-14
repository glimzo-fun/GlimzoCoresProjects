package me.pikashrey.glimzocore.commands.impl.essential.staff;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.MessageUtil;
public class ClearChatCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public ClearChatCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return "glimzo.staff.broadcast"; }
    @Override public boolean isPlayerOnly() { return false; }
    @Override public void execute(CommandArgs a) {
        for (int i = 0; i < 100; i++) MessageUtil.broadcast(" ");
        MessageUtil.broadcast("&8[&cChat&8] &7Chat was cleared by " + a.getSender().getName() + ".");
    }
}
