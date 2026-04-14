package me.pikashrey.glimzocore.features.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.data.staffmode.LastInventory;
import me.pikashrey.glimzocore.data.staffmode.StaffModeItem;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StaffManager {

    protected final GlimzoCore plugin;

    private final Set<UUID>              staffModeActive  = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, LastInventory> savedInventories = new ConcurrentHashMap<>();

    public StaffManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }


    public boolean toggleStaffMode(Player player) {
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) {
            player.sendMessage(CC.translate("&cYour player data isn't loaded. Try reconnecting."));
            return false;
        }

        if (data.isStaffMode()) {
            disableStaffMode(player, data);
            return false;
        } else {
            enableStaffMode(player, data);
            return true;
        }
    }


    private void enableStaffMode(Player player, PlayerData data) {
        // Deep-clone contents before clearing so we don't save null references
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor    = player.getInventory().getArmorContents();
        savedInventories.put(player.getUniqueId(), new LastInventory(contents, armor));

        // Wipe inventory and give staff items
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        for (StaffModeItem item : StaffModeItem.getItems()) {
            player.getInventory().setItem(item.getSlot(), item.getItem());
        }
        player.updateInventory();

        data.setStaffMode(true);
        staffModeActive.add(player.getUniqueId());

        // Auto-vanish on enter
        data.setVanished(true);
        applyVanish(player, true);

        // Fly
        player.setAllowFlight(true);
        player.setFlying(true);

        player.sendMessage(CC.translate("&a&lStaff Mode &aenabled. You are now vanished and flying."));
        player.sendMessage(CC.translate("&7Use &fBarrier &7item (slot 9) to exit."));
    }


    private void disableStaffMode(Player player, PlayerData data) {
        data.setStaffMode(false);
        staffModeActive.remove(player.getUniqueId());

        // Restore old inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        LastInventory saved = savedInventories.remove(player.getUniqueId());
        if (saved != null) {
            player.getInventory().setContents(saved.getContents());
            player.getInventory().setArmorContents(saved.getArmorContents());
        }
        player.updateInventory();

        // Restore hotbar AFTER inventory is restored
        if (plugin.getHotbarManager() != null) {
            plugin.getHotbarManager().giveHotbar(player);
        }

        // Disable vanish
        data.setVanished(false);
        applyVanish(player, false);

        // Remove flight (unless they have a rank perm for it)
        if (!player.hasPermission("glimzo.fly")) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        player.sendMessage(CC.translate("&7Staff mode &cdisabled&7. Welcome back."));
    }


    public void toggleVanish(Player player) {
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return;
        boolean next = !data.isVanished();
        data.setVanished(next);
        applyVanish(player, next);
        player.sendMessage(CC.translate(next
                ? "&aYou are now &2vanished&a."
                : "&7You are now &cvisible&7."));
    }

    public void applyVanish(Player player, boolean vanish) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (vanish) {
                if (!canSeeVanished(other)) other.hidePlayer(player);
            } else {
                other.showPlayer(player);
            }
        }
    }

    public void applyVanishOnJoin(Player joining) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(joining)) continue;
            PlayerData d = GlobalPlayer.get(online);
            if (d != null && d.isVanished() && !canSeeVanished(joining)) {
                joining.hidePlayer(online);
            }
        }
    }

    private boolean canSeeVanished(Player p) {
        return p.hasPermission("glimzo.staff.vanish") || p.hasPermission("glimzo.staff.mode");
    }


    public boolean isInStaffMode(UUID uuid) { return staffModeActive.contains(uuid); }

    public List<Player> getOnlineStaff() {
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers())
            if (p.hasPermission("glimzo.staff.mode")) list.add(p);
        return list;
    }

    public void onQuit(UUID uuid) {
        staffModeActive.remove(uuid);
        savedInventories.remove(uuid);
    }
}
