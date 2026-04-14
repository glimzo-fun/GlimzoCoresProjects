package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import org.bukkit.event.HandlerList;

public class ClanLevelUpEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String clanId;
    private final int    oldLevel;
    private final int    newLevel;

    public ClanLevelUpEvent(String clanId, int oldLevel, int newLevel) {
        this.clanId   = clanId;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public String getClanId()   { return clanId; }
    public int    getOldLevel() { return oldLevel; }
    public int    getNewLevel() { return newLevel; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}