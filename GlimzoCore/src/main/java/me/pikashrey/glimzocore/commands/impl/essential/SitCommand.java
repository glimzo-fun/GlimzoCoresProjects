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
 * /sit - makes the player "sit" by mounting an invisible ArmorStand.
 * Requires MrPanda ally Level 2.
 */
public class SitCommand implements GlimzoCommand {

    private final GlimzoCore plugin;

    // UUID -> the ArmorStand the player is sitting on
    private static final Map<UUID, ArmorStand> sittingStands = new HashMap<>();

    public SitCommand(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPermission() {
        return "glimzo.sit";
    }

    @Override
    public void execute(CommandArgs args) {
        Player player = args.getPlayer();
        if (player == null) return;

        // Perk gate - must have MrPanda Level 2+
        AllyPerkManager perkManager = plugin.getCosmeticManager().getAllyPerkManager();
        if (perkManager == null || perkManager.getAllyLevel(player, AllyType.MR_PANDA) < 2) {
            player.sendMessage(CC.translate("&cYou need &dMr. Panda &cally at &6Level 2&c to use /sit!"));
            return;
        }

        UUID uuid = player.getUniqueId();

        // Toggle: already sitting -> stand up
        if (sittingStands.containsKey(uuid)) {
            standUp(player);
            return;
        }

        // Spawn a real invisible ArmorStand slightly below the player
        Location loc = player.getLocation().clone().subtract(0, 1.1, 0);

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setCanPickupItems(false);
        stand.setCustomNameVisible(false);

        // Bukkit API: setPassenger on the ArmorStand (not on NMS entity)
        stand.setPassenger(player);

        sittingStands.put(uuid, stand);
        player.sendMessage(CC.translate("&dYou sat down. &7Type &f/sit &7again to stand up."));
    }

    /** Stand up and remove the ArmorStand. Call on toggle, quit, or death. */
    public static void standUp(Player player) {
        UUID uuid = player.getUniqueId();
        ArmorStand stand = sittingStands.remove(uuid);
        if (stand == null) return;
        stand.eject();
        stand.remove();
        player.sendMessage(CC.translate("&7You stood up."));
    }

    public static boolean isSitting(UUID uuid) {
        return sittingStands.containsKey(uuid);
    }

    /** Clean up all stands on shutdown. */
    public static void cleanupAll() {
        for (ArmorStand stand : sittingStands.values()) {
            if (stand != null && !stand.isDead()) { stand.eject(); stand.remove(); }
        }
        sittingStands.clear();
    }
}
