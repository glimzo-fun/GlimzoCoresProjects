package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class PlayerPrestigeEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData playerData;
    private final int        oldPrestige;
    private final int        newPrestige;

    public PlayerPrestigeEvent(PlayerData playerData, int oldPrestige, int newPrestige) {
        this.playerData  = playerData;
        this.oldPrestige = oldPrestige;
        this.newPrestige = newPrestige;
    }

    public PlayerData getPlayerData()  { return playerData; }
    public int        getOldPrestige() { return oldPrestige; }
    public int        getNewPrestige() { return newPrestige; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}