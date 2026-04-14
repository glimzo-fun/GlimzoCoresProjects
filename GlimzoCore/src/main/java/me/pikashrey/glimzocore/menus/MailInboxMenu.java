package me.pikashrey.glimzocore.menus;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.mail.MailManager;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Mail inbox GUI.
 *
 * Each slot shows one mail. Clicking a mail closes the GUI and prints the
 * full message to chat, then marks it as read.
 *
 * Layout (54-slot chest):
 *   Row 1   - header bar (glass pane filler)
 *   Rows 2-5 - mail items  (slots 9–44, 4 rows × 7 usable = 28 per page)
 *   Row 6   - footer bar  (prev page | info | next page)
 *
 * Loaded async before the menu is opened (see MailCommand).
 */
public class MailInboxMenu extends GlimzoMenu {

    private static final SimpleDateFormat DATE_FMT  = new SimpleDateFormat("MMM d, HH:mm");
    private static final int              PAGE_SIZE  = 28;
    // Mail slots: 9–44 (rows 2-5), skipping border columns 0,8,9+7=16... 
    // Use the inner 7 columns per row: cols 1-7 of each row
    private static final int[] MAIL_SLOTS = buildMailSlots();

    private final List<MailManager.MailEntry> mails;
    private       int                          page;

    /** Call this constructor after loading mails from DB (async). */
    public MailInboxMenu(GlimzoCore plugin, Player player,
                         List<MailManager.MailEntry> mails, int page) {
        super(plugin, player, "&2&l\uD83C\uDF3F Mail &8\u00BB &7Inbox", 54);
        this.mails = mails;
        this.page  = page;
    }

    @Override
    protected void buildContent() {
        ItemStack header = new ItemBuilder(Material.STAINED_GLASS_PANE).durability((short) 13)
                .name(" ").build();
        for (int i = 0; i < 9; i++) {
            final int fi = i;
            set(new Slot(fi) {
                @Override public ItemStack getItem()                       { return header; }
                @Override public void onClick(Player p, InventoryClickEvent e) {}
            });
        }

        ItemStack border = new ItemBuilder(Material.STAINED_GLASS_PANE).durability((short) 7)
                .name(" ").build();
        for (int row = 1; row <= 4; row++) {
            final int left  = row * 9;
            final int right = row * 9 + 8;
            set(new Slot(left)  { @Override public ItemStack getItem() { return border; }
                                  @Override public void onClick(Player p, InventoryClickEvent e) {} });
            set(new Slot(right) { @Override public ItemStack getItem() { return border; }
                                  @Override public void onClick(Player p, InventoryClickEvent e) {} });
        }

        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int mailIndex = start + i;
            int guiSlot   = MAIL_SLOTS[i];

            if (mailIndex >= mails.size()) {
                set(new Slot(guiSlot) {
                    @Override public ItemStack getItem() {
                        return new ItemBuilder(Material.STAINED_GLASS_PANE).durability((short) 15)
                                .name(" ").build();
                    }
                    @Override public void onClick(Player p, InventoryClickEvent e) {}
                });
                continue;
            }

            final MailManager.MailEntry mail = mails.get(mailIndex);
            final boolean isRead = mail.isRead();
            final String  date   = DATE_FMT.format(new Date(mail.sentAt));

            set(new Slot(guiSlot) {
                @Override
                public ItemStack getItem() {
                    Material icon = isRead ? Material.BOOK : Material.WRITTEN_BOOK;
                    String   nameColor = isRead ? "&7" : "&a";
                    return new ItemBuilder(icon)
                            .name(nameColor + (isRead ? "[READ] " : "[NEW] ") + "&fFrom &d" + mail.senderName)
                            .lore(
                                "&8Received: &7" + date,
                                " ",
                                "&7Click to read"
                            ).build();
                }

                @Override
                public void onClick(Player p, InventoryClickEvent e) {
                    p.closeInventory();
                    // Print the mail to chat
                    p.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    p.sendMessage(CC.translate("  &a&lMail &8#" + mail.id));
                    p.sendMessage(CC.translate("  &7From   &8» &d" + mail.senderName));
                    p.sendMessage(CC.translate("  &7Date   &8» &7" + date));
                    p.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    p.sendMessage(CC.translate("  &f" + mail.body));
                    p.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    p.sendMessage(CC.translate("  &8/mail delete " + mail.id + " &8- to delete this mail"));
                    // Mark read async
                    Bukkit.getScheduler().runTaskAsynchronously(plugin,
                            () -> plugin.getMailManager().markRead(mail.id));
                }
            });
        }

        buildFooter();
    }

    private void buildFooter() {
        // Bottom border
        ItemStack foot = new ItemBuilder(Material.STAINED_GLASS_PANE).durability((short) 13)
                .name(" ").build();
        for (int i = 45; i < 54; i++) {
            if (i == 45 || i == 53) continue; // reserved for nav
            final int fi = i;
            set(new Slot(fi) {
                @Override public ItemStack getItem()                       { return foot; }
                @Override public void onClick(Player p, InventoryClickEvent e) {}
            });
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) mails.size() / PAGE_SIZE));

        // Info item (centre)
        set(new Slot(49) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.PAPER)
                        .name("&7Page &f" + (page + 1) + " &7of &f" + totalPages)
                        .lore("&7Total mails: &f" + mails.size()).build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {}
        });

        // Previous page
        if (page > 0) {
            set(new Slot(45) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW)
                            .name("&a« Previous Page").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new MailInboxMenu(plugin, p, mails, page - 1).open();
                }
            });
        }

        // Next page
        if ((page + 1) * PAGE_SIZE < mails.size()) {
            set(new Slot(53) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.ARROW)
                            .name("&aNext Page »").build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    new MailInboxMenu(plugin, p, mails, page + 1).open();
                }
            });
        }
    }


    /** Build the 28 inner mail slot positions (cols 1-7, rows 1-4). */
    private static int[] buildMailSlots() {
        int[] slots = new int[28];
        int idx = 0;
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                slots[idx++] = row * 9 + col;
            }
        }
        return slots;
    }
}
