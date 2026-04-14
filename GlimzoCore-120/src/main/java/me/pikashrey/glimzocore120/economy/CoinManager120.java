package me.pikashrey.glimzocore120.economy;

import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore120.GlimzoCore120;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CoinManager120 {

    private final GlimzoCore120 plugin;

    public CoinManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    public long getCoins(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().get(uuid);
        return data != null ? data.getCoins() : 0;
    }

    public void addCoins(UUID uuid, long amount, String reason) {
        PlayerData data = plugin.getPlayerDataManager().get(uuid);
        if (data == null) return;
        data.setCoins(data.getCoins() + amount);

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            player.sendMessage(Component.text(
                    "+" + amount + " Coins", NamedTextColor.GOLD));
        }
        plugin.getPlayerDataManager().saveAsync(uuid);
    }

    public void removeCoins(UUID uuid, long amount, String reason) {
        PlayerData data = plugin.getPlayerDataManager().get(uuid);
        if (data == null) return;
        data.setCoins(Math.max(0, data.getCoins() - amount));
        plugin.getPlayerDataManager().saveAsync(uuid);
    }

    public void setCoins(UUID uuid, long amount) {
        PlayerData data = plugin.getPlayerDataManager().get(uuid);
        if (data == null) return;
        data.setCoins(amount);
        plugin.getPlayerDataManager().saveAsync(uuid);
    }
}
