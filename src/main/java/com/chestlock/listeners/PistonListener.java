package com.chestlock.listeners;

import com.chestlock.ChestLock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

/**
 * Prevents pistons from moving protected blocks
 */
public class PistonListener implements Listener {

    private final ChestLock plugin;

    public PistonListener(ChestLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.isLockable(block.getType()) && plugin.getDataHandler().isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.isLockable(block.getType()) && plugin.getDataHandler().isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
