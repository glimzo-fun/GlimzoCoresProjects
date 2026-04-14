package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.clan.ClanData;
import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player leaves a clan voluntarily or is kicked.
 */
public class ClanLeaveEvent extends GlimzoEvent {

    public enum Reason { LEFT, KICKED, CLAN_DISBANDED }

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData playerData;
    private final ClanData   clanData;
    private final Reason     reason;

    public ClanLeaveEvent(PlayerData playerData, ClanData clanData, Reason reason) {
        this.playerData = playerData;
        this.clanData   = clanData;
        this.reason     = reason;
    }

    public PlayerData getPlayerData() { return playerData; }
    public ClanData   getClanData()   { return clanData; }
    public Reason     getReason()     { return reason; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}
