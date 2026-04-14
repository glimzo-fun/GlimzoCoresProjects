package me.pikashrey.glimzocore.commands.impl.essential.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeCommand implements GlimzoCommand {

    private static final Set<UUID> FROZEN =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final GlimzoCore plugin;

    public FreezeCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String getPermission() { return "glimzo.staff.freeze"; }

    @Override
    public void execute(CommandArgs a) {
        if (!a.has(0)) { a.usage("/freeze <player>"); return; }
        Player target = Bukkit.getPlayer(a.get(0));
        if (target == null) { a.sendError("Player not online."); return; }

        if (FROZEN.contains(target.getUniqueId())) {
            FROZEN.remove(target.getUniqueId());
            target.sendMessage(CC.translate("&aYou have been unfrozen."));
            a.sendSuccess(target.getName() + " unfrozen.");
        } else {
            FROZEN.add(target.getUniqueId());
            target.sendMessage(CC.translate("&cYou have been frozen by a staff member."));
            a.sendSuccess(target.getName() + " frozen.");
        }
    }

    /** Static query used by PlayerMoveListener and InteractListener. */
    public static boolean isFrozenStatic(UUID uuid) {
        return FROZEN.contains(uuid);
    }

    /** Static freeze - used by staff mode compass left-click. */
    public static void freeze(UUID uuid) {
        FROZEN.add(uuid);
    }

    /** Static unfreeze - used by staff mode compass left-click. */
    public static void unfreeze(UUID uuid) {
        FROZEN.remove(uuid);
    }

    public boolean isFrozen(UUID uuid) { return FROZEN.contains(uuid); }
}

