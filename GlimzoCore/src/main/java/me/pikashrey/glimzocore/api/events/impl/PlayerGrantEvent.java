package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class PlayerGrantEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData playerData;
    private final String     rankId;
    private final String     grantedBy;
    private final long       expiresAt;  // -1 = permanent

    public PlayerGrantEvent(PlayerData playerData, String rankId, String grantedBy, long expiresAt) {
        this.playerData = playerData;
        this.rankId     = rankId;
        this.grantedBy  = grantedBy;
        this.expiresAt  = expiresAt;
    }

    public PlayerData getPlayerData() { return playerData; }
    public String     getRankId()     { return rankId; }
    public String     getGrantedBy()  { return grantedBy; }
    public long       getExpiresAt()  { return expiresAt; }
    public boolean    isPermanent()   { return expiresAt == -1; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}