package me.pikashrey.glimzocore.features.lore;
import java.util.List;
public class LoreEntry {
    private final String id;
    private final String title;
    private final List<String> content;
    private final int chapterIndex;
    private final int entryIndex;
    public LoreEntry(String id, String title, List<String> content, int chapterIndex, int entryIndex) {
        this.id = id; this.title = title; this.content = content;
        this.chapterIndex = chapterIndex; this.entryIndex = entryIndex;
    }
    public String getId()         { return id; }
    public String getTitle()      { return title; }
    public List<String> getContent() { return content; }
    public int getChapterIndex()  { return chapterIndex; }
    public int getEntryIndex()    { return entryIndex; }
}
