package me.pikashrey.glimzocore.features.rank;

import java.util.List;

public class ConfiguredRank {

    private final String id;
    private final String prefix;
    private final int weight;
    private final List<String> permissions;
    private final List<String> inheritance;
    private final boolean staff;
    private final boolean donor;
    private final boolean isDefault;
    private final int maxClanSize;

    public ConfiguredRank(String id, String prefix, int weight,
                          List<String> permissions, List<String> inheritance,
                          boolean staff, boolean donor) {
        this(id, prefix, weight, permissions, inheritance, staff, donor, false, 0);
    }

    public ConfiguredRank(String id, String prefix, int weight,
                          List<String> permissions, List<String> inheritance,
                          boolean staff, boolean donor, boolean isDefault, int maxClanSize) {
        this.id          = id.toLowerCase();
        this.prefix      = prefix;
        this.weight      = weight;
        this.permissions = permissions;
        this.inheritance = inheritance;
        this.staff       = staff;
        this.donor       = donor;
        this.isDefault   = isDefault;
        this.maxClanSize = maxClanSize;
    }

    public String       getId()          { return id; }
    public String       getPrefix()      { return prefix; }
    public int          getWeight()      { return weight; }
    public List<String> getPermissions() { return permissions; }
    public List<String> getInheritance() { return inheritance; }
    public boolean      isStaff()        { return staff; }
    public boolean      isDonor()        { return donor; }
    public boolean      isDefault()      { return isDefault; }
    public int          getMaxClanSize() { return maxClanSize; }
}
