package me.pikashrey.glimzocore.commands.impl.essential;
import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.Symbols;
public class EmojisCommand implements GlimzoCommand {
    private final GlimzoCore plugin;
    public EmojisCommand(GlimzoCore p) { this.plugin = p; }
    @Override public String getPermission() { return null; }
    @Override public void execute(CommandArgs a) {
        a.send("&6Available symbols:");
        a.send("  &f" + Symbols.STAR + " &7:star:   &f" + Symbols.HEART + " &7:heart:   &f"
                + Symbols.CROWN + " &7:crown:   &f" + Symbols.LIGHTNING + " &7:lightning:");
        a.send("  &f" + Symbols.DIAMOND + " &7:diamond:   &f" + Symbols.SWORD + " &7:sword:   &f"
                + Symbols.CHECKMARK + " &7:check:   &f" + Symbols.CROSS + " &7:cross:");
    }
}
