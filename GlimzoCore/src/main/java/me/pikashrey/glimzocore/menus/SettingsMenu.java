package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.player.PlayerSettings;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SettingsMenu extends GlimzoMenu {
    public SettingsMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&8Settings", 36);
    }
    @Override
    protected void buildContent() {
        fillEmpty();
        PlayerData data = GlobalPlayer.get(getPlayer());
        if (data == null) return;
        PlayerSettings s = data.getSettings();

        addToggle(10, Material.PAPER, "&bPrivate Messages", s.isPrivateMessagesEnabled(),
                p -> { plugin.getSettingsManager().togglePrivateMessages(p.getUniqueId()); new SettingsMenu(plugin, p).open(); });
        addToggle(11, Material.EMERALD, "&aFriend Requests", s.isFriendRequestsEnabled(),
                p -> { plugin.getSettingsManager().toggleFriendRequests(p.getUniqueId()); new SettingsMenu(plugin, p).open(); });
        addToggle(12, Material.REDSTONE, "&dParty Invites", s.isPartyInvitesEnabled(),
                p -> { plugin.getSettingsManager().togglePartyInvites(p.getUniqueId()); new SettingsMenu(plugin, p).open(); });
        addToggle(13, Material.GOLD_INGOT, "&6Clan Invites", s.isClanInvitesEnabled(),
                p -> { plugin.getSettingsManager().toggleClanInvites(p.getUniqueId()); new SettingsMenu(plugin, p).open(); });
        addToggle(14, Material.MAP, "&eSidebar", s.isScoreboardEnabled(),
                p -> { plugin.getSettingsManager().toggleScoreboard(p.getUniqueId()); new SettingsMenu(plugin, p).open(); });
        addToggle(15, Material.EYE_OF_ENDER, "&7Join Messages", s.isJoinMessagesEnabled(),
                p -> { plugin.getSettingsManager().toggleJoinMessages(p.getUniqueId()); new SettingsMenu(plugin, p).open(); });
        addToggle(22, Material.QUARTZ, "&fPlayer Visibility", s.isPlayerVisibilityEnabled(),
                p -> { plugin.getSettingsManager().togglePlayerVisibility(p.getUniqueId()); new SettingsMenu(plugin, p).open(); });
    }

    private void addToggle(int slot, Material mat, String name, boolean state, java.util.function.Consumer<Player> action) {
        set(new Slot(slot) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(mat)
                        .name(name)
                        .lore((state ? "&aEnabled" : "&cDisabled") + " &7- Click to toggle")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) { action.accept(p); }
        });
    }
}
