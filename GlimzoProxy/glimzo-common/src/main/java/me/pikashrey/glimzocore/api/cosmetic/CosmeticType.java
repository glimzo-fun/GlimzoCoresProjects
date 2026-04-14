package me.pikashrey.glimzocore.api.cosmetic;

public enum CosmeticType {

    AURA("Aura Effects"),
    WINGS("Animated Wings"),
    ALLY("Mythical Allies"),
    MORPH("Morph"),
    CHAT_TAG("Chat Tags"),
    JOIN_EFFECT("Join Effects");

    private final String displayName;

    CosmeticType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}