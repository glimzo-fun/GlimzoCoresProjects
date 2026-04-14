package me.pikashrey.glimzocore.features.rank;

import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public class RankSyncSubscriber extends JedisPubSub {

    private final Consumer<String> messageHandler;

    public RankSyncSubscriber(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void onMessage(String channel, String message) {
        messageHandler.accept(message);
    }

    @Override public void onSubscribe(String channel, int subscribedChannels) {}
    @Override public void onUnsubscribe(String channel, int subscribedChannels) {}
    @Override public void onPMessage(String pattern, String channel, String message) {}
    @Override public void onPSubscribe(String pattern, int subscribedChannels) {}
    @Override public void onPUnsubscribe(String pattern, int subscribedChannels) {}
}
