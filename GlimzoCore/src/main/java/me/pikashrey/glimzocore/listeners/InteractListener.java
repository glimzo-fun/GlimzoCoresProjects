package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.commands.impl.essential.staff.FreezeCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InteractListener implements Listener {

    protected final GlimzoCore plugin;

    public InteractListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player    player = event.getPlayer();
        ItemStack item   = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (!plugin.getStaffManager().isInStaffMode(player.getUniqueId())) return;

        boolean isRight = event.getAction() == Action.RIGHT_CLICK_AIR
                       || event.getAction() == Action.RIGHT_CLICK_BLOCK;
        boolean isLeft  = event.getAction() == Action.LEFT_CLICK_AIR
                       || event.getAction() == Action.LEFT_CLICK_BLOCK;

        if (!isRight && !isLeft) return;

        switch (item.getType()) {

            case BARRIER:
                event.setCancelled(true);
                plugin.getStaffManager().toggleStaffMode(player);
                break;

            case EYE_OF_ENDER:
                if (isRight) {
                    event.setCancelled(true);
                    plugin.getStaffManager().toggleVanish(player);
                }
                break;

            case COMPASS:
                event.setCancelled(true);
                if (isLeft) {
                    Player nearest = getNearestPlayer(player, 30);
                    if (nearest == null) {
                        player.sendMessage(CC.translate("&cNo player within 30 blocks."));
                    } else {
                        UUID uuid = nearest.getUniqueId();
                        if (FreezeCommand.isFrozenStatic(uuid)) {
                            FreezeCommand.unfreeze(uuid);
                            player.sendMessage(CC.translate("&aUnfroze &f" + nearest.getName() + "&a."));
                            nearest.sendMessage(CC.translate("&aYou have been unfrozen by a staff member."));
                        } else {
                            FreezeCommand.freeze(uuid);
                            player.sendMessage(CC.translate("&eFroze &f" + nearest.getName() + "&e."));
                            nearest.sendMessage(CC.translate("&cYou have been frozen by a staff member."));
                        }
                    }
                } else {
                    Player nearest = getNearestPlayer(player, 30);
                    if (nearest == null) {
                        player.sendMessage(CC.translate("&cNo player within 30 blocks."));
                    } else {
                        sendInspect(player, nearest);
                    }
                }
                break;

            case ENDER_PEARL:
                if (isRight) {
                    event.setCancelled(true);
                    List<Player> candidates = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.equals(player) && !plugin.getStaffManager().isInStaffMode(p.getUniqueId()))
                            candidates.add(p);
                    }
                    if (candidates.isEmpty()) {
                        player.sendMessage(CC.translate("&cNo players to teleport to."));
                    } else {
                        Player target = candidates.get((int)(Math.random() * candidates.size()));
                        player.teleport(target.getLocation());
                        player.sendMessage(CC.translate("&aTeleported to &f" + target.getName() + "&a."));
                    }
                }
                break;

            case BOOK:
                if (isRight) {
                    event.setCancelled(true);
                    sendPlayerList(player);
                }
                break;

            case NETHER_STAR:
                event.setCancelled(true);
                sendStaffList(player);
                break;

            default:
                break;
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffManager().isInStaffMode(player.getUniqueId())) return;

        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof Player)) return;

        ItemStack item = player.getItemInHand();
        if (item == null) return;

        event.setCancelled(true);

        if (item.getType() == Material.COMPASS) {
            sendInspect(player, (Player) clicked);
        } else if (item.getType() == Material.BARRIER) {
            // Right-clicking a player with barrier also exits (accidental click prevention)
            // Actually do nothing here - we handle BARRIER via onInteract above
        }
    }


    private Player getNearestPlayer(Player staff, double maxDist) {
        Player  nearest  = null;
        double  nearestD = maxDist * maxDist;
        Location origin  = staff.getLocation();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(staff)) continue;
            if (plugin.getStaffManager().isInStaffMode(p.getUniqueId())) continue;
            if (!p.getWorld().equals(staff.getWorld())) continue;
            double d = origin.distanceSquared(p.getLocation());
            if (d < nearestD) { nearestD = d; nearest = p; }
        }
        return nearest;
    }

    private void sendInspect(Player staff, Player target) {
        PlayerData data = GlobalPlayer.get(target);
        String rank = plugin.getRankManager() != null
                ? plugin.getRankManager().getChatPrefix(target.getUniqueId())
                : "&7[Baron]";
        staff.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        staff.sendMessage(CC.translate("  &a&l🌿 Inspect &7» &f" + target.getName()));
        staff.sendMessage(CC.translate("  &7Rank   &8» " + rank));
        if (data != null) {
            staff.sendMessage(CC.translate("  &7Coins  &8» &e" + data.getCoins() + "  &7Gems: &d" + data.getGems()));
            staff.sendMessage(CC.translate("  &7Level  &8» &a" + data.getLevel()));
            staff.sendMessage(CC.translate("  &7Banned &8» " + (data.isBanned() ? "&cYes" : "&aNo")
                    + "  &7Muted: " + (data.isMuted() ? "&cYes" : "&aNo")));
        }
        boolean frozen = FreezeCommand.isFrozenStatic(target.getUniqueId());
        staff.sendMessage(CC.translate("  &7Frozen &8» " + (frozen ? "&eYes" : "&aNo")));
        staff.sendMessage(CC.translate("  &7Ping   &8» &7" + getPing(target) + "ms"));
        staff.sendMessage(CC.translate("  &7Staff  &8» " + (plugin.getStaffManager().isInStaffMode(target.getUniqueId()) ? "&eYes" : "&aNo")));
        staff.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void sendPlayerList(Player staff) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        staff.sendMessage(CC.translate("&2&l🌿 Online Players &7(" + online.size() + "):"));
        for (Player p : online) {
            boolean inStaff = plugin.getStaffManager().isInStaffMode(p.getUniqueId());
            boolean frozen  = FreezeCommand.isFrozenStatic(p.getUniqueId());
            PlayerData pd   = GlobalPlayer.get(p);
            staff.sendMessage(CC.translate(
                    "  &7» &f" + p.getName()
                    + (inStaff ? " &8[Staff]" : "")
                    + (frozen  ? " &c[Frozen]" : "")
                    + "  &7Level: &a" + (pd != null ? pd.getLevel() : "?")
            ));
        }
    }

    private void sendStaffList(Player staff) {
        List<Player> onlineStaff = plugin.getStaffManager().getOnlineStaff();
        staff.sendMessage(CC.translate("&2&l🌿 Staff Members &7(" + onlineStaff.size() + "):"));
        for (Player s : onlineStaff) {
            boolean inMode = plugin.getStaffManager().isInStaffMode(s.getUniqueId());
            staff.sendMessage(CC.translate(
                    "  &7» &f" + s.getName()
                    + (inMode ? " &a[In Staff Mode]" : " &7[Normal]")
                    + "  &7Ping: &b" + getPing(s) + "ms"
            ));
        }
        if (onlineStaff.isEmpty())
            staff.sendMessage(CC.translate("  &8No staff currently online."));
    }

    private int getPing(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            return (int) handle.getClass().getField("ping").get(handle);
        } catch (Exception e) { return -1; }
    }
}
