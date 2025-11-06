package com.chestlock.data.migration;

import com.chestlock.ChestLock;
import com.chestlock.data.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages database schema migrations safely
 * - Automatic version tracking
 * - Safe migration execution
 * - Backup and rollback support
 * - Data integrity validation
 */
public class DatabaseMigrator {

    private final ChestLock plugin;
    private final DatabaseManager databaseManager;
    private final List<Migration> migrations;

    // Current schema version (update this when adding new migrations)
    private static final int CURRENT_VERSION = 1;

    public DatabaseMigrator(ChestLock plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.migrations = new ArrayList<>();

        // Register all migrations in order
        registerMigrations();
    }

    /**
     * Register all available migrations
     */
    private void registerMigrations() {
        migrations.add(new MigrationV1());
        // migrations.add(new MigrationV2()); // Uncomment when needed
        // Add future migrations here
    }

    /**
     * Run all pending migrations
     */
    public void migrate() {
        try (Connection conn = databaseManager.getConnection()) {
            // Ensure schema version table exists
            createSchemaVersionTable(conn);

            // Get current database version
            int currentDbVersion = getCurrentVersion(conn);
            plugin.getLogger().info("Current database schema version: " + currentDbVersion);

            // Check if migration is needed
            if (currentDbVersion >= CURRENT_VERSION) {
                plugin.getLogger().info("Database schema is up to date!");
                return;
            }

            plugin.getLogger().info("Starting database migration from version " +
                currentDbVersion + " to " + CURRENT_VERSION);

            // Execute pending migrations
            for (Migration migration : migrations) {
                if (migration.getVersion() > currentDbVersion) {
                    executeMigration(conn, migration);
                }
            }

            plugin.getLogger().info("Database migration completed successfully!");

        } catch (SQLException e) {
            plugin.getLogger().severe("Database migration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to migrate database", e);
        }
    }

    /**
     * Execute a single migration safely
     */
    private void executeMigration(Connection conn, Migration migration) throws SQLException {
        plugin.getLogger().info("Executing migration to version " + migration.getVersion() +
            ": " + migration.getDescription());

        // Create backup tables before migration
        createBackupTables(conn, migration.getVersion());

        // Disable auto-commit for transaction support
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // Execute migration
            migration.migrate(conn);

            // Validate data integrity
            if (validateDataIntegrity(conn)) {
                // Update schema version
                updateSchemaVersion(conn, migration.getVersion());

                // Commit transaction
                conn.commit();

                plugin.getLogger().info("Migration to version " + migration.getVersion() + " completed successfully");

                // Drop backup tables after successful migration
                dropBackupTables(conn, migration.getVersion());
            } else {
                throw new SQLException("Data integrity validation failed");
            }

        } catch (SQLException e) {
            // Rollback on error
            plugin.getLogger().severe("Migration to version " + migration.getVersion() + " failed: " + e.getMessage());
            conn.rollback();

            // Attempt to restore from backup
            restoreFromBackup(conn, migration.getVersion());

            throw e;
        } finally {
            // Restore auto-commit
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * Create schema version tracking table
     */
    private void createSchemaVersionTable(Connection conn) throws SQLException {
        String sql =
            "CREATE TABLE IF NOT EXISTS chestlock_schema_version (" +
            "  id INT PRIMARY KEY DEFAULT 1," +
            "  version INT NOT NULL," +
            "  migrated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  CHECK (id = 1)" + // Ensure only one row exists
            ") ENGINE=InnoDB";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);

            // Insert initial version if not exists
            stmt.executeUpdate(
                "INSERT IGNORE INTO chestlock_schema_version (id, version) VALUES (1, 0)"
            );
        }
    }

    /**
     * Get current database schema version
     */
    private int getCurrentVersion(Connection conn) throws SQLException {
        String sql = "SELECT version FROM chestlock_schema_version WHERE id = 1";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("version");
            }
        }
        return 0;
    }

    /**
     * Update schema version after successful migration
     */
    private void updateSchemaVersion(Connection conn, int version) throws SQLException {
        String sql = "UPDATE chestlock_schema_version SET version = ?, migrated_at = CURRENT_TIMESTAMP WHERE id = 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, version);
            stmt.executeUpdate();
        }
    }

    /**
     * Create backup tables before migration
     */
    private void createBackupTables(Connection conn, int version) {
        try (Statement stmt = conn.createStatement()) {
            String backupSuffix = "_backup_v" + version;

            // Check if tables exist before backing up
            if (tableExists(conn, "chestlock_protections")) {
                stmt.executeUpdate("DROP TABLE IF EXISTS chestlock_protections" + backupSuffix);
                stmt.executeUpdate("CREATE TABLE chestlock_protections" + backupSuffix +
                    " AS SELECT * FROM chestlock_protections");
                plugin.getLogger().info("Created backup: chestlock_protections" + backupSuffix);
            }

            if (tableExists(conn, "chestlock_friends")) {
                stmt.executeUpdate("DROP TABLE IF EXISTS chestlock_friends" + backupSuffix);
                stmt.executeUpdate("CREATE TABLE chestlock_friends" + backupSuffix +
                    " AS SELECT * FROM chestlock_friends");
                plugin.getLogger().info("Created backup: chestlock_friends" + backupSuffix);
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to create backup tables: " + e.getMessage());
        }
    }

    /**
     * Drop backup tables after successful migration
     */
    private void dropBackupTables(Connection conn, int version) {
        try (Statement stmt = conn.createStatement()) {
            String backupSuffix = "_backup_v" + version;

            stmt.executeUpdate("DROP TABLE IF EXISTS chestlock_friends" + backupSuffix);
            stmt.executeUpdate("DROP TABLE IF EXISTS chestlock_protections" + backupSuffix);

            plugin.getLogger().info("Dropped backup tables for version " + version);

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to drop backup tables: " + e.getMessage());
        }
    }

    /**
     * Restore from backup if migration fails
     */
    private void restoreFromBackup(Connection conn, int version) {
        plugin.getLogger().warning("Attempting to restore from backup...");

        try (Statement stmt = conn.createStatement()) {
            String backupSuffix = "_backup_v" + version;

            // Drop corrupted tables
            stmt.executeUpdate("DROP TABLE IF EXISTS chestlock_friends");
            stmt.executeUpdate("DROP TABLE IF EXISTS chestlock_protections");

            // Restore from backup
            if (tableExists(conn, "chestlock_protections" + backupSuffix)) {
                stmt.executeUpdate("CREATE TABLE chestlock_protections AS SELECT * FROM chestlock_protections" + backupSuffix);
                plugin.getLogger().info("Restored chestlock_protections from backup");
            }

            if (tableExists(conn, "chestlock_friends" + backupSuffix)) {
                stmt.executeUpdate("CREATE TABLE chestlock_friends AS SELECT * FROM chestlock_friends" + backupSuffix);
                plugin.getLogger().info("Restored chestlock_friends from backup");
            }

            plugin.getLogger().info("Database restored from backup successfully");

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to restore from backup: " + e.getMessage());
        }
    }

    /**
     * Validate data integrity after migration
     */
    private boolean validateDataIntegrity(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Check if required tables exist
            if (!tableExists(conn, "chestlock_protections")) {
                plugin.getLogger().severe("Validation failed: chestlock_protections table missing");
                return false;
            }

            if (!tableExists(conn, "chestlock_friends")) {
                plugin.getLogger().severe("Validation failed: chestlock_friends table missing");
                return false;
            }

            // Check for orphaned friends (friends without protection)
            String orphanCheck =
                "SELECT COUNT(*) as orphan_count FROM chestlock_friends f " +
                "LEFT JOIN chestlock_protections p ON f.protection_id = p.id " +
                "WHERE p.id IS NULL";

            try (ResultSet rs = stmt.executeQuery(orphanCheck)) {
                if (rs.next() && rs.getInt("orphan_count") > 0) {
                    plugin.getLogger().warning("Found orphaned friend records, but this is acceptable");
                    // Not a critical error, just a warning
                }
            }

            // Validation passed
            return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Data integrity validation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a table exists
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        String query =
            "SELECT COUNT(*) FROM information_schema.TABLES " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Get current schema version (for external use)
     */
    public static int getCurrentSchemaVersion() {
        return CURRENT_VERSION;
    }
}
