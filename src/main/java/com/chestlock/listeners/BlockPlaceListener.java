package com.chestlock.listeners;

import com.chestlock.ChestLock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Optionally auto-lock blocks when placed
 */
public class BlockPlaceListener implements Listener {

    private final ChestLock plugin;

    public BlockPlaceListener(ChestLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!plugin.isLockable(block.getType())) return;

        // Auto-lock is optional - for now just allow placement
        // Future: Add config option for auto-lock on place
    }
}
