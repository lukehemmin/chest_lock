package com.chestlock.model;

import java.util.*;

/**
 * Represents a protected block with owner and friends
 */
public class BlockProtection {
    private final UUID owner;
    private final Map<UUID, FriendPermission> friends;
    private boolean allowHopper;
    private boolean allowRedstone;

    public BlockProtection(UUID owner) {
        this.owner = owner;
        this.friends = new HashMap<>();
        this.allowHopper = false;
        this.allowRedstone = true;
    }

    public UUID getOwner() {
        return owner;
    }

    public Map<UUID, FriendPermission> getFriends() {
        return new HashMap<>(friends);
    }

    public void addFriend(UUID friendUuid, FriendPermission permission) {
        friends.put(friendUuid, permission);
    }

    public void removeFriend(UUID friendUuid) {
        friends.remove(friendUuid);
    }

    public boolean isFriend(UUID uuid) {
        return friends.containsKey(uuid);
    }

    public FriendPermission getFriendPermission(UUID uuid) {
        return friends.get(uuid);
    }

    public boolean canAccess(UUID uuid) {
        if (owner.equals(uuid)) return true;
        FriendPermission perm = friends.get(uuid);
        return perm != null && perm.canRead();
    }

    public boolean canModify(UUID uuid) {
        if (owner.equals(uuid)) return true;
        FriendPermission perm = friends.get(uuid);
        return perm != null && perm.canWrite();
    }

    public boolean isAllowHopper() {
        return allowHopper;
    }

    public void setAllowHopper(boolean allowHopper) {
        this.allowHopper = allowHopper;
    }

    public boolean isAllowRedstone() {
        return allowRedstone;
    }

    public void setAllowRedstone(boolean allowRedstone) {
        this.allowRedstone = allowRedstone;
    }

    public enum FriendPermission {
        READ_ONLY(true, false),
        READ_WRITE(true, true);

        private final boolean read;
        private final boolean write;

        FriendPermission(boolean read, boolean write) {
            this.read = read;
            this.write = write;
        }

        public boolean canRead() {
            return read;
        }

        public boolean canWrite() {
            return write;
        }
    }
}
