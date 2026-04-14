package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Fired when a clan is disbanded - either by the leader voluntarily or by an admin.
 */
public class ClanDisbandEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ClanData clanData;
    private final UUID     disbandedByUuid;
    private final String   disbandedByName;
    private final boolean  byAdmin;

    public ClanDisbandEvent(ClanData clanData, UUID disbandedByUuid,
                             String disbandedByName, boolean byAdmin) {
        this.clanData        = clanData;
        this.disbandedByUuid = disbandedByUuid;
        this.disbandedByName = disbandedByName;
        this.byAdmin         = byAdmin;
    }

    public ClanData getClanData()        { return clanData; }
    public UUID     getDisbandedByUuid() { return disbandedByUuid; }
    public String   getDisbandedByName() { return disbandedByName; }
    public boolean  wasByAdmin()         { return byAdmin; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}
