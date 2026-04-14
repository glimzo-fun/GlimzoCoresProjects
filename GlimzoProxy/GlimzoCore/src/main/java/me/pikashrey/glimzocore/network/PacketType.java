package me.pikashrey.glimzocore.network;

public enum PacketType {

    CONNECT       ("Connect"),
    CONNECT_OTHER ("ConnectOther"),
    KICK_PLAYER   ("KickPlayer"),
    GET_SERVER    ("GetServer"),
    GET_SERVERS   ("GetServers"),
    PLAYER_COUNT  ("PlayerCount"),
    PLAYER_LIST   ("PlayerList"),
    MESSAGE_RAW   ("MessageRaw");

    private final String channel;

    PacketType(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}