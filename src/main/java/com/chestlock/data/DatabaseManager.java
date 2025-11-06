package com.chestlock.data;

import com.chestlock.ChestLock;
import com.chestlock.data.migration.DatabaseMigrator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages MySQL/MariaDB connections using HikariCP connection pooling
 * with automatic schema migration support
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

            // Run database migrations
            runMigrations();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MySQL database!");
            plugin.getLogger().severe("Error: " + e.getMessage());
            plugin.getLogger().severe("Falling back to YAML storage...");
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    /**
     * Run database schema migrations
     */
    private void runMigrations() {
        plugin.getLogger().info("Checking database schema version...");
        DatabaseMigrator migrator = new DatabaseMigrator(plugin, this);
        migrator.migrate();
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
}
