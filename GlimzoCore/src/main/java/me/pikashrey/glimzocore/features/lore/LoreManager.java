package me.pikashrey.glimzocore.features.lore;

import me.pikashrey.glimzocore.GlimzoCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoreManager {

    protected final GlimzoCore plugin;

    private final List<LoreChapter> chapters = new ArrayList<>();
    public LoreManager(GlimzoCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        chapters.clear();
        org.bukkit.configuration.file.FileConfiguration config =
                plugin.getConfigManager().getLore();
        if (config == null) return;
        java.util.List<?> chapList = config.getList("chapters");
        if (chapList == null) return;

        for (int ci = 0; ci < chapList.size(); ci++) {
            Object rawChap = chapList.get(ci);
            if (!(rawChap instanceof java.util.Map)) continue;
            java.util.Map<?, ?> chapMap = (java.util.Map<?, ?>) rawChap;

            Object rawChapTitle = chapMap.get("title");
            String chapTitle = rawChapTitle != null ? rawChapTitle.toString() : "Chapter " + (ci + 1);
            List<LoreEntry> entries = new ArrayList<>();

            Object rawEntries = chapMap.get("entries");
            if (rawEntries instanceof List) {
                List<?> entryList = (List<?>) rawEntries;
                for (int ei = 0; ei < entryList.size(); ei++) {
                    if (!(entryList.get(ei) instanceof java.util.Map)) continue;
                    java.util.Map<?, ?> eMap = (java.util.Map<?, ?>) entryList.get(ei);
                    Object rawId    = eMap.get("id");
                    Object rawTitle = eMap.get("title");
                    String id    = rawId    != null ? rawId.toString()    : "entry_" + ci + "_" + ei;
                    String title = rawTitle != null ? rawTitle.toString()  : "Entry " + (ei + 1);
                    List<String> content = new ArrayList<>();
                    Object rawContent = eMap.get("content");
                    if (rawContent instanceof List) {
                        for (Object line : (List<?>) rawContent) content.add(String.valueOf(line));
                    }
                    entries.add(new LoreEntry(id, title, content, ci, ei));
                }
            }
            chapters.add(new LoreChapter(ci, chapTitle, entries));
        }
    }

    public List<LoreChapter> getChapters() { return Collections.unmodifiableList(chapters); }

    public LoreChapter getChapter(int index) {
        return (index >= 0 && index < chapters.size()) ? chapters.get(index) : null;
    }

    public LoreEntry getEntry(String id) {
        for (LoreChapter ch : chapters) {
            for (LoreEntry e : ch.getEntries()) {
                if (e.getId().equals(id)) return e;
            }
        }
        return null;
    }
}

