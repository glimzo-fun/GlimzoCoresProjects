package me.pikashrey.glimzocore.menus.grant;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.grant.Grant;
import me.pikashrey.glimzocore.menu.menu.SwitchableMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.menu.slots.pages.NextPageSlot;
import me.pikashrey.glimzocore.menu.slots.pages.PreviousPageSlot;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.general.TimeFormatUtils;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class GrantsMenu extends SwitchableMenu {

    private static final int PAGE_SIZE = 28;

    private final UUID   targetUuid;
    private final String targetName;

    public GrantsMenu(GlimzoCore plugin, Player viewer, UUID targetUuid, String targetName) {
        super(plugin, viewer, "&6Grants &7» &f" + targetName, 54);
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    @Override
    protected void buildContent() {
        fillEmpty();
        List<Grant> grants = plugin.getRankManager().getAllGrants(targetUuid);
        int start = currentPage * PAGE_SIZE;

        int[] contentSlots = new int[PAGE_SIZE];
        int si = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                contentSlots[si++] = row * 9 + col;

        for (int i = 0; i < PAGE_SIZE && (start + i) < grants.size(); i++) {
            final Grant g     = grants.get(start + i);
            final int   slot  = contentSlots[i];

            set(new Slot(slot) {
                @Override
                public ItemStack getItem() {
                    Material mat = g.isActive() ? Material.EMERALD : Material.REDSTONE;
                    return new ItemBuilder(mat)
                            .name(g.getRankRef().getColorCode() + g.getRankRef().getDisplayName()
                                    + (g.isActive() ? " &a[ACTIVE]" : " &c[INACTIVE]"))
                            .lore(
                                "&7Granted by: &f" + g.getIssuedByName(),
                                "&7Type: " + (g.isStaffGrant() ? "&bStaff" : "&6Donor"),
                                "&7Expires: &f" + TimeFormatUtils.formatExpiry(g.getExpiresAt()),
                                " ",
                                g.isActive() ? "&cClick to revoke" : "&8Revoked/Expired"
                            ).build();
                }

                @Override
                public void onClick(Player staff, InventoryClickEvent e) {
                    // Only active grants can be revoked
                    if (!g.isActive()) return;
                    // Require the same permission as /rank revoke
                    if (!staff.hasPermission("glimzo.rank.revoke")) {
                        staff.sendMessage(CC.translate("&cYou don't have permission to revoke grants."));
                        return;
                    }
                    plugin.getRankManager().revokeGrant(targetUuid, g.getId());
                    staff.sendMessage(CC.translate(
                            "&aRevoked " + g.getRankRef().getColorCode()
                            + g.getRankRef().getDisplayName()
                            + " &afrom &f" + targetName + "&a."));
                    // Refresh the menu so the item immediately shows INACTIVE
                    new GrantsMenu(plugin, staff, targetUuid, targetName).open();
                }
            });
        }

        if (currentPage > 0) set(new PreviousPageSlot(45, this::prevPage));
        if (start + PAGE_SIZE < grants.size()) set(new NextPageSlot(53, this::nextPage));
    }
}
