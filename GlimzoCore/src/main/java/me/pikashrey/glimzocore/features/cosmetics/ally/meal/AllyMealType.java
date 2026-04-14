package me.pikashrey.glimzocore.features.cosmetics.ally.meal;

/**
 * Every purchasable Ally Meal item.
 * Costs and XP values are read from cosmetics.yml at runtime.
 */
public enum AllyMealType {

    NUTELLA          ("nutella",           "Nutella",                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWM5MWQzMzc4NmUzZTNmMWJiMWQ1NDI0NzQyZTMzYzRkYThhZDAyMGEyNjZhOWFjODAwZjBkOTVjOGJiYmEzIn19fQ=="),
    GLASS_OF_BEER    ("glass_of_beer",     "Glass of Beer",                  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA1M2UyNjg2N2JiNTc1MzhlOTc4OTEzN2RiYmI1Mzc3NGUxOGVkYTZmZWY1MWNiMmVkZjQyNmIzNzI2NCJ9fX0="),
    BREAD            ("bread",             "Bread",                          "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjM0ODdkNDU3ZjkwNjJkNzg3YTNlNmNlMWM0NjY0YmY3NDAyZWM2N2RkMTExMjU2ZjE5YjM4Y2U0ZjY3MCJ9fX0="),
    CHEESE           ("cheese",            "Cheese",                         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU1ZDYxMWE4NzhlODIxMjMxNzQ5YjI5NjU3MDhjYWQ5NDI2NTA2NzJkYjA5ZTI2ODQ3YTg4ZTJmYWMyOTQ2In19fQ=="),
    STRAWBERRY_JAM   ("strawberry_jam",    "Strawberry Jam",                 "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzBiOGI1ODg5ZWUxYzYzODhkYzZjMmM1ZGJkNzBiNjk4NGFlZmU1NDMxOWEwOTVlNjRkYjc2MzgwOTdiODIxIn19fQ=="),
    CAKE             ("cake",              "Cake",                           "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTIxZDhkOWFlNTI3OGUyNmJjNDM5OTkyM2QyNWNjYjkxNzNlODM3NDhlOWJhZDZkZjc2MzE0YmE5NDM2OWUifX19"),
    CHERRY           ("cherry",            "Cherry",                         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgzN2E0OGVlNTMwY2ZlMzVhY2EzNzk2OWU0ZWE3MWQ4NzUyMzdkMmNiN2E4MWIxYWU4MGE3NWRjNzZlNWEifX19"),
    APPLE            ("apple",             "Apple",                          "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdiMTVmYzBmODk0NTNiYjljODY5ZGJjNDdhNjZjZjJlNzVlOGQzNzEzYjVmYmY3Yzg0Y2JmMmM2MzIxOTYifX19"),
    MELON            ("melon",             "Melon",                          "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNmZWQ1MTRjM2UyMzhjYTdhYzFjOTRiODk3ZmY2NzExYjFkYmU1MDE3NGFmYzIzNWM4ZjgwZDAyOSJ9fX0="),
    STRAWBERRY       ("strawberry",        "Strawberry",                     "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2JjODI2YWFhZmI4ZGJmNjc4ODFlNjg5NDQ0MTRmMTM5ODUwNjRhM2Y4ZjA0NGQ4ZWRmYjQ0NDNlNzZiYSJ9fX0="),
    COCONUT          ("coconut",           "Coconut",                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY2MTI1OWE3ZWQ3NWRmYzE1ZjQzMjhmNjlmYTVkNTQ5ZWYxYmE5YzdhYTg1YzUzYjhjNzYxNzNmYWMzYzY5In19fQ=="),
    TOMATO           ("tomato",            "Tomato",                         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMyOWI2OTI2ZTI1YTdjNzI1ZTk1OTY0OGJkNGYyYThlZWUzZjI5MjFjYmRjNDVkMzk5ZGY2Mjc4NmE3N2MifX19"),
    HAM              ("ham",               "Ham",                            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjYzMzZmNWJiOTk3NWJmNTdlMTRkYjY2MTVjMTg5NmM1YzRiOWMzOWFhZDE3YjE3ZTRlZTIwYjIzMWNmNiJ9fX0="),
    PINK_FROSTED_DONUT("pink_frosted_donut","Pink Frosted Donut",            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNlMDJlMjc2ZGY2YWIyODdjYjg4ZTY5NzJmZWNhYjlhNTE2OWM4Y2Y5MzQ5MmFlNTkzMTY2MTE0YzIxMjZhIn19fQ=="),
    CHOCOLATE_DONUT  ("chocolate_donut",   "Chocolate Frosted Donut",        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTlkYTU0ZmYzNjZlNzM4ZTMxZGU5MjkxOTk4NmFiYjRkNTBjYTk0NGZhOTkyNmFmNjM3NThiNzQ0OGYxOCJ9fX0="),
    OREO             ("oreo",              "Oreo",                           "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFmNjg1YTc0YTVjZDE2M2U0NjZjOTNiYWM1NGYyOTNkNTk4YWQ5ZmRiZWY2NmFkNGQ0OGU0NjU2OWFjYSJ9fX0="),
    HAMBURGER        ("hamburger",         "Hamburger",                      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzVlMjc5ODhhNjUzODAxMGVmYzBlMjQ3NTZiYzNlM2VlYTI0ZDc1MzZiMjA5MzJjMTdlMDQwNGU1Y2M1NWYifX19"),
    SUSHI_ROLL       ("sushi_roll",        "Sushi Roll",                     "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTA0MDUxODFkMzllNzYxOTdhMjYyYmU0Y2M2NTQxZThlM2VkMjQ2MzMzODRjODczYWRiOTFkZmUzOTAxYyJ9fX0="),
    COFFEE_CUP       ("coffee_cup",        "Coffee Cup",                     "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFkYWFmOGU3NGI0YTEzMjMwOTExMGVjOWMzOWM2YzBhMmU0NDM5MWVhYWRkYjFkY2NjMDhiOWY2ZDRiYzY3NCJ9fX0="),
    COCA_COLA        ("coca_cola",         "Coca-Cola Can",                  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE3MmE3ZWE3NTMzMjQ4MzFkODEzN2MxNmZiM2VmNmQxYWVlMjhiODIxNzhlNDhmZGEzMGJiNTA5YjhlIn19fQ=="),
    RAMEN            ("ramen",             "Bowl of Ramen",                  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY4MzRiNWIyNTQyNmRlNjM1MzhlYzgyY2E4ZmJlY2ZjYmIzZTY4MmQ4MDYzNjQzZDJlNjdhNzYyMWJkIn19fQ=="),
    SOUP             ("soup",              "Soup",                           "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNhZWU2ZTlhYzVlNzEwYzk4OWYyN2MzY2MwNDA2Njk2OTIxNDI1MTUxNzZmZTRiZDZiYTllN2I5YmU3MzMwIn19fQ=="),
    SPAGHETTI        ("spaghetti",         "Bowl of Spaghetti",              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ3ZmU2NWViNzQ1NDY4ZTg2ODczYTFiZGE0OGE1YTQ4OWZlZjkxY2M1MjJkODVlMDM2NGI1NWQ1M2Y4NjdlIn19fQ=="),
    SANDWICH         ("sandwich",          "Sandwich",                       "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ5NjU4OWZiNWMxZjY5Mzg3YjdmYjE3ZDkyMzEyMDU4ZmY2ZThlYmViM2ViODllNGY3M2U3ODE5NjExM2IifX19"),
    CHEESEBURGER     ("cheeseburger",      "Cheeseburger",                   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFlOWM5YmM4OThjN2NmZTlhM2IxMWI2ZGVhZWQxNzU3ODE3YWQwMjUxMWFiNWE4YjMwNmFiOTJlZWIzZTJkIn19fQ=="),
    CHOCOLATE_CAKE   ("chocolate_cake",    "Chocolate Cake",                 "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExOWZjYTRmMjhhNzU1ZDM3ZmJlNWRjZjZkOGMzZWY1MGZlMzk0YzFhNzg1MGJjN2UyYjcxZWU3ODMwM2M0YyJ9fX0="),
    CHOCOLATE        ("chocolate",         "Chocolate",                      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFjZmM3ODY0NjllNjM2YmY0ZWFhYzJlZDQ5ZDlhNmM1MjEyYWY2ZDkwNzFkOTYxOTQ0YmU4YTkzNWY0NzhlIn19fQ=="),
    PLUM             ("plum",              "Plum",                           "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWNjMDE2ZjU2OGQxNDMzODYwZDgyZmEzMzc5ZDc4NGNiYmQ1MmU1NmI1NWY3OGJlNzI5MWY4NjE4ZGEzOGM4In19fQ=="),
    BLUEBERRY        ("blueberry",         "Blueberry",                      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDFlOGUzNTJiM2ZiYWRjZTk5OGYxNTM1OTVkODM0MGRlZDI4NWMwNWFmNGFmNTdjNDQ1MWU4MjE0ZTFhZmIyIn19fQ=="),
    FRENCH_FRIES     ("french_fries",      "French Fries",                   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTYzYjhhZWFmMWRmMTE0ODhlZmM5YmQzMDNjMjMzYTg3Y2NiYTNiMzNmN2ZiYTljMmZlY2FlZTk1NjdmMDUzIn19fQ=="),
    TACO             ("taco",              "Taco",                           "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFkN2MwYTA0ZjE0ODVjN2EzZWYyNjFhNDhlZTgzYjJmMWFhNzAxYWIxMWYzZmM5MTFlMDM2NmE5Yjk3ZSJ9fX0="),
    SUSHI            ("sushi",             "Sushi",                          "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjNlNGY1Y2NlNWEyMmFkZDFlZTY0MWY5MjFiMmI5ZWYyMWQ1MTA2OWZmMWEzOTc2MTg2MTE5N2I4MDczNWFmMiJ9fX0=");

    private final String id;
    private final String displayName;
    private final String texture;

    AllyMealType(String id, String displayName, String texture) {
        this.id          = id;
        this.displayName = displayName;
        this.texture     = texture;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getTexture()     { return texture; }

    public static AllyMealType fromId(String id) {
        if (id == null) return null;
        for (AllyMealType t : values())
            if (t.id.equalsIgnoreCase(id)) return t;
        return null;
    }
}
