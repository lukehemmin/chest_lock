package com.chestlock.listeners;

import com.chestlock.ChestLock;
import com.chestlock.gui.LockMenuGUI;
import com.chestlock.model.BlockProtection;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles player interactions with blocks
 */
public class BlockInteractListener implements Listener {

    private final ChestLock plugin;

    public BlockInteractListener(ChestLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!plugin.isLockable(block.getType())) return;

        Player player = event.getPlayer();

        // Shift + Right Click = Open lock menu
        if (player.isSneaking() && event.getItem() == null) {
            event.setCancelled(true);

            if (!player.hasPermission("chestlock.lock")) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return;
            }

            // Open lock menu
            new LockMenuGUI(plugin, player, block).open();
            return;
        }

        // Normal right click = Check access
        BlockProtection protection = plugin.getDataHandler().getProtection(block);
        if (protection == null) {
            // Not protected, show hint
            if (!plugin.getDataHandler().isProtected(block)) {
                String hint = plugin.getMessageWithoutPrefix("lock-hint");
                if (!hint.isEmpty()) {
                    player.sendActionBar(hint);
                }
            }
            return;
        }

        // Check if player can access
        if (player.hasPermission("chestlock.bypass")) {
            return; // Bypass permission
        }

        if (!protection.canAccess(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("no-permission"));
        }
    }
}
