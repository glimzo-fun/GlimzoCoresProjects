package me.pikashrey.glimzocore.features.rank;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.permission.PermissionCache;
import me.pikashrey.glimzocore.features.permission.PermissionResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class RankLoader {

    private final GlimzoCore plugin;
    private final PermissionCache cache;
    private final PermissionResolver resolver;

    private final Map<String, ConfiguredRank> ranks = new HashMap<>();

    public RankLoader(GlimzoCore plugin, PermissionCache cache, PermissionResolver resolver) {
        this.plugin = plugin;
        this.cache = cache;
        this.resolver = resolver;
    }

    public void load() {
        ranks.clear();
        cache.clearRanks();

        File file = new File(plugin.getDataFolder(), "ranks.yml");
        if (!file.exists()) {
            plugin.saveResource("ranks.yml", false);
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("ranks");
        if (root == null) {
            plugin.log("&c[ranks.yml] Missing 'ranks' section.");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection rs = root.getConfigurationSection(id);
            if (rs == null) continue;

            String prefix = rs.getString("prefix", "&7");
            int weight = rs.getInt("weight", 0);
            List<String> perms = rs.getStringList("permissions");
            List<String> inheritance = rs.getStringList("inheritance");
            boolean staff       = rs.getBoolean("staff", false);
            boolean donor       = rs.getBoolean("donor", false);
            boolean isDefault   = rs.getBoolean("default", false);
            int     maxClanSize = rs.getInt("max-clan-size", 0);

            if (ranks.containsKey(id.toLowerCase())) {
                throw new IllegalStateException("Duplicate rank id in ranks.yml: " + id);
            }

            ConfiguredRank rank = new ConfiguredRank(
                    id,
                    prefix,
                    weight,
                    perms != null ? perms : Collections.emptyList(),
                    inheritance != null ? inheritance : Collections.emptyList(),
                    staff,
                    donor,
                    isDefault,
                    maxClanSize
            );
            ranks.put(rank.getId(), rank);
        }

        Map<String, List<String>> inheritance = buildInheritanceMap();
        validateParentsExist(inheritance);
        validateNoLoops(inheritance);

        Map<String, List<String>> permissions = buildPermissionsMap();
        for (String id : ranks.keySet()) {
            Set<String> flattened = resolver.resolve(id, inheritance, permissions);
            cache.cacheRank(id, flattened);
        }

        plugin.log("&a[RankLoader] Loaded " + ranks.size() + " ranks from ranks.yml");
    }

    private Map<String, List<String>> buildInheritanceMap() {
        Map<String, List<String>> map = new HashMap<>();
        for (ConfiguredRank r : ranks.values()) {
            map.put(r.getId(), new ArrayList<>(r.getInheritance()));
        }
        return map;
    }

    private Map<String, List<String>> buildPermissionsMap() {
        Map<String, List<String>> map = new HashMap<>();
        for (ConfiguredRank r : ranks.values()) {
            map.put(r.getId(), new ArrayList<>(r.getPermissions()));
        }
        return map;
    }

    private void validateParentsExist(Map<String, List<String>> inheritance) {
        for (Map.Entry<String, List<String>> e : inheritance.entrySet()) {
            for (String parent : e.getValue()) {
                if (!ranks.containsKey(parent.toLowerCase())) {
                    throw new IllegalStateException("Rank " + e.getKey()
                            + " inherits unknown parent '" + parent + "'");
                }
            }
        }
    }

    private void validateNoLoops(Map<String, List<String>> inheritance) {
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String id : inheritance.keySet()) {
            dfs(id, inheritance, visiting, visited);
        }
    }

    private void dfs(String id,
                     Map<String, List<String>> inheritance,
                     Set<String> visiting,
                     Set<String> visited) {
        if (visited.contains(id)) return;
        if (visiting.contains(id)) {
            throw new IllegalStateException("Inheritance loop detected at rank: " + id);
        }
        visiting.add(id);
        for (String parent : inheritance.getOrDefault(id, Collections.emptyList())) {
            dfs(parent.toLowerCase(), inheritance, visiting, visited);
        }
        visiting.remove(id);
        visited.add(id);
    }

    public ConfiguredRank get(String id) {
        return id == null ? null : ranks.get(id.toLowerCase());
    }

    public Collection<ConfiguredRank> getAll() {
        return Collections.unmodifiableCollection(ranks.values());
    }
}

