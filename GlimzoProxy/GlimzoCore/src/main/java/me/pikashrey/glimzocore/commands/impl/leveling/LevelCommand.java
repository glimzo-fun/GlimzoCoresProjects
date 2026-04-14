package me.pikashrey.glimzocore.commands.impl.leveling;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.LevelMenu;
import me.pikashrey.glimzocore.menus.LevelRewardsMenu;
import me.pikashrey.glimzocore.menus.PrestigeMenu;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class LevelCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public LevelCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        if (!a.has(0)) {
            new LevelMenu(plugin, player).open();
            return;
        }

        switch (a.get(0).toLowerCase()) {
            case "rewards": new LevelRewardsMenu(plugin, player).open(); break;
            case "prestige": new PrestigeMenu(plugin, player).open();   break;
            default: new LevelMenu(plugin, player).open();              break;
        }
    }

    @Override
    public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1) return Arrays.asList("rewards", "prestige");
        return java.util.Collections.emptyList();
    }
}
