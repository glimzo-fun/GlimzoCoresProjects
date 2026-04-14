package me.pikashrey.glimzocore.features.lore;
import java.util.List;
public class LoreChapter {
    private final int    index;
    private final String title;
    private final List<LoreEntry> entries;
    public LoreChapter(int index, String title, List<LoreEntry> entries) {
        this.index = index; this.title = title; this.entries = entries;
    }
    public int              getIndex()   { return index; }
    public String           getTitle()   { return title; }
    public List<LoreEntry>  getEntries() { return entries; }
}
