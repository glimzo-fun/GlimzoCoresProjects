package me.pikashrey.glimzocore.api.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.rank.ConfiguredRank;

public class RankRef {

    private final String id;

    private volatile ConfiguredRank cachedConfiguredRank;
    private static final ConfiguredRank SENTINEL = new ConfiguredRank(
            "__sentinel__", null, 0,
            java.util.Collections.emptyList(),
            java.util.Collections.emptyList(),
            false, false);

    private RankRef(String id) {
        this.id = id.toLowerCase();
    }

    /** Create a RankRef for the given rank id. Never returns null. */
    public static RankRef of(String id) {
        if (id == null || id.isEmpty()) return of(Rank.getDefault().getId());
        return new RankRef(id);
    }

    /** Convenience factory from a legacy Rank enum value. */
    public static RankRef of(Rank rank) {
        return new RankRef(rank.getId());
    }

    // Metadata - config-first, enum fallback

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        ConfiguredRank cfg = configured();
        if (cfg != null) {
            Rank enumRank = Rank.fromId(id);
            return enumRank != null ? enumRank.getDisplayName() : capitalise(id);
        }
        Rank enumRank = Rank.fromId(id);
        return enumRank != null ? enumRank.getDisplayName() : capitalise(id);
    }

    public String getChatPrefix() {
        ConfiguredRank cfg = configured();
        if (cfg != null && cfg.getPrefix() != null) return cfg.getPrefix();
        Rank enumRank = Rank.fromId(id);
        return enumRank != null ? enumRank.getChatPrefix() : "&7";
    }

    /** Colour code portion of the prefix (e.g. "&4"). */
    public String getColorCode() {
        String prefix = getChatPrefix().trim();
        if (prefix.length() >= 2 && prefix.charAt(0) == '&') {
            return prefix.substring(0, 2);
        }
        return "&7";
    }

    public int getWeight() {
        ConfiguredRank cfg = configured();
        if (cfg != null) return cfg.getWeight();
        Rank enumRank = Rank.fromId(id);
        return enumRank != null ? enumRank.getWeight() : 0;
    }

    public boolean isDonor() {
        ConfiguredRank cfg = configured();
        if (cfg != null) return cfg.isDonor();
        Rank enumRank = Rank.fromId(id);
        return enumRank != null && enumRank.isDonor();
    }

    public boolean isStaff() {
        ConfiguredRank cfg = configured();
        if (cfg != null) return cfg.isStaff();
        Rank enumRank = Rank.fromId(id);
        return enumRank != null && enumRank.isStaff();
    }

    public Rank toEnum() {
        return Rank.fromId(id);
    }

    /** Returns true if a {@link ConfiguredRank} exists in ranks.yml for this id. */
    public boolean isConfigured() {
        return configured() != null;
    }

    /** Whether this rank id is resolvable (either in config or in the enum). */
    public boolean isValid() {
        return isConfigured() || Rank.fromId(id) != null;
    }

    public void invalidateCache() {
        cachedConfiguredRank = null;
    }

    // Comparison helpers

    public boolean isHigherThan(RankRef other) {
        return this.getWeight() > other.getWeight();
    }

    public boolean isAtLeast(RankRef other) {
        return this.getWeight() >= other.getWeight();
    }

    // Internal - cached resolution

    private ConfiguredRank configured() {
        ConfiguredRank cached = cachedConfiguredRank;
        if (cached == SENTINEL) return null;
        if (cached != null) return cached;

        // Resolve
        GlimzoCore core = GlimzoCore.getInstance();
        if (core == null) return null;
        me.pikashrey.glimzocore.features.rank.RankLoader loader = core.getRankLoader();
        if (loader == null) return null;
        ConfiguredRank found = loader.get(id);

        // Store result - use SENTINEL to represent "looked up, not found"
        cachedConfiguredRank = (found != null) ? found : SENTINEL;
        return found;
    }

    private static String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    // Object overrides

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RankRef)) return false;
        return id.equals(((RankRef) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "RankRef{" + id + ", weight=" + getWeight() + "}";
    }
}
