package me.pikashrey.glimzocore.api.events.impl;

import me.pikashrey.glimzocore.api.events.GlimzoEvent;
import me.pikashrey.glimzocore.api.punishment.Punishment;
import org.bukkit.event.HandlerList;

public class PlayerUnPunishEvent extends GlimzoEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Punishment punishment;

    public PlayerUnPunishEvent(Punishment punishment) {
        this.punishment = punishment;
    }

    public Punishment getPunishment() { return punishment; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}