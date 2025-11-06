package com.chestlock.data.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Example migration for future updates - Version 2
 * This is a placeholder showing how to safely add new columns
 */
public class MigrationV2 implements Migration {

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        // Example: Add a new column to protections table
        // This is commented out as it's not needed yet, but shows the pattern

        /*
        try (Statement stmt = connection.createStatement()) {
            // Check if column exists before adding
            if (!columnExists(connection, "chestlock_protections", "block_type")) {
                // Add new column safely
                stmt.executeUpdate(
                    "ALTER TABLE chestlock_protections " +
                    "ADD COLUMN block_type VARCHAR(50) DEFAULT NULL"
                );

                // Add index if needed
                stmt.executeUpdate(
                    "ALTER TABLE chestlock_protections " +
                    "ADD INDEX idx_block_type (block_type)"
                );
            }
        }
        */
    }

    @Override
    public String getDescription() {
        return "Example migration - add block_type column (placeholder for future updates)";
    }

    /**
     * Helper method to check if a column exists
     */
    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String query = String.format(
            "SELECT COUNT(*) FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "AND TABLE_NAME = '%s' " +
            "AND COLUMN_NAME = '%s'",
            tableName, columnName
        );

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
