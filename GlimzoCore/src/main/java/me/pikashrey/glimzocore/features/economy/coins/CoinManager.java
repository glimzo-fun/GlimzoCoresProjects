package me.pikashrey.glimzocore.features.economy.coins;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.economy.Currency;
import me.pikashrey.glimzocore.api.economy.Transaction;
import me.pikashrey.glimzocore.api.events.impl.CoinsTransactionEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CoinManager {

    protected final GlimzoCore plugin;

    public CoinManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public long addCoins(UUID uuid, long amount, String reason) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null || amount <= 0) return 0;

        Transaction tx = new Transaction(uuid, Currency.COINS, Transaction.Type.ADD, amount, reason);
        CoinsTransactionEvent event = new CoinsTransactionEvent(data, tx);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;

        data.addCoins(amount);
        data.getStats().addCoinsEarned(amount);
        return amount;
    }

    public long removeCoins(UUID uuid, long amount, String reason) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null || amount <= 0 || data.getCoins() < amount) return 0;

        Transaction tx = new Transaction(uuid, Currency.COINS, Transaction.Type.REMOVE, amount, reason);
        CoinsTransactionEvent event = new CoinsTransactionEvent(data, tx);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;

        data.removeCoins(amount);
        data.getStats().addCoinsSpent(amount);
        return amount;
    }

    /** Bypasses event system - admin/console use only. */
    public void setCoins(UUID uuid, long amount) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) data.setCoins(amount);
    }

    public long getCoins(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null ? data.getCoins() : 0;
    }

    public boolean hasCoins(UUID uuid, long amount) {
        return getCoins(uuid) >= amount;
    }
}
