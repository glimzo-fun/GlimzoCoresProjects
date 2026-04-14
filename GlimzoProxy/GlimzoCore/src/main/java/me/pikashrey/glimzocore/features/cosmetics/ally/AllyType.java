package me.pikashrey.glimzocore.features.cosmetics.ally;

public enum AllyType {

    MR_PANDA  ("mr_panda",   "Mr. Panda",  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTk1ODU5ZWZkYjRmNzYyNmJjMjA1NTM0MWNkMmZhYWIzY2MwNjAyYzhhY2I1YzkxNDg1ZmRiYmFlMzExMzI1NCJ9fX0="),
    FALCON    ("falcon",     "Falcon",     "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDE2ZjU5NWU4ZjY3OTFiYzE1NDY1OWE4OTc2ZjZhOGZmZDk4NDdjZjc1YTJiZjYzOTkyZTNhNjU1ZTAifX19"),
    KITTY     ("kitty",      "Kitty",      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjMyMTY2NzFmYzZiNGM1NzkyZTBjOGI5YzYzYjdiZGRiYThkZGU0OGI5NDM4MzgxZWIyNTFiNGYwNTg4MTU4ZSJ9fX0="),
    PUG       ("pug",        "Pug",        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmViZjdiNmUxZTY1MzRkODU4ZDRhZjA4MTM1OGM4ZTNjZGE3ZDQ4NzYxM2FiYjY1ODBhZmYzZjE4NTE0M2EifX19"),
    CHARIZARD ("charizard",  "Charizard",  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkzN2ZiYTBiMWU5ODg1ZmI0YTg0YzkxNTA1MTNkZWU4YjIxN2NkMDRmMTQwZDI1MDVjYWI4YWUzOWI1ZDQifX19"),
    DR_DUCKY  ("dr_ducky",   "Dr. Ducky",  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM1N2ZiOGUzMjQyOWI3MWM2NjhkYjg2NjI4YTZkMWM0MDg2MzJiZDgzNWJmYWZhYTdlOTliOTQ0MGRjYTgifX19");

    private final String id;
    private final String displayName;
    private final String headTexture;

    AllyType(String id, String displayName, String headTexture) {
        this.id          = id;
        this.displayName = displayName;
        this.headTexture = headTexture;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getHeadTexture() { return headTexture; }

    public static AllyType fromId(String id) {
        if (id == null) return null;
        for (AllyType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    /**
     * Resolves an ally from any player input style:
     *   "mr_panda", "Mr. Panda", "mr. panda", "mr panda", "mrpanda"
     * Case-insensitive. Used by AllyCommand so multi-word names work.
     */
    public static AllyType fromInput(String input) {
        if (input == null || input.isEmpty()) return null;
        // Normalise: lowercase, strip dots and underscores, collapse spaces
        String norm = input.toLowerCase().replace(".", "").replace("_", " ").trim();
        for (AllyType t : values()) {
            String normId      = t.id.replace("_", " ");
            String normDisplay = t.displayName.toLowerCase().replace(".", "").trim();
            if (norm.equals(normId) || norm.equals(normDisplay)) return t;
        }
        return null;
    }
}
