package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class PlayerSeasonRankUpEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData playerData;
    private final int        oldRank;
    private final int        newRank;

    public PlayerSeasonRankUpEvent(PlayerData playerData, int oldRank, int newRank) {
        this.playerData = playerData;
        this.oldRank    = oldRank;
        this.newRank    = newRank;
    }

    public PlayerData getPlayerData() { return playerData; }
    public int        getOldRank()    { return oldRank; }
    public int        getNewRank()    { return newRank; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}