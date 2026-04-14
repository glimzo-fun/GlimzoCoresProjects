package me.pikashrey.glimzocore.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Hologram {

    private final int id;                        // DB id
    private Location baseLocation;              // top-line anchor point
    private final List<HologramLine> lines  = new ArrayList<>();
    private final Set<UUID>         viewers = new HashSet<>();

    public Hologram(int id, Location baseLocation, List<String> lineTexts) {
        this.id           = id;
        this.baseLocation = baseLocation.clone();
        for (int i = 0; i < lineTexts.size(); i++) {
            Location lineLoc = topToLine(i);
            lines.add(new HologramLine(lineTexts.get(i), lineLoc));
        }
    }

    //  Spawn / Destroy

    /** Show this hologram to one player. Safe to call multiple times. */
    public void spawn(Player player) {
        if (viewers.contains(player.getUniqueId())) return;
        viewers.add(player.getUniqueId());
        for (HologramLine line : lines) {
            line.spawn(player);
        }
    }

    /** Hide this hologram from one player. */
    public void destroy(Player player) {
        if (!viewers.remove(player.getUniqueId())) return;
        for (HologramLine line : lines) {
            line.destroy(player);
        }
    }

    /** Show to all currently online players in the same world. */
    public void spawnAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(baseLocation.getWorld())) spawn(p);
        }
    }

    /** Despawn from every viewer. */
    public void destroyAll() {
        for (UUID uuid : new HashSet<>(viewers)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) destroy(p);
        }
        viewers.clear();
    }

    //  Line manipulation

    public void setLine(int lineIndex, String newText) {
        if (lineIndex < 0 || lineIndex >= lines.size()) return;
        Collection<Player> viewerPlayers = getViewerPlayers();
        lines.get(lineIndex).updateText(newText, viewerPlayers);
    }

    /** Appends a new line at the bottom of the hologram. */
    public void addLine(String text) {
        int index    = lines.size();
        Location loc = topToLine(index);
        HologramLine newLine = new HologramLine(text, loc);
        lines.add(newLine);
        for (Player p : getViewerPlayers()) {
            newLine.spawn(p);
        }
    }

    /** Removes the line at the given index. Requires full hologram respawn (cheap). */
    public void removeLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= lines.size()) return;
        destroyAll();
        lines.remove(lineIndex);
        // Rebuild positions for lines below
        rebuildPositions();
        spawnAll();
    }

    /** Replaces ALL lines. More efficient than removeLine x N for bulk updates. */
    public void setLines(List<String> newTexts) {
        destroyAll();
        lines.clear();
        for (int i = 0; i < newTexts.size(); i++) {
            lines.add(new HologramLine(newTexts.get(i), topToLine(i)));
        }
        spawnAll();
    }

    //  Read

    public String getLine(int index) {
        if (index < 0 || index >= lines.size()) return null;
        return lines.get(index).getText();
    }

    public List<String> getAllLines() {
        List<String> out = new ArrayList<>();
        for (HologramLine l : lines) out.add(l.getText());
        return out;
    }

    public int lineCount() { return lines.size(); }

    //  Helpers

    private Location topToLine(int index) {
        // Top line is at baseLocation, each subsequent line is LOWER
        return baseLocation.clone().subtract(0, index * HologramLine.LINE_HEIGHT, 0);
    }

    private void rebuildPositions() {
        for (int i = 0; i < lines.size(); i++) {
            // We rebuild by creating new entities - simple and correct
            HologramLine old = lines.get(i);
            lines.set(i, new HologramLine(old.getText(), topToLine(i)));
        }
    }

    private Collection<Player> getViewerPlayers() {
        List<Player> out = new ArrayList<>();
        for (UUID uuid : viewers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) out.add(p);
        }
        return out;
    }

    //  Getters

    public int getId()                { return id; }
    public Location getBaseLocation() { return baseLocation.clone(); }
    public Set<UUID> getViewers()     { return Collections.unmodifiableSet(viewers); }
    public boolean isViewer(UUID uuid){ return viewers.contains(uuid); }
    public List<HologramLine> getLines() { return Collections.unmodifiableList(lines); }
}
