package me.pikashrey.glimzocore.api.clan;

import java.util.UUID;

public class ClanMember {

    private final UUID     uuid;
    private final String   name;
    private       ClanRole role;
    private final long     joinedAt;  // epoch ms

    public ClanMember(UUID uuid, String name, ClanRole role, long joinedAt) {
        this.uuid     = uuid;
        this.name     = name;
        this.role     = role;
        this.joinedAt = joinedAt;
    }

    public UUID     getUuid()     { return uuid; }
    public String   getName()     { return name; }
    public ClanRole getRole()     { return role; }
    public void     setRole(ClanRole r) { this.role = r; }
    public long     getJoinedAt() { return joinedAt; }

    public boolean isLeader()  { return role == ClanRole.LEADER; }
    public boolean isOfficer() { return role.isOfficer(); }

    @Override
    public String toString() { return name + " (" + role.getDisplayName() + ")"; }
}
