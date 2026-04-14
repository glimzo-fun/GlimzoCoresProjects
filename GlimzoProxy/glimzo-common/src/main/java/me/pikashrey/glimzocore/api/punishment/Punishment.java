package me.pikashrey.glimzocore.api.punishment;

import java.util.UUID;

public class Punishment {

    private final int            id;
    private final UUID           targetUuid;
    private final String         targetName;
    private final UUID           staffUuid;
    private final String         staffName;
    private final PunishmentType type;
    private final String         reason;
    private final long           issuedAt;
    private final long           expiresAt;  // -1 = permanent
    private       boolean        active;
    private       boolean        appealed;

    public Punishment(int id, UUID targetUuid, String targetName,
                      UUID staffUuid, String staffName,
                      PunishmentType type, String reason,
                      long issuedAt, long expiresAt,
                      boolean active, boolean appealed) {
        this.id         = id;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffUuid  = staffUuid;
        this.staffName  = staffName;
        this.type       = type;
        this.reason     = reason;
        this.issuedAt   = issuedAt;
        this.expiresAt  = expiresAt;
        this.active     = active;
        this.appealed   = appealed;
    }

    public boolean isActive() {
        if (!active) return false;
        if (expiresAt == -1) return true;
        if (System.currentTimeMillis() >= expiresAt) {
            active = false;
            return false;
        }
        return true;
    }
    public static Punishment create(UUID targetUuid, String targetName,
                                    UUID staffUuid, String staffName,
                                    PunishmentType type, String reason,
                                    long expiresAt) {
        return new Punishment(-1, targetUuid, targetName, staffUuid, staffName,
                type, reason, System.currentTimeMillis(), expiresAt, true, false);
    }
    public boolean isPermanent()  { return expiresAt == -1; }

    public long getRemainingMs() {
        if (expiresAt == -1) return -1;
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public void setActive(boolean active)     { this.active = active; }
    public void setAppealed(boolean appealed) { this.appealed = appealed; }

    public int            getId()         { return id; }
    public UUID           getTargetUuid() { return targetUuid; }
    public String         getTargetName() { return targetName; }
    public UUID           getStaffUuid()  { return staffUuid; }
    public String         getStaffName()  { return staffName; }
    public PunishmentType getType()       { return type; }
    public String         getReason()     { return reason; }
    public long           getIssuedAt()   { return issuedAt; }
    public long           getExpiresAt()  { return expiresAt; }
    public boolean        isAppealed()    { return appealed; }
}