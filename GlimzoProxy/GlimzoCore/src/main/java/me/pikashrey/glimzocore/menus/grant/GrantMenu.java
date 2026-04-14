package me.pikashrey.glimzocore.menus.grant;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.api.rank.grant.Grant;
import me.pikashrey.glimzocore.data.grant.GrantProcedure;
import me.pikashrey.glimzocore.data.grant.GrantProcedureState;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;
import me.pikashrey.glimzocore.menu.menu.GlimzoMenu;
import me.pikashrey.glimzocore.menu.slots.Slot;
import me.pikashrey.glimzocore.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GrantMenu extends GlimzoMenu {
    private final GrantProcedure procedure;

    public GrantMenu(GlimzoCore plugin, Player staff, GrantProcedure procedure) {
        super(plugin, staff, "&6Grant Rank &7» &f" + procedure.getTargetName(), 54);
        this.procedure = procedure;
    }

    @Override
    protected void buildContent() {
        fillEmpty();
        if (procedure.getState() == GrantProcedureState.SELECT_RANK) {
            buildRankSelection();
        } else if (procedure.getState() == GrantProcedureState.SELECT_DURATION) {
            buildDurationSelection();
        } else {
            buildConfirm();
        }
    }

    private void buildRankSelection() {
        // Build rank list from config (sorted by weight descending) - includes config-only ranks
        List<ConfiguredRank> configRanks = new ArrayList<>(plugin.getRankLoader().getAll());
        configRanks.sort(Comparator.comparingInt(ConfiguredRank::getWeight).reversed());

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31};
        for (int i = 0; i < Math.min(configRanks.size(), slots.length); i++) {
            final ConfiguredRank cfgRank = configRanks.get(i);
            final RankRef ref = RankRef.of(cfgRank.getId());
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    return new ItemBuilder(Material.WOOL)
                            .name(ref.getColorCode() + ref.getDisplayName())
                            .lore("&7Click to select",
                                  cfgRank.isDonor() ? "&6Donor rank" : (cfgRank.isStaff() ? "&bStaff rank" : "&7Standard rank"),
                                  "&7Weight: &f" + cfgRank.getWeight())
                            .build();
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    procedure.setSelectedRank(ref);
                    procedure.setState(GrantProcedureState.SELECT_DURATION);
                    new GrantMenu(plugin, p, procedure).open();
                }
            });
        }
    }

    private void buildDurationSelection() {
        long[] durations = {-1, 3600_000L, 86400_000L, 7 * 86400_000L, 30L * 86400_000L};
        String[] labels  = {"Permanent", "1 Hour", "1 Day", "1 Week", "1 Month"};
        int[] slots = {11, 13, 15, 21, 23};
        for (int i = 0; i < durations.length; i++) {
            final long dur = durations[i];
            final String label = labels[i];
            set(new Slot(slots[i]) {
                @Override public ItemStack getItem() {
                    return ItemBuilder.of(Material.WATCH, "&e" + label, "&7Click to select");
                }
                @Override public void onClick(Player p, InventoryClickEvent e) {
                    procedure.setSelectedDuration(dur);
                    procedure.setState(GrantProcedureState.CONFIRM);
                    new GrantMenu(plugin, p, procedure).open();
                }
            });
        }
    }

    private void buildConfirm() {
        RankRef rank = procedure.getSelectedRank();
        String dur = procedure.getSelectedDuration() == -1 ? "Permanent"
                : me.pikashrey.glimzocore.utilities.general.TimeFormatUtils
                        .format(procedure.getSelectedDuration());

        set(new Slot(11) {
            @Override public ItemStack getItem() {
                return new ItemBuilder(Material.EMERALD_BLOCK)
                        .name("&aConfirm Grant")
                        .lore("&7Player: &f" + procedure.getTargetName(),
                              "&7Rank: " + rank.getColorCode() + rank.getDisplayName(),
                              "&7Duration: &f" + dur, " ", "&aClick to confirm")
                        .build();
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                Grant g = Grant.create(
                        procedure.getTargetUuid(), procedure.getTargetName(),
                        p.getUniqueId(), p.getName(),
                        rank, procedure.computeExpiresAt(), true);
                plugin.getRankManager().addGrant(g);
                plugin.getRankManager().clearProcedure(p.getUniqueId());
                p.closeInventory();
                p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate(
                        "&aGranted " + rank.getColorCode() + rank.getDisplayName()
                        + " &ato &f" + procedure.getTargetName() + "&a."));
            }
        });

        set(new Slot(15) {
            @Override public ItemStack getItem() {
                return ItemBuilder.of(Material.REDSTONE_BLOCK, "&cCancel");
            }
            @Override public void onClick(Player p, InventoryClickEvent e) {
                plugin.getRankManager().clearProcedure(p.getUniqueId());
                p.closeInventory();
                p.sendMessage(me.pikashrey.glimzocore.utilities.chat.CC.translate("&cGrant cancelled."));
            }
        });
    }
}
