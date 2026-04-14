package me.pikashrey.glimzocore.menus.punishments;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.punishment.PunishmentType;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import me.pikashrey.glimzocore.menu.menu.SwitchableMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.menu.slots.pages.NextPageSlot;
import me.pikashrey.glimzocore.menu.slots.pages.PreviousPageSlot;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckMenu extends SwitchableMenu {
    private final UUID targetUuid;
    private final String targetName;
    private PunishmentFilter filter = PunishmentFilter.ALL;
    private static final int PAGE_SIZE = 18;

    public CheckMenu(GlimzoCore plugin, Player viewer, UUID targetUuid, String targetName) {
        super(plugin, viewer, "&cCheck &7» &f" + targetName, 54);
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    @Override
    protected void buildContent() {
        fillEmpty();
        
        Punishment activeBan = plugin.getPunishmentManager().getActiveBan(targetUuid);
        Punishment activeMute = plugin.getPunishmentManager().getActiveMute(targetUuid);
        List<Punishment> history = plugin.getPunishmentManager().getHistory(targetUuid);

        // Filter buttons at top
        set(new Slot(10) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(filter == PunishmentFilter.ALL ? Material.DIAMOND_BLOCK : Material.COAL_BLOCK)
                        .name("&7All Punishments")
                        .lore(filter == PunishmentFilter.ALL ? "&a✓ Selected" : "&7Click to select")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                filter = PunishmentFilter.ALL;
                currentPage = 0;
                refreshMenu();
            }
        });

        set(new Slot(12) {
            @Override public ItemStack getItem() {
                long banCount = history.stream().filter(p -> p.getType() == PunishmentType.BAN || p.getType() == PunishmentType.TEMP_BAN).count();
                return new ItemBuilder(filter == PunishmentFilter.BANS ? Material.REDSTONE_BLOCK : Material.COAL_BLOCK)
                        .name("&cBans (&f" + banCount + "&c)")
                        .lore(filter == PunishmentFilter.BANS ? "&a✓ Selected" : "&7Click to view ban history")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                filter = PunishmentFilter.BANS;
                currentPage = 0;
                refreshMenu();
            }
        });

        set(new Slot(14) {
            @Override public ItemStack getItem() {
                long muteCount = history.stream().filter(p -> p.getType() == PunishmentType.MUTE || p.getType() == PunishmentType.TEMP_MUTE).count();
                return new ItemBuilder(filter == PunishmentFilter.MUTES ? Material.GOLD_BLOCK : Material.COAL_BLOCK)
                        .name("&eMutes (&f" + muteCount + "&e)")
                        .lore(filter == PunishmentFilter.MUTES ? "&a✓ Selected" : "&7Click to view mute history")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                filter = PunishmentFilter.MUTES;
                currentPage = 0;
                refreshMenu();
            }
        });

        set(new Slot(16) {
            @Override public ItemStack getItem() {
                long warnCount = history.stream().filter(p -> p.getType() == PunishmentType.WARN).count();
                return new ItemBuilder(filter == PunishmentFilter.WARNS ? Material.BOOK_AND_QUILL : Material.COAL_BLOCK)
                        .name("&6Warns (&f" + warnCount + "&6)")
                        .lore(filter == PunishmentFilter.WARNS ? "&a✓ Selected" : "&7Click to view warnings")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                filter = PunishmentFilter.WARNS;
                currentPage = 0;
                refreshMenu();
            }
        });

        // Show active punishments
        set(new Slot(27) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(activeBan != null ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK)
                        .name(activeBan != null ? "&c⚠ BANNED (ACTIVE)" : "&a✓ No Active Ban")
                        .lore(activeBan != null
                                ? new String[]{"&7Reason: &f" + activeBan.getReason(),
                                    "&7By: &f" + activeBan.getStaffName(),
                                    "&7Expires: &f" + TimeFormatUtils.formatExpiry(activeBan.getExpiresAt())}
                                : new String[]{"&7Player is not banned."})
                        .build();
            }
        });

        set(new Slot(29) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(activeMute != null ? Material.GOLD_BLOCK : Material.EMERALD_BLOCK)
                        .name(activeMute != null ? "&e⚠ MUTED (ACTIVE)" : "&a✓ No Active Mute")
                        .lore(activeMute != null
                                ? new String[]{"&7Reason: &f" + activeMute.getReason(),
                                    "&7By: &f" + activeMute.getStaffName(),
                                    "&7Expires: &f" + TimeFormatUtils.formatExpiry(activeMute.getExpiresAt())}
                                : new String[]{"&7Player is not muted."})
                        .build();
            }
        });

        // Filtered history
        List<Punishment> filtered = getFilteredHistory(history);
        int start = currentPage * PAGE_SIZE;
        
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40};
        
        for (int i = 0; i < PAGE_SIZE && (start + i) < filtered.size(); i++) {
            final Punishment p = filtered.get(start + i);
            final int slot = slots[i];
            
            set(new Slot(slot) {
                @Override public ItemStack getItem() {
                    Material mat = p.getType() == PunishmentType.BAN || p.getType() == PunishmentType.TEMP_BAN ? Material.REDSTONE
                            : p.getType() == PunishmentType.MUTE || p.getType() == PunishmentType.TEMP_MUTE ? Material.GOLD_NUGGET
                            : Material.PAPER;
                    
                    String typeStr = p.getType().name().replace("_", " ");
                    return new ItemBuilder(mat)
                            .name("&7" + typeStr + (p.isActive() ? " &a[ACTIVE]" : " &7[INACTIVE]"))
                            .lore("&7Reason: &f" + p.getReason(),
                                  "&7By: &f" + p.getStaffName(),
                                  "&7Date: &f" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(p.getIssuedAt())),
                                  "&7Expires: &f" + TimeFormatUtils.formatExpiry(p.getExpiresAt()))
                            .build();
                }
            });
        }
        
        if (currentPage > 0) set(new PreviousPageSlot(45, this::prevPage));
        if (start + PAGE_SIZE < filtered.size()) set(new NextPageSlot(53, this::nextPage));
    }

    private List<Punishment> getFilteredHistory(List<Punishment> history) {
        List<Punishment> result = new ArrayList<>();
        for (Punishment p : history) {
            if (filter == PunishmentFilter.ALL) {
                result.add(p);
            } else if (filter == PunishmentFilter.BANS && (p.getType() == PunishmentType.BAN || p.getType() == PunishmentType.TEMP_BAN)) {
                result.add(p);
            } else if (filter == PunishmentFilter.MUTES && (p.getType() == PunishmentType.MUTE || p.getType() == PunishmentType.TEMP_MUTE)) {
                result.add(p);
            } else if (filter == PunishmentFilter.WARNS && p.getType() == PunishmentType.WARN) {
                result.add(p);
            }
        }
        return result;
    }

    private void refreshMenu() {
        getPlayer().closeInventory();
        open();
    }

    enum PunishmentFilter {
        ALL, BANS, MUTES, WARNS
    }
}

