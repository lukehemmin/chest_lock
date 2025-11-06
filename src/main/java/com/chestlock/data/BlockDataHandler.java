package com.chestlock.data;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import com.chestlock.model.BlockProtection.FriendPermission;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Handles block protection data using PersistentDataContainer for TileEntities
 * and location-based storage for regular blocks (doors, trapdoors, etc.)
 */
public class BlockDataHandler {
    private final ChestLock plugin;
    private final NamespacedKey ownerKey;
    private final NamespacedKey friendsKey;
    private final NamespacedKey hopperKey;
    private final NamespacedKey redstoneKey;

    // Location-based storage for non-tile-entity blocks
    private final LocationStorage locationStorage;

    public BlockDataHandler(ChestLock plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "owner");
        this.friendsKey = new NamespacedKey(plugin, "friends");
        this.hopperKey = new NamespacedKey(plugin, "hopper");
        this.redstoneKey = new NamespacedKey(plugin, "redstone");
        this.locationStorage = new LocationStorage(plugin);
    }

    /**
     * Check if a block is a TileEntity (can use PersistentDataContainer)
     */
    public boolean isTileEntity(Block block) {
        BlockState state = block.getState();
        return state instanceof TileState;
    }

    /**
     * Lock a block with protection
     */
    public void lockBlock(Block block, UUID owner) {
        if (isTileEntity(block)) {
            lockTileEntity(block, owner);
        } else {
            lockRegularBlock(block, owner);
        }
    }

    /**
     * Unlock a block (remove protection)
     */
    public void unlockBlock(Block block) {
        if (isTileEntity(block)) {
            unlockTileEntity(block);
        } else {
            unlockRegularBlock(block);
        }
    }

    /**
     * Get protection data for a block
     */
    public BlockProtection getProtection(Block block) {
        if (isTileEntity(block)) {
            return getProtectionFromTileEntity(block);
        } else {
            return getProtectionFromLocation(block.getLocation());
        }
    }

    /**
     * Check if a block is protected
     */
    public boolean isProtected(Block block) {
        return getProtection(block) != null;
    }

    /**
     * Add a friend to a protected block
     */
    public void addFriend(Block block, UUID friendUuid, FriendPermission permission) {
        BlockProtection protection = getProtection(block);
        if (protection != null) {
            protection.addFriend(friendUuid, permission);
            saveProtection(block, protection);
        }
    }

    /**
     * Remove a friend from a protected block
     */
    public void removeFriend(Block block, UUID friendUuid) {
        BlockProtection protection = getProtection(block);
        if (protection != null) {
            protection.removeFriend(friendUuid);
            saveProtection(block, protection);
        }
    }

    /**
     * Update protection settings
     */
    public void updateSettings(Block block, boolean allowHopper, boolean allowRedstone) {
        BlockProtection protection = getProtection(block);
        if (protection != null) {
            protection.setAllowHopper(allowHopper);
            protection.setAllowRedstone(allowRedstone);
            saveProtection(block, protection);
        }
    }

    /**
     * Save protection data
     */
    private void saveProtection(Block block, BlockProtection protection) {
        if (isTileEntity(block)) {
            saveProtectionToTileEntity(block, protection);
        } else {
            saveProtectionToLocation(block.getLocation(), protection);
        }
    }

    // === TileEntity methods (PersistentDataContainer) ===

    private void lockTileEntity(Block block, UUID owner) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();

            pdc.set(ownerKey, PersistentDataType.STRING, owner.toString());
            pdc.set(friendsKey, PersistentDataType.STRING, "");
            pdc.set(hopperKey, PersistentDataType.BYTE, (byte) 0);
            pdc.set(redstoneKey, PersistentDataType.BYTE, (byte) 1);

            tileState.update();
        }
    }

    private void unlockTileEntity(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();

            pdc.remove(ownerKey);
            pdc.remove(friendsKey);
            pdc.remove(hopperKey);
            pdc.remove(redstoneKey);

            tileState.update();
        }
    }

    private BlockProtection getProtectionFromTileEntity(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();

            if (!pdc.has(ownerKey, PersistentDataType.STRING)) {
                return null;
            }

            String ownerStr = pdc.get(ownerKey, PersistentDataType.STRING);
            if (ownerStr == null) return null;

            UUID owner = UUID.fromString(ownerStr);
            BlockProtection protection = new BlockProtection(owner);

            // Load friends
            String friendsStr = pdc.get(friendsKey, PersistentDataType.STRING);
            if (friendsStr != null && !friendsStr.isEmpty()) {
                String[] friendEntries = friendsStr.split(";");
                for (String entry : friendEntries) {
                    if (entry.isEmpty()) continue;
                    String[] parts = entry.split(":");
                    if (parts.length == 2) {
                        UUID friendUuid = UUID.fromString(parts[0]);
                        FriendPermission perm = FriendPermission.valueOf(parts[1]);
                        protection.addFriend(friendUuid, perm);
                    }
                }
            }

            // Load settings
            Byte hopperByte = pdc.get(hopperKey, PersistentDataType.BYTE);
            if (hopperByte != null) {
                protection.setAllowHopper(hopperByte == 1);
            }

            Byte redstoneByte = pdc.get(redstoneKey, PersistentDataType.BYTE);
            if (redstoneByte != null) {
                protection.setAllowRedstone(redstoneByte == 1);
            }

            return protection;
        }
        return null;
    }

    private void saveProtectionToTileEntity(Block block, BlockProtection protection) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();

            pdc.set(ownerKey, PersistentDataType.STRING, protection.getOwner().toString());

            // Save friends as "uuid:permission;uuid:permission"
            StringBuilder friendsBuilder = new StringBuilder();
            for (Map.Entry<UUID, FriendPermission> entry : protection.getFriends().entrySet()) {
                if (friendsBuilder.length() > 0) {
                    friendsBuilder.append(";");
                }
                friendsBuilder.append(entry.getKey().toString())
                        .append(":")
                        .append(entry.getValue().name());
            }
            pdc.set(friendsKey, PersistentDataType.STRING, friendsBuilder.toString());

            pdc.set(hopperKey, PersistentDataType.BYTE, (byte) (protection.isAllowHopper() ? 1 : 0));
            pdc.set(redstoneKey, PersistentDataType.BYTE, (byte) (protection.isAllowRedstone() ? 1 : 0));

            tileState.update();
        }
    }

    // === Location-based methods (for doors, trapdoors, etc.) ===

    private void lockRegularBlock(Block block, UUID owner) {
        BlockProtection protection = new BlockProtection(owner);
        locationStorage.save(block.getLocation(), protection);
    }

    private void unlockRegularBlock(Block block) {
        locationStorage.remove(block.getLocation());
    }

    private BlockProtection getProtectionFromLocation(Location location) {
        return locationStorage.get(location);
    }

    private void saveProtectionToLocation(Location location, BlockProtection protection) {
        locationStorage.save(location, protection);
    }

    /**
     * Save all location-based protections to disk
     */
    public void saveAll() {
        locationStorage.saveToFile();
    }

    /**
     * Load all location-based protections from disk
     */
    public void loadAll() {
        locationStorage.loadFromFile();
    }
}
