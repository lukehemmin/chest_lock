package com.chestlock.data;

import com.chestlock.ChestLock;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Manages MySQL/MariaDB connections using HikariCP connection pooling
 */
public class DatabaseManager {

    private final ChestLock plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(ChestLock plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the database connection pool
     */
    public void connect() {
        String host = plugin.getConfig().getString("storage.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("storage.mysql.port", 3306);
        String database = plugin.getConfig().getString("storage.mysql.database", "chestlock");
        String username = plugin.getConfig().getString("storage.mysql.username", "root");
        String password = plugin.getConfig().getString("storage.mysql.password", "password");
        int maxPoolSize = plugin.getConfig().getInt("storage.mysql.pool.maximum-pool-size", 10);
        int minIdle = plugin.getConfig().getInt("storage.mysql.pool.minimum-idle", 2);
        long connectionTimeout = plugin.getConfig().getLong("storage.mysql.pool.connection-timeout", 30000);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);

        // Performance settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // Connection pool name
        config.setPoolName("ChestLock-HikariCP");

        try {
            dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Successfully connected to MySQL database!");
            plugin.getLogger().info(String.format("Connection: %s:%d/%s", host, port, database));

            // Create tables
            createTables();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MySQL database!");
            plugin.getLogger().severe("Error: " + e.getMessage());
            plugin.getLogger().severe("Falling back to YAML storage...");
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    /**
     * Close the database connection pool
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Disconnected from MySQL database");
        }
    }

    /**
     * Get a connection from the pool
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or has been closed");
        }
        return dataSource.getConnection();
    }

    /**
     * Check if the database is connected
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Create necessary database tables
     */
    private void createTables() {
        String createProtectionsTable =
            "CREATE TABLE IF NOT EXISTS chestlock_protections (" +
            "  id INT AUTO_INCREMENT PRIMARY KEY," +
            "  world VARCHAR(255) NOT NULL," +
            "  x INT NOT NULL," +
            "  y INT NOT NULL," +
            "  z INT NOT NULL," +
            "  owner VARCHAR(36) NOT NULL," +
            "  allow_hopper BOOLEAN DEFAULT FALSE," +
            "  allow_redstone BOOLEAN DEFAULT TRUE," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "  UNIQUE KEY unique_location (world, x, y, z)," +
            "  INDEX idx_owner (owner)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        String createFriendsTable =
            "CREATE TABLE IF NOT EXISTS chestlock_friends (" +
            "  id INT AUTO_INCREMENT PRIMARY KEY," +
            "  protection_id INT NOT NULL," +
            "  friend_uuid VARCHAR(36) NOT NULL," +
            "  permission VARCHAR(20) NOT NULL," +
            "  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  UNIQUE KEY unique_friend (protection_id, friend_uuid)," +
            "  FOREIGN KEY (protection_id) REFERENCES chestlock_protections(id) ON DELETE CASCADE," +
            "  INDEX idx_friend_uuid (friend_uuid)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(createProtectionsTable);
                 PreparedStatement stmt2 = conn.prepareStatement(createFriendsTable)) {

                stmt1.executeUpdate();
                stmt2.executeUpdate();

                plugin.getLogger().info("Database tables created successfully");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
            throw new RuntimeException("Failed to create database tables", e);
        }
    }
}
