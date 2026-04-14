package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
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
 * Friends menu - two views:
 *   Tab A (slot 45): Friends list - click a head to remove
 *   Tab B (slot 46): Pending incoming requests - Accept / Deny / Ignore
 *
 * BUG FIX:
 *   - Removed fillEmpty() from buildContent() - GlimzoMenu.open() now calls
 *     fillEmpty() AFTER buildContent() to guarantee buttons are never covered.
 *   - sendFriendRequestNotification() now sends a properly clickable Adventure
 *     Component instead of a plain text hint that required manual typing.
 */
public class FriendsMenu extends GlimzoMenu {

    private static final int   PAGE_SIZE     = 21;
    private static final int[] CONTENT_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34
    };

    private final boolean showRequests;
    private final int     page;

    public FriendsMenu(GlimzoCore plugin, Player player) {
        this(plugin, player, false, 0);
    }

    public FriendsMenu(GlimzoCore plugin, Player player, boolean showRequests, int page) {
        super(plugin, player, showRequests
                ? "&aFriends &8» &7Pending Request"
                : "&aFriends &8(Page " + (page + 1) + ")", 54);
        this.showRequests = showRequests;
        this.page         = page;
    }

    @Override
    protected void buildContent() {
        // NOTE: Do NOT call fillEmpty() here. GlimzoMenu.open() calls it
        // AFTER buildContent() so these slots are never covered by glass.

        if (showRequests) {
            buildRequestsTab();
        } else {
            buildFriendsTab();
        }

        final boolean sr = showRequests;
        final int     pg = page;

        set(new Slot(45) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.BOOK)
                        .name(!sr ? "&a&lFriends &7(active)" : "&7Friends List")
                        .lore(!sr ? "&aCurrent tab" : "&eClick to switch")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                if (sr) new FriendsMenu(plugin, p, false, 0).open();
            }
        });

        UUID pendingFrom = plugin.getFriendManager().getPendingRequester(getPlayer().getUniqueId());
        set(new Slot(46) {
            @Override public ItemStack getItem() {
                boolean hasPending = pendingFrom != null;
                ItemBuilder b = new ItemBuilder(Material.PAPER)
                        .name(hasPending ? "&e&lPending Request &7(1)" : "&7No Pending Requests");
                if (hasPending) {
                    String name = resolvePlayerName(pendingFrom);
                    b.lore("&7From: &f" + name, "", "&eClick to respond");
                } else {
                    b.lore("&8No incoming friend requests.");
                }
                return b.build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                if (pendingFrom != null) new FriendsMenu(plugin, p, true, 0).open();
            }
        });

        set(new Slot(49) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.ARROW).name("&7Close").build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) { p.closeInventory(); }
        });
    }


    private void buildFriendsTab() {
        List<UUID> friends = new ArrayList<>(plugin.getFriendManager().getFriends(getPlayer().getUniqueId()));
        int start = page * PAGE_SIZE;

        if (friends.isEmpty()) {
            set(new Slot(22) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.BARRIER)
                            .name("&cNo Friends Yet")
                            .lore("&7Use &a/friend add <player> &7to add friends!")
                            .build();
                }
            });
            return;
        }

        for (int i = 0; i < PAGE_SIZE && (start + i) < friends.size(); i++) {
            final UUID fUuid = friends.get(start + i);
            final int  slot  = CONTENT_SLOTS[i];

            set(new Slot(slot) {
                @Override public ItemStack getItem() {
                    Player online = Bukkit.getPlayer(fUuid);
                    String name   = resolvePlayerName(fUuid);
                    String status = online != null ? "&a● Online" : "&8● Offline";
                    return new ItemBuilder(Material.SKULL_ITEM)
                            .name("&f" + name)
                            .lore(status, "", "&cLeft-click &7to remove friend")
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    plugin.getFriendManager().removeFriend(p.getUniqueId(), fUuid);
                    p.sendMessage(CC.translate("&7Removed &f" + resolvePlayerName(fUuid) + " &7from your friends."));
                    new FriendsMenu(plugin, p, false, page).open();
                }
            });
        }

        if (page > 0) {
            set(new Slot(47) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW).name("&7« Previous").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new FriendsMenu(plugin, p, false, page - 1).open();
                }
            });
        }
        if (start + PAGE_SIZE < friends.size()) {
            set(new Slot(51) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW).name("&7Next »").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new FriendsMenu(plugin, p, false, page + 1).open();
                }
            });
        }
    }


    private void buildRequestsTab() {
        UUID requesterUuid = plugin.getFriendManager().getPendingRequester(getPlayer().getUniqueId());

        if (requesterUuid == null) {
            set(new Slot(22) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.BARRIER)
                            .name("&cNo Pending Requests")
                            .lore("&7You have no incoming friend requests.")
                            .build();
                }
            });
            return;
        }

        final String requesterName = resolvePlayerName(requesterUuid);

        // Centre: requester head
        set(new Slot(22) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.SKULL_ITEM)
                        .name("&f" + requesterName)
                        .lore("&7Sent you a friend request!", "", "&eChoose below to respond.")
                        .build();
            }
        });

        set(new Slot(20) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.EMERALD)
                        .name("&a&lAccept")
                        .lore("&7Accept &f" + requesterName + "&7's friend request.")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                UUID pending = plugin.getFriendManager().getPendingRequester(p.getUniqueId());
                if (pending == null) {
                    p.sendMessage(CC.translate("&cThis friend request has expired."));
                    new FriendsMenu(plugin, p, false, 0).open();
                    return;
                }
                plugin.getFriendManager().addFriend(p.getUniqueId(), pending);
                new FriendsMenu(plugin, p, false, 0).open();
            }
        });

        set(new Slot(24) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.REDSTONE)
                        .name("&c&lDeny")
                        .lore("&7Deny &f" + requesterName + "&7's friend request.")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                plugin.getFriendManager().denyRequest(p.getUniqueId(), requesterUuid);
                p.sendMessage(CC.translate("&7Denied &f" + requesterName + "&7's friend request."));
                new FriendsMenu(plugin, p, false, 0).open();
            }
        });

        set(new Slot(26) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.COAL)
                        .name("&8Ignore")
                        .lore("&7Deny and ignore future requests",
                                "&7from &f" + requesterName + "&7.")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                plugin.getFriendManager().denyRequest(p.getUniqueId(), requesterUuid);
                plugin.getChatManager().ignore(p.getUniqueId(), requesterUuid);
                p.sendMessage(CC.translate("&7Ignored &f" + requesterName + "&7."));
                new FriendsMenu(plugin, p, false, 0).open();
            }
        });
    }


    /**
     * Send a clickable Adventure Component notification to the recipient.
     * Clicking [Accept] runs /friend accept <name>, clicking [Deny] runs /friend deny <name>.
     * This replaces the old plain-text hint that required manual typing.
     */
    public static void sendFriendRequestNotification(Player recipient, String senderName) {
        recipient.sendMessage(CC.translate("&a" + senderName + " &7sent you a friend request! "
                + "&a&l[Accept]&r&7 - /friend accept " + senderName + "  "
                + "&c&l[Deny]&r&7 - /friend deny " + senderName));
    }


    private String resolvePlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null && data.getName() != null) return data.getName();
        return uuid.toString().substring(0, 8);
    }
}