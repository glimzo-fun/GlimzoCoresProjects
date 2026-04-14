package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class PlayerLevelUpEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData playerData;
    private final int        oldLevel;
    private final int        newLevel;

    public PlayerLevelUpEvent(PlayerData playerData, int oldLevel, int newLevel) {
        this.playerData = playerData;
        this.oldLevel   = oldLevel;
        this.newLevel   = newLevel;
    }

    public PlayerData getPlayerData() { return playerData; }
    public int        getOldLevel()   { return oldLevel; }
    public int        getNewLevel()   { return newLevel; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}