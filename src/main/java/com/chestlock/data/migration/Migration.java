package com.chestlock.data.migration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for database schema migrations
 */
public interface Migration {

    /**
     * Get the target version of this migration
     */
    int getVersion();

    /**
     * Execute the migration
     * @param connection Database connection
     * @throws SQLException if migration fails
     */
    void migrate(Connection connection) throws SQLException;

    /**
     * Get description of this migration
     */
    String getDescription();
}
