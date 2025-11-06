package com.chestlock.data;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import com.chestlock.model.BlockProtection.FriendPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MySQL/MariaDB-based storage for block protections
 */
public class MySQLStorage implements IBlockStorage {

    private final ChestLock plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, BlockProtection> cache;

    public MySQLStorage(ChestLock plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.cache = new ConcurrentHashMap<>();
    }

    private String locationToKey(Location loc) {
        return String.format("%s,%d,%d,%d",
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ());
    }

    @Override
    public void save(Location location, BlockProtection protection) {
        // Update cache
        cache.put(locationToKey(location), protection);

        // Save to database asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = databaseManager.getConnection()) {
                // Insert or update protection
                String sql = "INSERT INTO chestlock_protections (world, x, y, z, owner, allow_hopper, allow_redstone) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE owner=VALUES(owner), allow_hopper=VALUES(allow_hopper), " +
                        "allow_redstone=VALUES(allow_redstone), updated_at=CURRENT_TIMESTAMP";

                int protectionId;
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    stmt.setString(5, protection.getOwner().toString());
                    stmt.setBoolean(6, protection.isAllowHopper());
                    stmt.setBoolean(7, protection.isAllowRedstone());
                    stmt.executeUpdate();

                    // Get protection ID
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            protectionId = rs.getInt(1);
                        } else {
                            // If not generated (ON DUPLICATE KEY UPDATE), fetch it
                            protectionId = getProtectionId(conn, location);
                        }
                    }
                }

                // Delete old friends
                String deleteFriendsSql = "DELETE FROM chestlock_friends WHERE protection_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteFriendsSql)) {
                    stmt.setInt(1, protectionId);
                    stmt.executeUpdate();
                }

                // Insert new friends
                if (!protection.getFriends().isEmpty()) {
                    String insertFriendSql = "INSERT INTO chestlock_friends (protection_id, friend_uuid, permission) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertFriendSql)) {
                        for (Map.Entry<UUID, FriendPermission> entry : protection.getFriends().entrySet()) {
                            stmt.setInt(1, protectionId);
                            stmt.setString(2, entry.getKey().toString());
                            stmt.setString(3, entry.getValue().name());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save protection to MySQL: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public BlockProtection get(Location location) {
        String key = locationToKey(location);

        // Check cache first
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        // Load from database
        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT id, owner, allow_hopper, allow_redstone FROM chestlock_protections " +
                    "WHERE world = ? AND x = ? AND y = ? AND z = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, location.getWorld().getName());
                stmt.setInt(2, location.getBlockX());
                stmt.setInt(3, location.getBlockY());
                stmt.setInt(4, location.getBlockZ());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int protectionId = rs.getInt("id");
                        UUID owner = UUID.fromString(rs.getString("owner"));
                        boolean allowHopper = rs.getBoolean("allow_hopper");
                        boolean allowRedstone = rs.getBoolean("allow_redstone");

                        BlockProtection protection = new BlockProtection(owner);
                        protection.setAllowHopper(allowHopper);
                        protection.setAllowRedstone(allowRedstone);

                        // Load friends
                        loadFriends(conn, protectionId, protection);

                        // Cache it
                        cache.put(key, protection);
                        return protection;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load protection from MySQL: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void remove(Location location) {
        // Remove from cache
        cache.remove(locationToKey(location));

        // Remove from database asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = databaseManager.getConnection()) {
                String sql = "DELETE FROM chestlock_protections WHERE world = ? AND x = ? AND y = ? AND z = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location.getWorld().getName());
                    stmt.setInt(2, location.getBlockX());
                    stmt.setInt(3, location.getBlockY());
                    stmt.setInt(4, location.getBlockZ());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove protection from MySQL: " + e.getMessage());
            }
        });
    }

    @Override
    public void saveAll() {
        // MySQL saves immediately, no batch operation needed
        plugin.getLogger().info("MySQL storage: Data is saved automatically");
    }

    @Override
    public void loadAll() {
        cache.clear();

        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT id, world, x, y, z, owner, allow_hopper, allow_redstone FROM chestlock_protections";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                int loaded = 0;
                while (rs.next()) {
                    try {
                        int protectionId = rs.getInt("id");
                        String worldName = rs.getString("world");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        UUID owner = UUID.fromString(rs.getString("owner"));
                        boolean allowHopper = rs.getBoolean("allow_hopper");
                        boolean allowRedstone = rs.getBoolean("allow_redstone");

                        World world = Bukkit.getWorld(worldName);
                        if (world == null) continue;

                        Location location = new Location(world, x, y, z);
                        String key = locationToKey(location);

                        BlockProtection protection = new BlockProtection(owner);
                        protection.setAllowHopper(allowHopper);
                        protection.setAllowRedstone(allowRedstone);

                        // Load friends
                        loadFriends(conn, protectionId, protection);

                        cache.put(key, protection);
                        loaded++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load protection: " + e.getMessage());
                    }
                }

                plugin.getLogger().info("Loaded " + loaded + " protected blocks from MySQL database");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load protections from MySQL: " + e.getMessage());
        }
    }

    @Override
    public Set<Location> getProtectedLocations() {
        Set<Location> locations = new HashSet<>();

        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT world, x, y, z FROM chestlock_protections";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String worldName = rs.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        locations.add(new Location(world, x, y, z));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get protected locations from MySQL: " + e.getMessage());
        }

        return locations;
    }

    @Override
    public void close() {
        cache.clear();
        databaseManager.disconnect();
    }

    // Helper methods

    private int getProtectionId(Connection conn, Location location) throws SQLException {
        String sql = "SELECT id FROM chestlock_protections WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setInt(2, location.getBlockX());
            stmt.setInt(3, location.getBlockY());
            stmt.setInt(4, location.getBlockZ());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("Failed to get protection ID");
    }

    private void loadFriends(Connection conn, int protectionId, BlockProtection protection) throws SQLException {
        String sql = "SELECT friend_uuid, permission FROM chestlock_friends WHERE protection_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, protectionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID friendUuid = UUID.fromString(rs.getString("friend_uuid"));
                    FriendPermission permission = FriendPermission.valueOf(rs.getString("permission"));
                    protection.addFriend(friendUuid, permission);
                }
            }
        }
    }
}
