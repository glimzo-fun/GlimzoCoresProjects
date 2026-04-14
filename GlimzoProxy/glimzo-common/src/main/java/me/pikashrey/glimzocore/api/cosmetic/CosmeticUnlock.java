package me.pikashrey.glimzocore.api.cosmetic;

import java.util.UUID;

public class CosmeticUnlock {

    private final UUID   playerUuid;
    private final String cosmeticId;
    private final long   unlockedAt;
    private final String source;     // "purchase", "season_pass", "achievement", "gift"
    private       boolean equipped;

    public CosmeticUnlock(UUID playerUuid, String cosmeticId, long unlockedAt, String source, boolean equipped) {
        this.playerUuid = playerUuid;
        this.cosmeticId = cosmeticId;
        this.unlockedAt = unlockedAt;
        this.source     = source;
        this.equipped   = equipped;
    }

    public void setEquipped(boolean equipped) { this.equipped = equipped; }

    public UUID    getPlayerUuid() { return playerUuid; }
    public String  getCosmeticId() { return cosmeticId; }
    public long    getUnlockedAt() { return unlockedAt; }
    public String  getSource()     { return source; }
    public boolean isEquipped()    { return equipped; }
}