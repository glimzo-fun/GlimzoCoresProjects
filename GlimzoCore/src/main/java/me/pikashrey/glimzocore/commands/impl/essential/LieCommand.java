package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * /lie - makes the player "lie down" by mounting a very low invisible ArmorStand.
 * Requires MrPanda ally Level 2.
 */
public class LieCommand implements GlimzoCommand {

    private final GlimzoCore plugin;

    private static final Map<UUID, ArmorStand> lyingStands = new HashMap<>();

    public LieCommand(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPermission() {
        return "glimzo.lie";
    }

    @Override
    public void execute(CommandArgs args) {
        Player player = args.getPlayer();
        if (player == null) return;

        AllyPerkManager perkManager = plugin.getCosmeticManager().getAllyPerkManager();
        if (perkManager == null || perkManager.getAllyLevel(player, AllyType.MR_PANDA) < 2) {
            player.sendMessage(CC.translate("&cYou need &dMr. Panda &cally at &6Level 2&c to use /lie!"));
            return;
        }

        UUID uuid = player.getUniqueId();

        // Toggle: already lying -> get up
        if (lyingStands.containsKey(uuid)) {
            getUp(player);
            return;
        }

        // Place the stand very low - makes the player appear to lie flat
        Location loc = player.getLocation().clone().subtract(0, 1.8, 0);

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setCanPickupItems(false);
        stand.setCustomNameVisible(false);

        // Bukkit API: setPassenger on the ArmorStand
        stand.setPassenger(player);

        lyingStands.put(uuid, stand);
        player.sendMessage(CC.translate("&dYou lay down. &7Type &f/lie &7again to get up."));
    }

    /** Get up and remove the ArmorStand. Call on toggle, quit, or death. */
    public static void getUp(Player player) {
        UUID uuid = player.getUniqueId();
        ArmorStand stand = lyingStands.remove(uuid);
        if (stand == null) return;
        stand.eject();
        stand.remove();
        player.sendMessage(CC.translate("&7You got up."));
    }

    public static boolean isLying(UUID uuid) {
        return lyingStands.containsKey(uuid);
    }

    /** Clean up all stands on shutdown. */
    public static void cleanupAll() {
        for (ArmorStand stand : lyingStands.values()) {
            if (stand != null && !stand.isDead()) { stand.eject(); stand.remove(); }
        }
        lyingStands.clear();
    }
}
