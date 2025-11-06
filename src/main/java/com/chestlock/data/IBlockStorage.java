package com.chestlock.data;

import com.chestlock.model.BlockProtection;
import org.bukkit.Location;

import java.util.Set;

/**
 * Interface for block protection storage
 * Implementations: YamlStorage, MySQLStorage
 */
public interface IBlockStorage {

    /**
     * Save protection data for a location
     */
    void save(Location location, BlockProtection protection);

    /**
     * Get protection data for a location
     */
    BlockProtection get(Location location);

    /**
     * Remove protection data for a location
     */
    void remove(Location location);

    /**
     * Save all data (for file-based storage)
     */
    void saveAll();

    /**
     * Load all data (for file-based storage)
     */
    void loadAll();

    /**
     * Get all protected locations
     */
    Set<Location> getProtectedLocations();

    /**
     * Close/cleanup resources
     */
    void close();
}
