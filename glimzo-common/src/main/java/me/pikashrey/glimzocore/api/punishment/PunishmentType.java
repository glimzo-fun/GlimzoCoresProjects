package me.pikashrey.glimzocore.api.punishment;

public enum PunishmentType {

    BAN("Ban",           true),
    TEMP_BAN("Temp Ban", true),
    MUTE("Mute",         false),
    TEMP_MUTE("Temp Mute", false),
    WARN("Warning",      false),
    KICK("Kick",         false);

    private final String  displayName;
    private final boolean removesFromServer;

    PunishmentType(String displayName, boolean removesFromServer) {
        this.displayName       = displayName;
        this.removesFromServer = removesFromServer;
    }

    public String  getDisplayName()      { return displayName; }
    public boolean removesFromServer()   { return removesFromServer; }
    public boolean isBan()  { return this == BAN  || this == TEMP_BAN; }
    public boolean isMute() { return this == MUTE || this == TEMP_MUTE; }
    public boolean isTemp() { return this == TEMP_BAN || this == TEMP_MUTE; }

    @Override
    public String toString() { return displayName; }
}