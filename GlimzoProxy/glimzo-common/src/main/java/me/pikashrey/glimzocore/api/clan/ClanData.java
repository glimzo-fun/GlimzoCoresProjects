package me.pikashrey.glimzocore.api.clan;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ClanData {

    private final String      id;           // UUID string - primary key
    private       String      name;
    private       String      tag;          // short tag e.g. "NOW" - shown in clan chat
    private       String      description;
    private       String      motd;         // message of the day
    private       UUID        leaderUuid;
    private       String      leaderName;
    private       int         level;        // 1-10
    private       long        xp;           // total clan XP toward next level
    private       long        createdAt;

    // CopyOnWriteArrayList: addMember called async (loadForPlayer), removeMember on main thread
    private final List<ClanMember> members = new CopyOnWriteArrayList<>();

    public ClanData(String id, String name, String tag, String description, String motd,
                    UUID leaderUuid, String leaderName, int level, long xp, long createdAt) {
        this.id          = id;
        this.name        = name;
        this.tag         = tag;
        this.description = description;
        this.motd        = motd;
        this.leaderUuid  = leaderUuid;
        this.leaderName  = leaderName;
        this.level       = Math.max(1, Math.min(level, ClanLevel.maxLevel()));
        this.xp          = Math.max(0, xp);
        this.createdAt   = createdAt;
    }

    // --- Level helpers ---

    public ClanLevel getClanLevel() {
        return ClanLevel.forLevel(level);
    }

    /** XP needed to reach the next level. Returns -1 if already max level. */
    public long getXpToNextLevel() {
        if (level >= ClanLevel.maxLevel()) return -1;
        long needed = ClanLevel.forLevel(level + 1).getXpRequired() - xp;
        return Math.max(0, needed); // clamp - xp can overshoot between ticks
    }

    public boolean isMaxLevel() {
        return level >= ClanLevel.maxLevel();
    }

    /** Add XP and return the new level (may have leveled up). */
    public int addXp(long amount) {
        this.xp += amount;
        int newLevel = level;
        while (newLevel < ClanLevel.maxLevel()) {
            long required = ClanLevel.forLevel(newLevel + 1).getXpRequired();
            if (this.xp >= required) {
                newLevel++;
            } else {
                break;
            }
        }
        this.level = newLevel;
        return newLevel;
    }

    // --- Member helpers ---

    public List<ClanMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(ClanMember member) {
        members.add(member);
    }

    public void removeMember(UUID uuid) {
        members.removeIf(m -> m.getUuid().equals(uuid));
    }

    public ClanMember getMember(UUID uuid) {
        for (ClanMember m : members) {
            if (m.getUuid().equals(uuid)) return m;
        }
        return null;
    }

    public boolean isMember(UUID uuid) {
        return getMember(uuid) != null;
    }

    /** Whether the given UUID is the current leader of this clan. */
    public boolean isLeader(UUID uuid) {
        return leaderUuid != null && leaderUuid.equals(uuid);
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getBaseCapacity(int leaderRankMaxClanSize) {
        return leaderRankMaxClanSize;
    }

    // --- Getters / Setters ---

    public String getId()          { return id; }

    public String getName()        { return name; }
    public void   setName(String n){ this.name = n; }

    public String getTag()         { return tag; }
    public void   setTag(String t) { this.tag = t; }

    public String getDescription()           { return description; }
    public void   setDescription(String d)   { this.description = d; }

    public String getMotd()                  { return motd; }
    public void   setMotd(String m)          { this.motd = m; }

    public UUID   getLeaderUuid()            { return leaderUuid; }
    public void   setLeaderUuid(UUID u)      { this.leaderUuid = u; }

    public String getLeaderName()            { return leaderName; }
    public void   setLeaderName(String n)    { this.leaderName = n; }

    public int    getLevel()                 { return level; }
    public void   setLevel(int l)            { this.level = Math.max(1, Math.min(l, ClanLevel.maxLevel())); }

    public long   getXp()                    { return xp; }
    public void   setXp(long x)             { this.xp = Math.max(0, x); }

    public long   getCreatedAt()             { return createdAt; }
    public void   setCreatedAt(long t)       { this.createdAt = t; }

    @Override
    public String toString() { return "[" + tag + "] " + name + " (Lv." + level + ")"; }
}

