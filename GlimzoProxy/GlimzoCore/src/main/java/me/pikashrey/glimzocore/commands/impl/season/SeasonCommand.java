package me.pikashrey.glimzocore.commands.impl.season;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.season.Season;
import me.pikashrey.glimzocore.api.season.SeasonRank;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.menus.SeasonMenu;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SeasonCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public SeasonCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return null; }
    @Override public boolean isPlayerOnly() { return false; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) {
            // Player: open season menu; console: show info
            if (a.isPlayer()) {
                new SeasonMenu(plugin, a.getPlayer()).open();
            } else {
                showSeasonInfo(a);
            }
            return;
        }

        String sub = a.get(0).toLowerCase();
        switch (sub) {
            case "info":
                showSeasonInfo(a);
                break;
            case "top":
                showLeaderboard(a);
                break;
            case "addxp":
                handleAddXp(a);
                break;
            default:
                if (a.isPlayer()) new SeasonMenu(plugin, a.getPlayer()).open();
                else showSeasonInfo(a);
        }
    }

    private void showSeasonInfo(CommandArgs a) {
        Season season = plugin.getSeasonManager().getCurrentSeason();
        if (season == null) { a.sendError("No active season."); return; }

        a.send("&8&m--------------------------------");
        a.send("  &d&l" + season.getDisplayName());
        a.send("  &7Status: " + (season.isActive() ? "&aActive" : "&cInactive"));
        a.send("  &7Ends: &f" + TimeFormatUtils.formatExpiry(season.getEndTime()));
        a.send("  &7Total ranks: &f" + season.getRanks().size());

        if (a.isPlayer()) {
            PlayerData data = GlobalPlayer.get(a.getPlayer());
            if (data != null) {
                SeasonRank rank = plugin.getSeasonManager().getPlayerRank(data);
                a.send("  &7Your rank: " + (rank != null ? rank.getColoredName() : "&8Unranked"));
                a.send("  &7Your XP: &f" + data.getSeasonXp());
            }
        }
        a.send("&8&m--------------------------------");
    }

    private void showLeaderboard(CommandArgs a) {
        a.send("&d&lSeason Leaderboard:");
        plugin.getSeasonLeaderboard().getEntries().forEach(e ->
            a.send("  &7#" + e.position + " &f" + e.name
                    + " &7- Rank &d" + e.seasonRank + " &7(" + e.seasonXp + " XP)")
        );
    }

    private void handleAddXp(CommandArgs a) {
        if (!a.getSender().hasPermission("glimzo.staff.mode")) { a.noPermission(); return; }
        if (!a.has(2)) { a.usage("/season addxp <player> <amount>"); return; }
        Player target = Bukkit.getPlayer(a.get(1));
        if (target == null) { a.sendError("Player not online."); return; }
        long amount;
        try { amount = Long.parseLong(a.get(2)); } catch (NumberFormatException e) {
            a.sendError("Amount must be a number."); return;
        }
        plugin.getSeasonManager().addSeasonXp(target.getUniqueId(), amount);
        a.sendSuccess("Added &d" + amount + " &aSeason XP to &f" + target.getName() + "&a.");
    }

    @Override
    public List<String> tabComplete(CommandArgs a) {
        if (a.length() == 1) return Arrays.asList("info", "top", "addxp");
        return java.util.Collections.emptyList();
    }
}
