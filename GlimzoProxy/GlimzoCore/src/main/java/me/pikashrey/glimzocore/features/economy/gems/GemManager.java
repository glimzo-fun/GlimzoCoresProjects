package me.pikashrey.glimzocore.features.economy.gems;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.economy.Currency;
import me.pikashrey.glimzocore.api.economy.Transaction;
import me.pikashrey.glimzocore.api.events.impl.GemsTransactionEvent;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.Bukkit;

import java.util.UUID;

public class GemManager {

    protected final GlimzoCore plugin;

    public GemManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public long addGems(UUID uuid, long amount, String reason) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null || amount <= 0) return 0;

        Transaction tx = new Transaction(uuid, Currency.GEMS, Transaction.Type.ADD, amount, reason);
        GemsTransactionEvent event = new GemsTransactionEvent(data, tx);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;

        data.addGems(amount);
        data.getStats().addGemsEarned(amount);
        return amount;
    }

    public long removeGems(UUID uuid, long amount, String reason) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data == null || amount <= 0) return 0;
        if (data.getGems() < amount) return 0;

        Transaction tx = new Transaction(uuid, Currency.GEMS, Transaction.Type.REMOVE, amount, reason);
        GemsTransactionEvent event = new GemsTransactionEvent(data, tx);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;

        data.removeGems(amount);
        data.getStats().addGemsSpent(amount);
        return amount;
    }

    public void setGems(UUID uuid, long amount) {
        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null) data.setGems(amount);
    }

    public long getGems(UUID uuid) {
        PlayerData data = GlobalPlayer.get(uuid);
        return data != null ? data.getGems() : 0;
    }

    public boolean hasGems(UUID uuid, long amount) {
        return getGems(uuid) >= amount;
    }
}

