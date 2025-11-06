package com.chestlock.gui;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import com.chestlock.model.BlockProtection.FriendPermission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Handles clicks in friend menu
 */
public class FriendMenuClickListener implements Listener {

    private final ChestLock plugin;
    private final Player player;
    private final Block block;
    private final Inventory inventory;

    public FriendMenuClickListener(ChestLock plugin, Player player, Block block, Inventory inventory) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        // Back button
        if (slot == 45) {
            player.closeInventory();
            new LockMenuGUI(plugin, player, block).open();
            return;
        }

        // Add friend button
        if (slot == 49) {
            player.closeInventory();
            new OnlinePlayerSelectorGUI(plugin, player, block).open();
            return;
        }

        // Friend skull clicked
        if (slot < 45 && event.getCurrentItem().getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
            OfflinePlayer friend = meta.getOwningPlayer();
            if (friend == null) return;

            BlockProtection protection = plugin.getDataHandler().getProtection(block);
            if (protection == null) return;

            UUID friendUuid = friend.getUniqueId();

            if (event.getClick() == ClickType.LEFT) {
                // Toggle permission
                FriendPermission currentPerm = protection.getFriendPermission(friendUuid);
                if (currentPerm != null) {
                    FriendPermission newPerm = currentPerm == FriendPermission.READ_ONLY ?
                            FriendPermission.READ_WRITE : FriendPermission.READ_ONLY;
                    plugin.getDataHandler().addFriend(block, friendUuid, newPerm);
                    player.sendMessage(plugin.getMessageWithoutPrefix("prefix") +
                            " §e권한이 변경되었습니다.");
                    player.closeInventory();
                    new FriendMenuGUI(plugin, player, block).open();
                }
            } else if (event.getClick() == ClickType.RIGHT) {
                // Remove friend
                plugin.getDataHandler().removeFriend(block, friendUuid);
                player.sendMessage(plugin.getMessage("friend-removed")
                        .replace("%player%", friend.getName() != null ? friend.getName() : "Unknown"));
                player.closeInventory();
                new FriendMenuGUI(plugin, player, block).open();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;

        HandlerList.unregisterAll(this);
    }
}
