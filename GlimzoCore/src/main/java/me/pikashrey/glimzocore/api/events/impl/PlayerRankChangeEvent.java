package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.api.rank.grant.Grant;
import org.bukkit.event.HandlerList;

public class PlayerRankChangeEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerData playerData;
    private final RankRef    oldRankRef;
    private final RankRef    newRankRef;
    private final Grant      grant;

    public PlayerRankChangeEvent(PlayerData playerData, RankRef oldRankRef, RankRef newRankRef, Grant grant) {
        this.playerData  = playerData;
        this.oldRankRef  = oldRankRef  != null ? oldRankRef  : RankRef.of(Rank.getDefault());
        this.newRankRef  = newRankRef  != null ? newRankRef  : RankRef.of(Rank.getDefault());
        this.grant       = grant;
    }

    /** Legacy constructor accepting Rank enum values. */
    public PlayerRankChangeEvent(PlayerData playerData, Rank oldRank, Rank newRank, Grant grant) {
        this(playerData,
             oldRank != null ? RankRef.of(oldRank) : RankRef.of(Rank.getDefault()),
             newRank != null ? RankRef.of(newRank) : RankRef.of(Rank.getDefault()),
             grant);
    }

    public PlayerData getPlayerData() { return playerData; }
    public RankRef    getOldRankRef() { return oldRankRef; }
    public RankRef    getNewRankRef() { return newRankRef; }
    public Grant      getGrant()      { return grant; }

    public Rank getOldRank() { return oldRankRef.toEnum(); }

    public Rank getNewRank() { return newRankRef.toEnum(); }

    /** Whether this event represents a rank upgrade (weight increase). */
    public boolean isUpgrade() {
        return newRankRef.getWeight() > oldRankRef.getWeight();
    }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}
