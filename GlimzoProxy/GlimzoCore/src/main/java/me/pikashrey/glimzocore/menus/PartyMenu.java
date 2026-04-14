package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.party.Party;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Party menu - shows current party or a pending invite with Accept/Decline buttons.
 *
 * BUG FIX: Removed fillEmpty() from buildContent(). GlimzoMenu.open() now
 * calls fillEmpty() after buildContent() so Accept/Decline buttons (slots 11, 15)
 * are never overwritten by glass panes.
 */
public class PartyMenu extends GlimzoMenu {

    public PartyMenu(GlimzoCore plugin, Player player) {
        super(plugin, player, "&dParty", 36);
    }

    @Override
    protected void buildContent() {
        // No fillEmpty() here - called by GlimzoMenu.open() after this method

        Party party = plugin.getPartyManager().getParty(getPlayer().getUniqueId());
        boolean hasPendingInvite = plugin.getPartyManager().hasPendingInvite(getPlayer().getUniqueId());

        if (party == null && hasPendingInvite) {
            buildInviteResponse();
            return;
        }

        if (party == null) {
            set(new Slot(13) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.BARRIER)
                            .name("&cNot in a party")
                            .lore("&7Use &d/party invite <player> &7to start one.")
                            .build();
                }
            });
            return;
        }

        buildMemberList(party);

        boolean isLeader = party.isLeader(getPlayer().getUniqueId());
        if (isLeader) {
            set(new Slot(31) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.TNT)
                            .name("&c&lDisband Party")
                            .lore("&7Disbands the party for all members.")
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.closeInventory();
                    plugin.getPartyManager().disbandParty(p.getUniqueId());
                }
            });
        } else {
            set(new Slot(31) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.WOOD_DOOR)
                            .name("&e&lLeave Party")
                            .lore("&7Leaves the party.")
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    p.closeInventory();
                    plugin.getPartyManager().leaveParty(p.getUniqueId());
                    p.sendMessage(CC.translate("&7You left the party."));
                }
            });
        }

        set(new Slot(35) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.ARROW).name("&7Close").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) { p.closeInventory(); }
        });
    }


    private void buildMemberList(Party party) {
        List<UUID> members = new ArrayList<>(party.getMembers());
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < Math.min(members.size(), slots.length); i++) {
            final UUID   mUuid = members.get(i);
            final boolean lead = party.isLeader(mUuid);
            final Player  mp   = Bukkit.getPlayer(mUuid);
            final String  mName = mp != null ? mp.getName() : mUuid.toString().substring(0, 8);
            final int     slotIdx = slots[i];
            set(new Slot(slotIdx) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.SKULL_ITEM)
                            .name((lead ? "&6[Leader] " : "&7") + mName)
                            .lore("&7" + (mp != null ? "Online" : "Offline"))
                            .build();
                }
            });
        }
    }


    private void buildInviteResponse() {
        UUID inviterUuid = plugin.getPartyManager().getInviterUuid(getPlayer().getUniqueId());
        String inviterName = "Unknown";
        if (inviterUuid != null) {
            Player inviterPlayer = Bukkit.getPlayer(inviterUuid);
            inviterName = inviterPlayer != null ? inviterPlayer.getName()
                    : inviterUuid.toString().substring(0, 8);
        }
        final String finalInviterName = inviterName;

        // Centre: inviter head
        set(new Slot(13) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.SKULL_ITEM)
                        .name("&f" + finalInviterName)
                        .lore("&7Invited you to their party!", "", "&eChoose below to respond.")
                        .build();
            }
        });

        set(new Slot(11) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.EMERALD)
                        .name("&a&lAccept")
                        .lore("&7Join &f" + finalInviterName + "&7's party.")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                if (!plugin.getPartyManager().hasPendingInvite(p.getUniqueId())) {
                    p.sendMessage(CC.translate("&cThat party invite has expired."));
                    p.closeInventory();
                    return;
                }
                boolean joined = plugin.getPartyManager().joinParty(p.getUniqueId(), p.getName());
                if (joined) {
                    p.sendMessage(CC.translate("&aYou joined the party!"));
                } else {
                    p.sendMessage(CC.translate("&cCould not join the party (it may be full or disbanded)."));
                }
                p.closeInventory();
            }
        });

        set(new Slot(15) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.REDSTONE)
                        .name("&c&lDecline")
                        .lore("&7Decline &f" + finalInviterName + "&7's invite.")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                plugin.getPartyManager().declineInvite(p.getUniqueId());
                p.sendMessage(CC.translate("&7You declined the party invite."));
                p.closeInventory();
            }
        });

        // Close
        set(new Slot(35) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.ARROW).name("&7Close").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) { p.closeInventory(); }
        });
    }


    /**
     * Send a clickable Adventure Component invite notification.
     * /party accept and /party decline run automatically on click.
     */
    public static void sendPartyInviteNotification(Player recipient, String inviterName) {
        recipient.sendMessage(CC.translate("&d" + inviterName + " &7invited you to their party! "
                + "&a&l[Accept]&r&7 - /party accept  "
                + "&c&l[Decline]&r&7 - /party decline"));
    }
}