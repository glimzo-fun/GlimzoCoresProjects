package me.pikashrey.glimzocore.features.social;

import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public class SocialSyncSubscriber extends JedisPubSub {

    private final Consumer<String> messageHandler;

    public SocialSyncSubscriber(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override public void onMessage(String channel, String message) { messageHandler.accept(message); }
    @Override public void onSubscribe(String channel, int count) {}
    @Override public void onUnsubscribe(String channel, int count) {}
    @Override public void onPMessage(String p, String c, String m) {}
    @Override public void onPSubscribe(String p, int c) {}
    @Override public void onPUnsubscribe(String p, int c) {}
}
