package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.economy.Transaction;
import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class CoinsTransactionEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData  playerData;
    private final Transaction transaction;

    public CoinsTransactionEvent(PlayerData playerData, Transaction transaction) {
        this.playerData  = playerData;
        this.transaction = transaction;
    }

    public PlayerData  getPlayerData()  { return playerData; }
    public Transaction getTransaction() { return transaction; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}