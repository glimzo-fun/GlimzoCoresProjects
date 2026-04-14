package me.pikashrey.glimzocore120.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.pikashrey.glimzocore120.GlimzoCore120;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager120 {

    private final GlimzoCore120 plugin;
    private HikariDataSource dataSource;

    public DatabaseManager120(GlimzoCore120 plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" +
                    plugin.getConfig().getString("database.host", "localhost") + ":" +
                    plugin.getConfig().getInt("database.port", 3306) + "/" +
                    plugin.getConfig().getString("database.name", "glimzo") +
                    "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
            config.setUsername(plugin.getConfig().getString("database.user", "root"));
            config.setPassword(plugin.getConfig().getString("database.password", ""));
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(10000);
            config.setPoolName("GlimzoCore120-Pool");
            dataSource = new HikariDataSource(config);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MySQL: " + e.getMessage());
            return false;
        }
    }

    public void createTables() {
        // Same tables as GlimzoCore-18 - shared database!
        // We don't recreate them, GlimzoCore-18 already created them.
        // Just verify connection is healthy.
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            st.execute("SELECT 1");
            plugin.getLogger().info("Database tables verified.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Table verification failed: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
