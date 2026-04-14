package me.pikashrey.glimzocore.nametags.handler;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.nametags.NameTag;
import me.pikashrey.glimzocore.nametags.adapter.NameTagAdapter;
import me.pikashrey.glimzocore.nametags.adapter.impl.NameTagAdapterImpl;
import me.pikashrey.glimzocore.nametags.board.NameTagBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameTagHandler {

    private final GlimzoCore                  plugin;
    private final NameTagAdapter              adapter;
    private final Map<UUID, NameTagBoard>     boards = new HashMap<>();

    public NameTagHandler(GlimzoCore plugin) {
        this.plugin  = plugin;
        this.adapter = new NameTagAdapterImpl(plugin);
    }

    /**
     * Called when a player joins. Sets up their board and registers them on all others.
     */
    public void onJoin(Player joining) {
        NameTagBoard board = new NameTagBoard(joining);
        boards.put(joining.getUniqueId(), board);

        // Populate all existing players on the new board
        board.initAllPlayers(Bukkit.getOnlinePlayers(),
                p -> adapter.getNameTag(p));

        // Push the joining player's tag onto every existing board
        NameTag joiningTag = adapter.getNameTag(joining);
        for (Map.Entry<UUID, NameTagBoard> entry : boards.entrySet()) {
            if (!entry.getKey().equals(joining.getUniqueId())) {
                entry.getValue().applyTag(joining, joiningTag);
            }
        }
    }

    /**
     * Called when a player quits. Tears down their board and removes them from others.
     */
    public void onQuit(UUID uuid) {
        boards.remove(uuid);
        for (NameTagBoard board : boards.values()) {
            board.removeTag(uuid);
        }
    }

    /**
     * Push an updated tag for a player to all boards (called on rank change).
     */
    public void refresh(Player player) {
        NameTag tag = adapter.getNameTag(player);
        for (NameTagBoard board : boards.values()) {
            board.applyTag(player, tag);
        }
    }

    /**
     * Full refresh for all players - called periodically by NameTagThread.
     */
    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    public NameTagBoard getBoard(UUID uuid) {
        return boards.get(uuid);
    }
}

