package me.pikashrey.glimzocore.api.player;

import java.util.UUID;

public class PlayerData {

    // Identity
    private final UUID   uuid;
    private final String name;        // real username, never changes
    private String       nick;          // null = no nick active
    private String       nickRank;      // null = use real rank; set when Cosmos+ spoofs a rank
    private String       displayName;   // computed: nick ?? name
    private String       customPrefix;  // null = use rank prefix; set by /setprefix
    // Computed cache - invalidated by rank grant/revoke and /setprefix
    // Volatile: read from AsyncPlayerChatEvent, written from main thread
    private volatile String cachedChatPrefix;

    // Dirty flag - set whenever persistent data changes, cleared after a save.
    // AutoSaveTask uses this to skip unchanged players and reduce DB writes.
    private volatile boolean dirty = false;

    // Economy
    private long coins;
    private long gems;

    // Leveling
    private int level;
    private int prestige;
    private long experience;          // XP progress toward next level

    // Season
    private int  seasonRank;          // rank index within current season
    private long seasonXp;            // total XP earned this season
    private boolean seasonPassActive; // has paid season pass

    // Punishment state
    private boolean banned;
    private boolean muted;
    private long    banExpiry;        // epoch ms, -1 = permanent
    private long    muteExpiry;       // epoch ms, -1 = permanent
    private String  banReason;
    private String  muteReason;

    // Rank
    private String rankId;            // null = default rank

    //  Social
    private String clanId;            // null = not in a clan
    private int    clanRole;          // 0=member, 1=officer, 2=leader

    // Settings (from PlayerSettings)
    private final PlayerSettings settings;

    // Stats (from PlayerStats)
    private final PlayerStats stats;

    // Staff
    private boolean staffMode;
    private boolean vanished;
    private boolean socialSpyEnabled;
    private boolean flyEnabled;         // persisted: was /fly active when logged off?

    // Misc
    private boolean firstJoin;
    private long    firstJoinTime;
    private long    lastSeenTime;
    private int     totalPlaytimeMinutes;

    //  Constructor - used when loading from DB

    public PlayerData(UUID uuid, String name) {
        this.uuid        = uuid;
        this.name        = name;
        this.displayName = name;
        this.settings    = new PlayerSettings();
        this.stats       = new PlayerStats();
        // sensible defaults for a brand new player
        this.coins       = 0;
        this.gems        = 0;
        this.level       = 1;
        this.prestige    = 0;
        this.experience  = 0;
        this.seasonRank  = 0;
        this.seasonXp    = 0;
        this.banExpiry   = -1;
        this.muteExpiry  = -1;
        this.firstJoin   = true;
    }

    //  Convenience helpers

    /** Returns the nick if active, otherwise the real username. */
    public String getDisplayName() {
        return (nick != null && !nick.isEmpty()) ? nick : name;
    }

    public boolean hasNick() {
        return nick != null && !nick.isEmpty();
    }

    public boolean isBanned() {
        if (!banned) return false;
        if (banExpiry == -1) return true;
        return System.currentTimeMillis() < banExpiry;
    }

    public boolean isMuted() {
        if (!muted) return false;
        if (muteExpiry == -1) return true;
        return System.currentTimeMillis() < muteExpiry;
    }

    public boolean isInClan() {
        return clanId != null && !clanId.isEmpty();
    }

    //  Getters & Setters

    public UUID   getUuid()          { return uuid; }
    public String getName()          { return name; }

    public String getNick()          { return nick; }
    public void   setNick(String n)  { this.nick = n; dirty = true; }

    public String getNickRank()              { return nickRank; }
    public void   setNickRank(String r)      { this.nickRank = r; dirty = true; }
    public boolean hasNickRank()             { return nickRank != null && !nickRank.isEmpty(); }

    public String  getCustomPrefix()           { return customPrefix; }
    public void    setCustomPrefix(String p)   { this.customPrefix = p; invalidateChatPrefix(); dirty = true; }
    public boolean hasCustomPrefix()           { return customPrefix != null && !customPrefix.isEmpty(); }

    /** Returns the cached resolved chat prefix, or null if the cache is stale (needs recompute). */
    public String  getCachedChatPrefix()       { return cachedChatPrefix; }
    /** Store a freshly computed prefix string. */
    public void    setCachedChatPrefix(String p){ this.cachedChatPrefix = p; }
    /** Invalidate the cache so the next getChatPrefix() call recomputes it. */
    public void    invalidateChatPrefix()      { this.cachedChatPrefix = null; }

    public boolean isDirty()    { return dirty; }
    public void    markDirty()  { this.dirty = true; }
    public void    clearDirty() { this.dirty = false; }

    public long getCoins()           { return coins; }
    public void setCoins(long c)     { this.coins = Math.max(0, c); dirty = true; }
    public void addCoins(long c)     { this.coins = Math.max(0, this.coins + c); dirty = true; }
    public void removeCoins(long c)  { this.coins = Math.max(0, this.coins - c); dirty = true; }

    public long getGems()            { return gems; }
    public void setGems(long g)      { this.gems = Math.max(0, g); dirty = true; }
    public void addGems(long g)      { this.gems = Math.max(0, this.gems + g); dirty = true; }
    public void removeGems(long g)   { this.gems = Math.max(0, this.gems - g); dirty = true; }

    public int  getLevel()           { return level; }
    public void setLevel(int l)      { this.level = Math.max(1, l); dirty = true; }

    public int  getPrestige()        { return prestige; }
    public void setPrestige(int p)   { this.prestige = Math.max(0, p); dirty = true; }

    public long getExperience()      { return experience; }
    public void setExperience(long e){ this.experience = Math.max(0, e); dirty = true; }
    public void addExperience(long e){ this.experience = Math.max(0, this.experience + e); dirty = true; }

    public int  getSeasonRank()      { return seasonRank; }
    public void setSeasonRank(int r) { this.seasonRank = r; dirty = true; }

    public long getSeasonXp()          { return seasonXp; }
    public void setSeasonXp(long x)    { this.seasonXp = Math.max(0, x); dirty = true; }
    public void addSeasonXp(long x)    { this.seasonXp += x; dirty = true; }

    public boolean hasSeasonPass()           { return seasonPassActive; }
    public void setSeasonPassActive(boolean b){ this.seasonPassActive = b; dirty = true; }

    public void   setBanned(boolean b)       { this.banned = b; dirty = true; }
    public long   getBanExpiry()             { return banExpiry; }
    public void   setBanExpiry(long e)       { this.banExpiry = e; dirty = true; }
    public String getBanReason()             { return banReason; }
    public void   setBanReason(String r)     { this.banReason = r; dirty = true; }

    public void   setMuted(boolean b)        { this.muted = b; dirty = true; }
    public long   getMuteExpiry()            { return muteExpiry; }
    public void   setMuteExpiry(long e)      { this.muteExpiry = e; dirty = true; }
    public String getMuteReason()            { return muteReason; }
    public void   setMuteReason(String r)    { this.muteReason = r; dirty = true; }

    public String getRankId()                { return rankId; }
    public void   setRankId(String id)       { this.rankId = id; dirty = true; }

    public String getClanId()                { return clanId; }
    public void   setClanId(String id)       { this.clanId = id; dirty = true; }
    public int    getClanRole()              { return clanRole; }
    public void   setClanRole(int r)         { this.clanRole = r; dirty = true; }

    public PlayerSettings getSettings()      { return settings; }
    public PlayerStats    getStats()         { return stats; }

    public boolean isStaffMode()             { return staffMode; }
    public void    setStaffMode(boolean b)       { this.staffMode = b; dirty = true; }

    public boolean isVanished()              { return vanished; }
    public void    setVanished(boolean b)        { this.vanished = b; dirty = true; }

    public boolean isSocialSpyEnabled()          { return socialSpyEnabled; }
    public void    setSocialSpyEnabled(boolean b){ this.socialSpyEnabled = b; dirty = true; }

    public boolean isFlyEnabled()                { return flyEnabled; }
    public void    setFlyEnabled(boolean b)      { this.flyEnabled = b; dirty = true; }

    public boolean isFirstJoin()             { return firstJoin; }
    public void    setFirstJoin(boolean b)   { this.firstJoin = b; }

    public long getFirstJoinTime()           { return firstJoinTime; }
    public void setFirstJoinTime(long t)     { this.firstJoinTime = t; }

    public long getLastSeenTime()            { return lastSeenTime; }
    public void setLastSeenTime(long t)      { this.lastSeenTime = t; }

    public int  getTotalPlaytimeMinutes()             { return totalPlaytimeMinutes; }
    public void setTotalPlaytimeMinutes(int m)        { this.totalPlaytimeMinutes = m; dirty = true; }
    public void addPlaytimeMinutes(int m)             { this.totalPlaytimeMinutes += m; dirty = true; }
}