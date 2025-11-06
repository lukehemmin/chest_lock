package com.chestlock.listeners;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Handles inventory opening for protected blocks
 */
public class InventoryListener implements Listener {

    private final ChestLock plugin;

    public InventoryListener(ChestLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();

        if (event.getInventory().getLocation() == null) return;

        Block block = event.getInventory().getLocation().getBlock();
        if (!plugin.isLockable(block.getType())) return;

        BlockProtection protection = plugin.getDataHandler().getProtection(block);
        if (protection == null) return;

        // Bypass permission
        if (player.hasPermission("chestlock.bypass")) {
            return;
        }

        // Check access
        if (!protection.canAccess(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("no-permission"));
        }
    }
}
