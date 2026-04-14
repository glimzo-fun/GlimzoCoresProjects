package me.pikashrey.glimzocore.commands.impl.cosmetics;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyLevelManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import org.bukkit.entity.Player;

public class AllyCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    private final AllyPerkManager allyPerkManager;
    private final AllyLevelManager allyLevelManager;

    public AllyCommand(GlimzoCore plugin, AllyPerkManager allyPerkManager, AllyLevelManager allyLevelManager) {
        this.plugin = plugin;
        this.allyPerkManager = allyPerkManager;
        this.allyLevelManager = allyLevelManager;
    }

    @Override
    public String getPermission() {
        return "glimzo.ally";
    }

    @Override
    public void execute(CommandArgs args) {
        Player player = args.getPlayer();
        if (player == null) return;

        String[] a = args.getArgs();

        if (a.length == 0) {
            showAllyInfo(args, player);
            return;
        }

        String subcommand = a[0].toLowerCase();

        switch (subcommand) {
            case "level":
                showLevel(args, player, a);
                break;
            case "list":
                listAllies(args, player);
                break;
            default:
                args.sendError("Unknown subcommand: " + subcommand);
        }
    }

    private void showAllyInfo(CommandArgs args, Player player) {
        args.send("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        args.send("&f&lAlly System &8» &7Manage your allies");
        args.send("");
        args.send("&f/ally list&8 &7- View all allies");
        args.send("&f/ally level <ally>&8 &7- Check ally level");
        args.send("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void listAllies(CommandArgs args, Player player) {
        args.send("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        args.send("&f&lYour Allies");
        args.send("");

        for (AllyType type : AllyType.values()) {
            int level = allyPerkManager.getAllyLevel(player, type);
            String status = level == 0 ? "&7(Locked)" : "&a(Level " + level + "/3)";
            args.send("&f" + type.getDisplayName() + " &8» " + status);
        }

        args.send("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void showLevel(CommandArgs args, Player player, String[] a) {
        if (a.length < 2) {
            args.sendError("Usage: /ally level <ally_name>");
            return;
        }

        // Join all remaining args as the ally name so "Mr. Panda" works
        String allyInput = joinArgs(a, 1, a.length);
        AllyType allyType = AllyType.fromInput(allyInput);
        if (allyType == null) {
            args.sendError("Unknown ally: " + allyInput + ". Use: mr_panda, falcon, kitty, pug, charizard, dr_ducky");
            return;
        }

        int level = allyPerkManager.getAllyLevel(player, allyType);
        args.send("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        args.send("&f" + allyType.getDisplayName() + " &8» Level " + level + "/3");

        if (level > 0) {
            args.send("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            for (int i = 1; i <= 3; i++) {
                String prefix = i == level ? "&a✓" : i < level ? "&a✓" : "&7✗";
                args.send(prefix + " &fLevel " + i);
            }
        }

        args.send("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /** Joins args[from] through args[to-1] with spaces. */
    private String joinArgs(String[] a, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from) sb.append(" ");
            sb.append(a[i]);
        }
        return sb.toString().trim();
    }

}

