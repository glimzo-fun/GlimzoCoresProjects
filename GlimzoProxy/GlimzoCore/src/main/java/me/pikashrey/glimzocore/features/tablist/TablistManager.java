package me.pikashrey.glimzocore.features.tablist;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.utilities.general.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TablistManager {

    protected final GlimzoCore plugin;

    public TablistManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        // Initialize tablist for all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateTablist(p);
        }
    }

    public void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) clearTablist(p);
    }

    public void updateTablist(Player player) {
        PlayerData data = GlobalPlayer.get(player);
        if (data == null) return;

        RankRef rank = plugin.getRankManager().getActiveRankRef(player);
        double tps = ServerUtils.getTps();
        String tpsColor = tps >= 19.0 ? "&a" : tps >= 16.0 ? "&e" : "&c";

        String header = CC.translate(
                "\n  &2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                + "           &a&l🌿 GLIMZO NETWORK &a&l🌿\n"
                + "  &2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                + "           &7" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + " players in the forest"
                + "   " + tpsColor + String.format("%.1f", tps) + " TPS\n");

        String footer = CC.translate(
                "\n  &7Rank &8» " + rank.getColorCode() + rank.getDisplayName()
                + "    &7Level &8» &a" + data.getLevel()
                + (data.getPrestige() > 0 ? " &2✦" + data.getPrestige() : "")
                + "\n  &2play.glimzo.net &8|  &7store.glimzo.net\n");

        sendTabList(player, header, footer);
    }

    private void clearTablist(Player player) {
        sendTabList(player, "", "");
    }

    private static void sendTabList(Player player, String header, String footer) {
        try {
            net.md_5.bungee.api.chat.BaseComponent[] h =
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(header);
            net.md_5.bungee.api.chat.BaseComponent[] f =
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(footer);
            player.getClass()
                  .getMethod("setPlayerListHeaderFooter",
                          net.md_5.bungee.api.chat.BaseComponent[].class,
                          net.md_5.bungee.api.chat.BaseComponent[].class)
                  .invoke(player, h, f);
        } catch (Exception ignored) {
            // Server version doesn't support tab header/footer - silently skip
        }
    }
}

