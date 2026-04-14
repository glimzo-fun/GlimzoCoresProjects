package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyItemManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyPerkManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyType;
import me.pikashrey.glimzocore.menus.CosmeticBrowserMenu;
import me.pikashrey.glimzocore.menus.CosmeticBrowserMenu.Category;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AllyItemListener implements Listener {

    private final GlimzoCore      plugin;
    private final AllyItemManager allyItemManager;

    private final Map<UUID, Long> abilityCooldown  = new HashMap<>();
    private static final long     ABILITY_COOLDOWN_MS = 3000L;

    public AllyItemListener(GlimzoCore plugin, AllyItemManager allyItemManager) {
        this.plugin          = plugin;
        this.allyItemManager = allyItemManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player    player = event.getPlayer();
        ItemStack item   = event.getItem();

        if (!allyItemManager.isAllySkull(item)) return;
        event.setCancelled(true);

        AllyType ally = allyItemManager.getAllyFromSkull(item);
        if (ally == null) return;

        boolean isLeft  = event.getAction() == Action.LEFT_CLICK_AIR
                       || event.getAction() == Action.LEFT_CLICK_BLOCK;
        boolean isRight = event.getAction() == Action.RIGHT_CLICK_AIR
                       || event.getAction() == Action.RIGHT_CLICK_BLOCK;

        if (isRight) {
            new CosmeticBrowserMenu(plugin, player, Category.ALLIES, ally).open();
            return;
        }

        if (isLeft) {
            UUID uuid = player.getUniqueId();
            long now  = System.currentTimeMillis();
            Long last = abilityCooldown.get(uuid);
            if (last != null && now - last < ABILITY_COOLDOWN_MS) {
                long remaining = (ABILITY_COOLDOWN_MS - (now - last)) / 1000 + 1;
                player.sendMessage("§cAbility on cooldown! §7(" + remaining + "s)");
                return;
            }

            AllyPerkManager perkManager = plugin.getCosmeticManager().getAllyPerkManager();
            int level = perkManager.getAllyLevel(player, ally);

            boolean used = triggerAbility(player, ally, level);
            if (used) abilityCooldown.put(uuid, now);
        }
    }

    private boolean triggerAbility(Player player, AllyType ally, int level) {
        switch (ally) {
            case PUG:       return PugAbility.boneThrow(player, level, plugin);
            case FALCON:    return FalconAbility.dash(player, level);
            case CHARIZARD: return CharizardAbility.fireBlast(player, level);
            case KITTY:     return KittyAbility.pounce(player, level);
            case MR_PANDA:  return PandaAbility.teleport(player, level);
            case DR_DUCKY:  return DuckyAbility.doubleJump(player, level);
            default:        return false;
        }
    }


    private static class PugAbility {
        static boolean boneThrow(Player p, int level, GlimzoCore plugin) {
            if (level < 2) { p.sendMessage("§cUnlock Pug Level 2 for Bone Throw!"); return false; }
            // Use a Snowball projectile - visible, flies properly, not affected by ItemSpawnEvent
            org.bukkit.util.Vector dir = p.getLocation().getDirection().normalize().multiply(1.8);
            org.bukkit.entity.Snowball bone = p.getWorld().spawn(
                p.getEyeLocation(), org.bukkit.entity.Snowball.class);
            bone.setVelocity(dir);
            bone.setShooter(p);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.SHOOT_ARROW, 0.8f, 1.2f);
            p.sendMessage("§f🦴 Bone thrown!");
            return true;
        }
    }

    private static class FalconAbility {
        static boolean dash(Player p, int level) {
            if (level < 3) { p.sendMessage("§cUnlock Falcon Level 3 for Dash!"); return false; }
            org.bukkit.util.Vector dir = p.getLocation().getDirection().normalize().multiply(2.5);
            dir.setY(0.3);
            p.setVelocity(dir);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENDERMAN_TELEPORT, 0.6f, 1.8f);
            p.sendMessage("§e🦅 Dash!");
            return true;
        }
    }

    private static class CharizardAbility {
        static boolean fireBlast(Player p, int level) {
            if (level < 1) { p.sendMessage("§cEquip Charizard to use this ability!"); return false; }
            org.bukkit.util.Vector dir = p.getLocation().getDirection().normalize();
            org.bukkit.entity.Fireball fb = p.getWorld().spawn(
                p.getEyeLocation().add(dir), org.bukkit.entity.Fireball.class);
            fb.setDirection(dir.multiply(2));
            fb.setShooter(p);
            fb.setYield(1.5f);
            fb.setIsIncendiary(true);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.GHAST_FIREBALL, 1.0f, 1.0f);
            p.sendMessage("§6🔥 Fire Blast!");
            return true;
        }
    }

    private static class KittyAbility {
        static boolean pounce(Player p, int level) {
            if (level < 2) { p.sendMessage("§cUnlock Kitty Level 2 for Pounce!"); return false; }
            org.bukkit.util.Vector dir = p.getLocation().getDirection().normalize();
            dir.multiply(1.8).setY(0.6);
            p.setVelocity(dir);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.CAT_MEOW, 1.0f, 1.2f);
            p.sendMessage("§d🐾 Pounce!");
            return true;
        }
    }

    private static class PandaAbility {
        static boolean teleport(Player p, int level) {
            if (level < 3) { p.sendMessage("§cUnlock Mr. Panda Level 3 for Teleport!"); return false; }
            org.bukkit.Location eye = p.getEyeLocation();
            org.bukkit.util.Vector dir = eye.getDirection().normalize();
            org.bukkit.Location dest = null;
            // Step 0.5 blocks at a time up to 30 blocks - fine-grained so we land on the exact block face
            for (double dist = 0.5; dist <= 30.0; dist += 0.5) {
                org.bukkit.Location check = eye.clone().add(dir.clone().multiply(dist));
                if (check.getBlock().getType() != org.bukkit.Material.AIR) {
                    // Land centered on top of the hit block
                    dest = check.getBlock().getLocation().clone().add(0.5, 1.0, 0.5);
                    dest.setYaw(p.getLocation().getYaw());
                    dest.setPitch(p.getLocation().getPitch());
                    break;
                }
            }
            if (dest == null) { p.sendMessage("§cNo block in range!"); return false; }
            p.teleport(dest);
            p.getWorld().playSound(dest, org.bukkit.Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
            p.sendMessage("§0🐼 Teleported!");
            return true;
        }
    }

    private static class DuckyAbility {
        static boolean doubleJump(Player p, int level) {
            if (level < 3) { p.sendMessage("§cUnlock Dr. Ducky Level 3 for Double Jump!"); return false; }
            org.bukkit.util.Vector vel = p.getVelocity();
            vel.setY(0.8);
            p.setVelocity(vel);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.CHICKEN_EGG_POP, 1.0f, 1.2f);
            p.sendMessage("§9🦆 Double Jump!");
            return true;
        }
    }
}
