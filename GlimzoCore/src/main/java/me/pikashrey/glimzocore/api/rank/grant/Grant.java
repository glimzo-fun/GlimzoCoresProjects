package me.pikashrey.glimzocore.api.rank.grant;

import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.api.rank.RankRef;

import java.util.UUID;

public class Grant {

    private final int     id;
    private final UUID    targetUuid;
    private final String  targetName;
    private final UUID    issuedByUuid;
    private final String  issuedByName;
    private final RankRef rankRef;
    private final long    issuedAt;
    private final long    expiresAt;
    private final boolean staffGrant;
    private       boolean active;

    public Grant(int id, UUID targetUuid, String targetName,
                 UUID issuedByUuid, String issuedByName,
                 RankRef rankRef, long issuedAt, long expiresAt,
                 boolean staffGrant, boolean active) {
        this.id           = id;
        this.targetUuid   = targetUuid;
        this.targetName   = targetName;
        this.issuedByUuid = issuedByUuid;
        this.issuedByName = issuedByName;
        this.rankRef      = rankRef;
        this.issuedAt     = issuedAt;
        this.expiresAt    = expiresAt;
        this.staffGrant   = staffGrant;
        this.active       = active;
    }

    public Grant(int id, UUID targetUuid, String targetName,
                 UUID issuedByUuid, String issuedByName,
                 Rank rank, long issuedAt, long expiresAt,
                 boolean staffGrant, boolean active) {
        this(id, targetUuid, targetName, issuedByUuid, issuedByName,
             RankRef.of(rank), issuedAt, expiresAt, staffGrant, active);
    }

    public static Grant create(UUID targetUuid, String targetName,
                               UUID issuedByUuid, String issuedByName,
                               RankRef rankRef, long expiresAt, boolean staffGrant) {
        return new Grant(-1, targetUuid, targetName, issuedByUuid, issuedByName,
                         rankRef, System.currentTimeMillis(), expiresAt, staffGrant, true);
    }

    public static Grant create(UUID targetUuid, String targetName,
                               UUID issuedByUuid, String issuedByName,
                               Rank rank, long expiresAt, boolean staffGrant) {
        return create(targetUuid, targetName, issuedByUuid, issuedByName, RankRef.of(rank), expiresAt, staffGrant);
    }

    public boolean isActive() {
        if (!active) return false;
        if (expiresAt == -1) return true;
        return System.currentTimeMillis() < expiresAt;
    }

    public boolean hasJustExpired() {
        if (!active || expiresAt == -1) return false;
        if (System.currentTimeMillis() >= expiresAt) { active = false; return true; }
        return false;
    }

    public boolean isPermanent()  { return expiresAt == -1; }

    public long getRemainingMs() {
        if (expiresAt == -1) return -1;
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public int getEffectiveWeight() {
        return rankRef.getWeight() + (staffGrant ? 50 : 0);
    }

    public void setActive(boolean active) { this.active = active; }

    public int     getId()           { return id; }
    public UUID    getTargetUuid()   { return targetUuid; }
    public String  getTargetName()   { return targetName; }
    public UUID    getIssuedByUuid() { return issuedByUuid; }
    public String  getIssuedByName() { return issuedByName; }
    public RankRef getRankRef()      { return rankRef; }
    public long    getIssuedAt()     { return issuedAt; }
    public long    getExpiresAt()    { return expiresAt; }
    public boolean isStaffGrant()    { return staffGrant; }

    public Rank getRank() { return rankRef.toEnum(); }

    @Override
    public String toString() {
        return rankRef.getDisplayName() + " (by " + issuedByName + ", "
               + (isPermanent() ? "permanent" : "temp") + ")";
    }
}
