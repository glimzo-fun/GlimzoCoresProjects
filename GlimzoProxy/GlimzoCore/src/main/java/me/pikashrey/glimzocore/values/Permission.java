package me.pikashrey.glimzocore.values;

public final class Permission {

    private Permission() {}

    // --- Rank nodes ---
    public static final String RANK_CHIEF      = "glimzo.rank.chief";
    public static final String RANK_ADMIN      = "glimzo.rank.admin";
    public static final String RANK_ARCHITECT  = "glimzo.rank.architect";
    public static final String RANK_PIBBLE     = "glimzo.rank.pibble";
    public static final String RANK_GUARDIAN   = "glimzo.rank.guardian";
    public static final String RANK_GUIDE      = "glimzo.rank.guide";
    public static final String RANK_COSMOS     = "glimzo.rank.cosmos";
    public static final String RANK_ASCENDANT  = "glimzo.rank.ascendant";
    public static final String RANK_LEGENDARY  = "glimzo.rank.legendary";
    public static final String RANK_MYTHIC     = "glimzo.rank.mythic";
    public static final String RANK_WARDEN     = "glimzo.rank.warden";
    public static final String RANK_BARON      = "glimzo.rank.baron";

    // --- Rank management (staff) ---
    public static final String RANK_GRANT      = "glimzo.rank.grant";
    public static final String RANK_REVOKE     = "glimzo.rank.revoke";
    public static final String RANK_HISTORY    = "glimzo.rank.history";
    public static final String RANK_SETPREFIX  = "glimzo.rank.setprefix";
    public static final String RANK_GIFT       = "glimzo.rank.gift";       // gift a donor rank

    // --- Clan ---
    public static final String CLAN_CREATE     = "glimzo.clan.create";
    public static final String CLAN_ADMIN      = "glimzo.clan.admin";      // bypass rank requirement, disband others, etc.
    public static final String CLAN_CHAT       = "glimzo.clan.chat";

    // --- Staff tools ---
    public static final String STAFF_ALERT     = "glimzo.staff.alert";
    public static final String STAFF_BROADCAST = "glimzo.staff.broadcast";
    public static final String STAFF_CHAT      = "glimzo.staff.chat";
    public static final String STAFF_FREEZE    = "glimzo.staff.freeze";
    public static final String STAFF_INVSEE    = "glimzo.staff.invsee";
    public static final String STAFF_VANISH    = "glimzo.staff.vanish";
    public static final String STAFF_MODE      = "glimzo.staff.mode";

    // --- Punishments ---
    public static final String PUNISH_BAN      = "glimzo.punish.ban";
    public static final String PUNISH_MUTE     = "glimzo.punish.mute";
    public static final String PUNISH_KICK     = "glimzo.punish.kick";
    public static final String PUNISH_WARN     = "glimzo.punish.warn";
    public static final String PUNISH_HISTORY  = "glimzo.punish.history";
    public static final String PUNISH_CHECK    = "glimzo.punish.check";

    // --- Economy ---
    public static final String COINS_ADMIN     = "glimzo.coins.admin";
    public static final String GEMS_ADMIN      = "glimzo.gems.admin";

    // --- General ---
    public static final String FLY             = "glimzo.fly";
    public static final String NICK            = "glimzo.nick";
    public static final String NICK_COLOR      = "glimzo.nick.color";
    public static final String BYPASS_FILTER   = "glimzo.bypass.filter";
    public static final String BYPASS_COOLDOWN = "glimzo.bypass.cooldown";
}

