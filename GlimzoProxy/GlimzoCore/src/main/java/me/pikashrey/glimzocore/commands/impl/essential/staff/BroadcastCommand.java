package me.pikashrey.glimzocore.commands.impl.essential.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.MessageUtil;

public class BroadcastCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public BroadcastCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.staff.broadcast"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/broadcast <message>"); return; }
        MessageUtil.broadcast("&2&l[&a&lBroadcast&2&l] &r&7" + a.join(0));
    }
}
