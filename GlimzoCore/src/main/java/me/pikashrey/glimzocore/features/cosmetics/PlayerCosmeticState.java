package me.pikashrey.glimzocore.features.cosmetics;

import java.util.UUID;

public class PlayerCosmeticState {

    private final UUID uuid;

    private String activeAuraId;
    private String activeWingsId;
    private String activeAllyId;
    private String activeMorphId;
    private String activeChatTagId;
    private String activeJoinEffectId;
    private String activeJoinMessageId;

    /** Set whenever any active cosmetic changes. Cleared after a DB save. */
    private volatile boolean dirty = false;

    public PlayerCosmeticState(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }

    public boolean isDirty()    { return dirty; }
    public void    markDirty()  { this.dirty = true; }
    public void    clearDirty() { this.dirty = false; }

    public String getActiveAuraId()      { return activeAuraId; }
    public void   setActiveAuraId(String id)      { this.activeAuraId = id;       dirty = true; }

    public String getActiveWingsId()     { return activeWingsId; }
    public void   setActiveWingsId(String id)     { this.activeWingsId = id;      dirty = true; }

    public String getActiveAllyId()      { return activeAllyId; }
    public void   setActiveAllyId(String id)      { this.activeAllyId = id;       dirty = true; }

    public String getActiveMorphId()     { return activeMorphId; }
    public void   setActiveMorphId(String id)     { this.activeMorphId = id;      dirty = true; }

    public String getActiveChatTagId()   { return activeChatTagId; }
    public void   setActiveChatTagId(String id)   { this.activeChatTagId = id;    dirty = true; }

    public String getActiveJoinEffectId(){ return activeJoinEffectId; }
    public void   setActiveJoinEffectId(String id){ this.activeJoinEffectId = id; dirty = true; }

    public String getActiveJoinMessageId(){ return activeJoinMessageId; }
    public void   setActiveJoinMessageId(String id){ this.activeJoinMessageId = id; dirty = true; }

    public boolean hasAura()      { return activeAuraId      != null; }
    public boolean hasWings()     { return activeWingsId     != null; }
    public boolean hasAlly()      { return activeAllyId      != null; }
    public boolean hasMorph()     { return activeMorphId     != null; }
    public boolean hasChatTag()   { return activeChatTagId   != null; }
    public boolean hasJoinEffect(){ return activeJoinEffectId!= null; }
    public boolean hasJoinMessage(){ return activeJoinMessageId != null; }
}
