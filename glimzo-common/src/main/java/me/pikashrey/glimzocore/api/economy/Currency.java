package me.pikashrey.glimzocore.api.economy;

public enum Currency {

    COINS("Coins", "\u2739"),
    GEMS("Gems",   "\u25C6");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol      = symbol;
    }

    public String getDisplayName() { return displayName; }
    public String getSymbol()      { return symbol; }

    @Override
    public String toString() { return displayName; }
}