package me.pikashrey.glimzocore.commands.impl.essential.messages;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.entity.Player;

public class SocialSpyCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public SocialSpyCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.staff.chat"; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer(); if (player == null) return;
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return;
        boolean newState = !data.isSocialSpyEnabled();
        data.setSocialSpyEnabled(newState);
        a.sendSuccess("Social spy " + (newState ? "&aenabled" : "&cdisabled") + "&a.");
    }
}
