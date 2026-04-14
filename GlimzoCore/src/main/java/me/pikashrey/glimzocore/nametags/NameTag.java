package me.pikashrey.glimzocore.nametags;

public class NameTag {

    private final String prefix; // e.g. "§4[Chief] "
    private final String suffix; // e.g. "" (unused for now)

    public NameTag(String prefix, String suffix) {
        this.prefix = prefix != null ? prefix : "";
        this.suffix = suffix != null ? suffix : "";
    }

    public String getPrefix() { return prefix; }
    public String getSuffix() { return suffix; }

    /** Scoreboard team names must be ≤ 16 chars in 1.8. Use a hash of uuid. */
    public static String teamName(java.util.UUID uuid) {
        String hex = uuid.toString().replace("-", "");
        return hex.substring(0, Math.min(16, hex.length()));
    }
}

