package me.pikashrey.glimzocore.api.economy;

import java.util.UUID;

public class Transaction {

    public enum Type {
        ADD,
        REMOVE
    }

    private final UUID     playerUuid;
    private final Currency currency;
    private final Type     type;
    private final long     amount;
    private final String   reason;
    private final long     timestamp;

    public Transaction(UUID playerUuid, Currency currency, Type type, long amount, String reason) {
        this.playerUuid = playerUuid;
        this.currency   = currency;
        this.type       = type;
        this.amount     = amount;
        this.reason     = reason;
        this.timestamp  = System.currentTimeMillis();
    }

    public UUID     getPlayerUuid() { return playerUuid; }
    public Currency getCurrency()   { return currency; }
    public Type     getType()       { return type; }
    public long     getAmount()     { return amount; }
    public String   getReason()     { return reason; }
    public long     getTimestamp()  { return timestamp; }

    public boolean isAdd()    { return type == Type.ADD; }
    public boolean isRemove() { return type == Type.REMOVE; }
}