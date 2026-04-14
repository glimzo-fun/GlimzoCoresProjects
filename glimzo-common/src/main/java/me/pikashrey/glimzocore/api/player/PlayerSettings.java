package me.pikashrey.glimzocore.api.player;

public class PlayerSettings {

    // Chat & messaging
    private boolean privateMessagesEnabled  = true;  // receive /msg from others
    private boolean friendRequestsEnabled   = true;  // receive friend requests
    private boolean partyInvitesEnabled     = true;  // receive party invites
    private boolean clanInvitesEnabled      = true;  // receive clan invites

    // Visuals
    private boolean scoreboardEnabled       = true;  // sidebar scoreboard
    private boolean bossbarEnabled          = true;  // bossbar display
    private boolean playerVisibilityEnabled = true;  // see other players
    private boolean joinMessagesEnabled     = true;  // see join/quit messages

    // Cosmetics
    private boolean cosmeticsEnabled        = true;  // show own cosmetics
    private boolean otherCosmeticsEnabled   = true;  // see other players' cosmetics

    // Notifications
    private boolean achievementAlertsEnabled  = true;
    private boolean levelUpAlertsEnabled      = true;
    private boolean seasonRankUpAlertsEnabled = true;

    // Language
    private String language = "en";

    //  Getters & Setters

    public boolean isPrivateMessagesEnabled()              { return privateMessagesEnabled; }
    public void    setPrivateMessagesEnabled(boolean b)    { this.privateMessagesEnabled = b; }

    public boolean isFriendRequestsEnabled()               { return friendRequestsEnabled; }
    public void    setFriendRequestsEnabled(boolean b)     { this.friendRequestsEnabled = b; }

    public boolean isPartyInvitesEnabled()                 { return partyInvitesEnabled; }
    public void    setPartyInvitesEnabled(boolean b)       { this.partyInvitesEnabled = b; }

    public boolean isClanInvitesEnabled()                  { return clanInvitesEnabled; }
    public void    setClanInvitesEnabled(boolean b)        { this.clanInvitesEnabled = b; }

    public boolean isScoreboardEnabled()                   { return scoreboardEnabled; }
    public void    setScoreboardEnabled(boolean b)         { this.scoreboardEnabled = b; }

    public boolean isBossbarEnabled()                      { return bossbarEnabled; }
    public void    setBossbarEnabled(boolean b)            { this.bossbarEnabled = b; }

    public boolean isPlayerVisibilityEnabled()             { return playerVisibilityEnabled; }
    public void    setPlayerVisibilityEnabled(boolean b)   { this.playerVisibilityEnabled = b; }

    public boolean isJoinMessagesEnabled()                 { return joinMessagesEnabled; }
    public void    setJoinMessagesEnabled(boolean b)       { this.joinMessagesEnabled = b; }

    public boolean isCosmeticsEnabled()                    { return cosmeticsEnabled; }
    public void    setCosmeticsEnabled(boolean b)          { this.cosmeticsEnabled = b; }

    public boolean isOtherCosmeticsEnabled()               { return otherCosmeticsEnabled; }
    public void    setOtherCosmeticsEnabled(boolean b)     { this.otherCosmeticsEnabled = b; }

    public boolean isAchievementAlertsEnabled()            { return achievementAlertsEnabled; }
    public void    setAchievementAlertsEnabled(boolean b)  { this.achievementAlertsEnabled = b; }

    public boolean isLevelUpAlertsEnabled()                { return levelUpAlertsEnabled; }
    public void    setLevelUpAlertsEnabled(boolean b)      { this.levelUpAlertsEnabled = b; }

    public boolean isSeasonRankUpAlertsEnabled()           { return seasonRankUpAlertsEnabled; }
    public void    setSeasonRankUpAlertsEnabled(boolean b) { this.seasonRankUpAlertsEnabled = b; }

    public String  getLanguage()                           { return language; }
    public void    setLanguage(String lang)                { this.language = lang; }
}