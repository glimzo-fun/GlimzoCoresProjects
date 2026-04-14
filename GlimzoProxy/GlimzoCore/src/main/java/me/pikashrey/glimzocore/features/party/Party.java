package me.pikashrey.glimzocore.features.party;

import java.util.*;

public class Party {

    // Default max size - overridden at runtime by PartyManager using social.yml
    public static final int DEFAULT_MAX_SIZE = 16;

    private final UUID         leaderUuid;
    private final String       leaderName;
    private final Set<UUID>    members    = new LinkedHashSet<>();  // preserves insertion order
    private final long         createdAt;

    public Party(UUID leaderUuid, String leaderName) {
        this.leaderUuid = leaderUuid;
        this.leaderName = leaderName;
        this.createdAt  = System.currentTimeMillis();
        members.add(leaderUuid);
    }

    public void addMember(UUID uuid)    { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
    public boolean isMember(UUID uuid)  { return members.contains(uuid); }
    public boolean isLeader(UUID uuid)  { return leaderUuid.equals(uuid); }
    public boolean isFull()             { return members.size() >= DEFAULT_MAX_SIZE; }
    public boolean isFull(int maxSize)  { return members.size() >= maxSize; }

    public UUID      getLeaderUuid() { return leaderUuid; }
    public String    getLeaderName() { return leaderName; }
    public Set<UUID> getMembers()    { return Collections.unmodifiableSet(members); }
    public int       getSize()       { return members.size(); }
    public long      getCreatedAt()  { return createdAt; }
}

