package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class ClanXpGainEvent extends GlimzoEvent {

    public enum Source {
        MEMBER_ACTIVITY,
        QUEST_COMPLETION,
        GAME_WIN,
        ADMIN_GRANT
    }

    private static final HandlerList HANDLERS = new HandlerList();

    private final ClanData clanData;
    private final UUID     triggeredByUuid; // member whose action triggered the gain; null for admin
    private final Source   source;
    private       long     amount;          // mutable - listeners can adjust the value

    public ClanXpGainEvent(ClanData clanData, UUID triggeredByUuid, Source source, long amount) {
        this.clanData        = clanData;
        this.triggeredByUuid = triggeredByUuid;
        this.source          = source;
        this.amount          = amount;
    }

    public ClanData getClanData()        { return clanData; }
    public UUID     getTriggeredByUuid() { return triggeredByUuid; }
    public Source   getSource()          { return source; }
    public long     getAmount()          { return amount; }
    public void     setAmount(long a)    { this.amount = Math.max(0, a); }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}
