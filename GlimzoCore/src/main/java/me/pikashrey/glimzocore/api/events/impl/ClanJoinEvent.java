package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class ClanJoinEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData playerData;
    private final ClanData   clanData;

    public ClanJoinEvent(PlayerData playerData, ClanData clanData) {
        this.playerData = playerData;
        this.clanData   = clanData;
    }

    public PlayerData getPlayerData() { return playerData; }
    public ClanData   getClanData()   { return clanData; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}
