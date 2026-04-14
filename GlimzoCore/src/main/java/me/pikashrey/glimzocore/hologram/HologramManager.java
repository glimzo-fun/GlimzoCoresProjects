package me.pikashrey.glimzocore.hologram;

import me.pikashrey.glimzocore.database.mysql.MySQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private static final String LINE_DELIMITER = "\u00A7\u00A7"; // §§

    private final JavaPlugin   plugin;
    private final MySQLManager mysqlManager;

    private final Map<Integer, Hologram> holograms = new ConcurrentHashMap<>();

    public HologramManager(JavaPlugin plugin, MySQLManager mysqlManager) {
        this.plugin        = plugin;
        this.mysqlManager  = mysqlManager;
    }

    //  Lifecycle

    public void load() {
        createTableIfNotExists();
        loadFromDatabase();
    }

    public void unload() {
        for (Hologram h : holograms.values()) h.destroyAll();
        holograms.clear();
    }

    //  Public API

    /**
     * Create a new hologram. The DB insert runs async; once the id is assigned
     * the hologram is spawned on the main thread. Returns immediately with null -
     * the hologram will appear for online players within one tick.
     */
    public void create(Location loc, List<String> lines) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int id = insertDatabase(loc, lines);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (id < 0) {
                    plugin.getLogger().warning("[HologramManager] Failed to persist hologram.");
                    return;
                }
                Hologram h = new Hologram(id, loc, lines);
                holograms.put(id, h);
                h.spawnAll();
            });
        });
    }

    public boolean delete(int id) {
        Hologram h = holograms.remove(id);
        if (h == null) return false;
        h.destroyAll();
        // DB delete is fire-and-forget - run async to avoid blocking main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> deleteDatabase(id));
        return true;
    }

    public void save(Hologram h) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> updateDatabase(h));
    }

    public Hologram getById(int id) {
        return holograms.get(id);
    }

    public Collection<Hologram> getAll() {
        return Collections.unmodifiableCollection(holograms.values());
    }

    //  Player join / quit

    public void onPlayerJoin(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            for (Hologram h : holograms.values()) {
                if (h.getBaseLocation().getWorld().equals(player.getWorld())) {
                    h.spawn(player);
                }
            }
        }, 20L);
    }

    public void onPlayerQuit(Player player) {
        for (Hologram h : holograms.values()) {
            h.destroy(player);
        }
    }

    //  MySQL

    private void createTableIfNotExists() {
        String ddl = "CREATE TABLE IF NOT EXISTS glimzo_holograms (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "world VARCHAR(100) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "`lines` TEXT NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        try (Connection con = mysqlManager.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(ddl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDatabase() {
        try (Connection con = mysqlManager.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT * FROM glimzo_holograms")) {
            while (rs.next()) {
                int    id        = rs.getInt("id");
                String worldName = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                String rawLines  = rs.getString("lines");

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("[HologramManager] World '" + worldName
                            + "' not found, skipping hologram " + id);
                    continue;
                }

                List<String> lines = Arrays.asList(rawLines.split(LINE_DELIMITER, -1));
                holograms.put(id, new Hologram(id, new Location(world, x, y, z), lines));
            }
            plugin.getLogger().info("[HologramManager] Loaded " + holograms.size() + " hologram(s).");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int insertDatabase(Location loc, List<String> lines) {
        String sql = "INSERT INTO glimzo_holograms (world, x, y, z, lines) VALUES (?,?,?,?,?)";
        try (Connection con = mysqlManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, loc.getWorld().getName());
            ps.setDouble(2, loc.getX());
            ps.setDouble(3, loc.getY());
            ps.setDouble(4, loc.getZ());
            ps.setString(5, String.join(LINE_DELIMITER, lines));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void updateDatabase(Hologram h) {
        String sql = "UPDATE glimzo_holograms SET lines=? WHERE id=?";
        try (Connection con = mysqlManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, String.join(LINE_DELIMITER, h.getAllLines()));
            ps.setInt(2, h.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDatabase(int id) {
        String sql = "DELETE FROM glimzo_holograms WHERE id=?";
        try (Connection con = mysqlManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
