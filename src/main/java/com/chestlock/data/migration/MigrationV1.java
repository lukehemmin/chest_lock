package com.chestlock.data.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initial database schema - Version 1
 */
public class MigrationV1 implements Migration {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create protections table
            stmt.executeUpdate(
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
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );

            // Create friends table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS chestlock_friends (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  protection_id INT NOT NULL," +
                "  friend_uuid VARCHAR(36) NOT NULL," +
                "  permission VARCHAR(20) NOT NULL," +
                "  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE KEY unique_friend (protection_id, friend_uuid)," +
                "  FOREIGN KEY (protection_id) REFERENCES chestlock_protections(id) ON DELETE CASCADE," +
                "  INDEX idx_friend_uuid (friend_uuid)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );
        }
    }

    @Override
    public String getDescription() {
        return "Create initial schema with protections and friends tables";
    }
}
