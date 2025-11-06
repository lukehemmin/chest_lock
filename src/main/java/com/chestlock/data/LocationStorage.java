package com.chestlock.data;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import com.chestlock.model.BlockProtection.FriendPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores protection data for non-tile-entity blocks using location as key
 */
public class LocationStorage {
    private final ChestLock plugin;
    private final Map<String, BlockProtection> protectedBlocks;
    private final File dataFile;

    public LocationStorage(ChestLock plugin) {
        this.plugin = plugin;
        this.protectedBlocks = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "protections.yml");
    }

    /**
     * Convert location to string key
     */
    private String locationToKey(Location loc) {
        return String.format("%s,%d,%d,%d",
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ());
    }

    /**
     * Parse location from string key
     */
    private Location keyToLocation(String key) {
        String[] parts = key.split(",");
        if (parts.length != 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Save protection data for a location
     */
    public void save(Location location, BlockProtection protection) {
        String key = locationToKey(location);
        protectedBlocks.put(key, protection);
    }

    /**
     * Get protection data for a location
     */
    public BlockProtection get(Location location) {
        String key = locationToKey(location);
        return protectedBlocks.get(key);
    }

    /**
     * Remove protection data for a location
     */
    public void remove(Location location) {
        String key = locationToKey(location);
        protectedBlocks.remove(key);
    }

    /**
     * Save all data to file
     */
    public void saveToFile() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, BlockProtection> entry : protectedBlocks.entrySet()) {
            String key = entry.getKey();
            BlockProtection protection = entry.getValue();

            ConfigurationSection section = config.createSection(key);
            section.set("owner", protection.getOwner().toString());
            section.set("allowHopper", protection.isAllowHopper());
            section.set("allowRedstone", protection.isAllowRedstone());

            // Save friends
            List<String> friendsList = new ArrayList<>();
            for (Map.Entry<UUID, FriendPermission> friend : protection.getFriends().entrySet()) {
                friendsList.add(friend.getKey().toString() + ":" + friend.getValue().name());
            }
            section.set("friends", friendsList);
        }

        try {
            config.save(dataFile);
            plugin.getLogger().info("Saved " + protectedBlocks.size() + " protected blocks to file");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save protections: " + e.getMessage());
        }
    }

    /**
     * Load all data from file
     */
    public void loadFromFile() {
        if (!dataFile.exists()) {
            plugin.getLogger().info("No protections file found, starting fresh");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        protectedBlocks.clear();

        int loaded = 0;
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            try {
                String ownerStr = section.getString("owner");
                if (ownerStr == null) continue;

                UUID owner = UUID.fromString(ownerStr);
                BlockProtection protection = new BlockProtection(owner);

                protection.setAllowHopper(section.getBoolean("allowHopper", false));
                protection.setAllowRedstone(section.getBoolean("allowRedstone", true));

                // Load friends
                List<String> friendsList = section.getStringList("friends");
                for (String friendEntry : friendsList) {
                    String[] parts = friendEntry.split(":");
                    if (parts.length == 2) {
                        UUID friendUuid = UUID.fromString(parts[0]);
                        FriendPermission perm = FriendPermission.valueOf(parts[1]);
                        protection.addFriend(friendUuid, perm);
                    }
                }

                protectedBlocks.put(key, protection);
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load protection for " + key + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " protected blocks from file");
    }

    /**
     * Get all protected locations
     */
    public Set<Location> getProtectedLocations() {
        Set<Location> locations = new HashSet<>();
        for (String key : protectedBlocks.keySet()) {
            Location loc = keyToLocation(key);
            if (loc != null) {
                locations.add(loc);
            }
        }
        return locations;
    }
}
