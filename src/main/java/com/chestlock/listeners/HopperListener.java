package com.chestlock.listeners;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

/**
 * Prevents hoppers from accessing protected blocks
 */
public class HopperListener implements Listener {

    private final ChestLock plugin;

    public HopperListener(ChestLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent event) {
        // Check source container
        if (event.getSource().getLocation() != null) {
            Block sourceBlock = event.getSource().getLocation().getBlock();
            if (plugin.isLockable(sourceBlock.getType())) {
                BlockProtection protection = plugin.getDataHandler().getProtection(sourceBlock);
                if (protection != null && !protection.isAllowHopper()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Check destination container
        if (event.getDestination().getLocation() != null) {
            Block destBlock = event.getDestination().getLocation().getBlock();
            if (plugin.isLockable(destBlock.getType())) {
                BlockProtection protection = plugin.getDataHandler().getProtection(destBlock);
                if (protection != null && !protection.isAllowHopper()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
