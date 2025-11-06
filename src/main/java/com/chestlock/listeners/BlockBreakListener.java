package com.chestlock.listeners;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Prevents breaking protected blocks
 */
public class BlockBreakListener implements Listener {

    private final ChestLock plugin;

    public BlockBreakListener(ChestLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!plugin.isLockable(block.getType())) return;

        BlockProtection protection = plugin.getDataHandler().getProtection(block);
        if (protection == null) return;

        // Bypass permission
        if (player.hasPermission("chestlock.bypass") || player.hasPermission("chestlock.admin")) {
            plugin.getDataHandler().unlockBlock(block);
            return;
        }

        // Only owner can break
        if (!protection.getOwner().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("not-owner"));
            return;
        }

        // Remove protection when broken
        plugin.getDataHandler().unlockBlock(block);
    }
}
