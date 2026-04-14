package me.pikashrey.glimzocore.commands.impl.admin;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.rank.RankLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ReloadCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public ReloadCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.admin.reload"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) {
            a.send("&eUsage: /glimzo <reload <ranks|config> | build>");
            return;
        }

        switch (a.get(0).toLowerCase()) {

            case "build": {
                if (!a.getSender().hasPermission("glimzo.admin.build")) { a.noPermission(); return; }
                if (!a.isPlayer()) { a.send("&cBuild mode can only be toggled in-game."); return; }
                new BuildCommand(plugin).execute(a);
                return;
            }

            case "ranks": {
                a.send("&7Reloading ranks...");
                RankLoader loader = plugin.getRankLoader();
                try {
                    loader.load();

                    plugin.getPermissionCache().clearAll();

                    // 3 - Re-resolve and re-apply permissions for every online player
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        plugin.getPermissionManager().applyPermissions(
                                p,
                                plugin.getRankManager().resolvePermissions(p.getUniqueId())
                        );
                    }

                    a.send("&a&l✔ &aRanks reloaded. " + loader.getAll().size()
                            + " ranks active. Permissions refreshed for "
                            + Bukkit.getOnlinePlayers().size() + " online player(s).");
                    // Re-sync all display panels so rank changes appear immediately
                    if (plugin.getSyncManager() != null)
                        plugin.getSyncManager().syncAll();
                } catch (Exception e) {
                    a.send("&cRank reload failed: " + e.getMessage());
                    plugin.log("&c[Reload] ranks.yml reload error: " + e.getMessage());
                }
                break;
            }

            case "config": {
                a.send("&7Reloading config...");
                plugin.reloadConfig();
                plugin.getConfigManager().reload();
                a.send("&aconfig.yml and settings reloaded.");
                break;
            }

            default:
                a.send("&eUsage: /glimzo reload <ranks|config>");
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1) return Arrays.asList("ranks", "config");
        return java.util.Collections.emptyList();
    }
}
