package com.chestlock.gui;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Handles clicks in the lock menu GUI
 */
public class LockMenuClickListener implements Listener {

    private final ChestLock plugin;
    private final Player player;
    private final Block block;
    private final Inventory inventory;

    public LockMenuClickListener(ChestLock plugin, Player player, Block block, Inventory inventory) {
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
        BlockProtection protection = plugin.getDataHandler().getProtection(block);

        if (protection == null) {
            // Not locked - lock it
            if (slot == 13) {
                plugin.getDataHandler().lockBlock(block, player.getUniqueId());
                player.sendMessage(plugin.getMessage("locked"));
                player.closeInventory();
            }
        } else if (protection.getOwner().equals(player.getUniqueId())) {
            // Owner options
            switch (slot) {
                case 10: // Unlock
                    plugin.getDataHandler().unlockBlock(block);
                    player.sendMessage(plugin.getMessage("unlocked"));
                    player.closeInventory();
                    break;

                case 12: // Friends management
                    new FriendMenuGUI(plugin, player, block).open();
                    break;

                case 14: // Hopper toggle
                    boolean newHopperState = !protection.isAllowHopper();
                    plugin.getDataHandler().updateSettings(block, newHopperState, protection.isAllowRedstone());
                    player.sendMessage(plugin.getMessageWithoutPrefix("prefix") +
                            " §e호퍼가 " + (newHopperState ? "§a허용" : "§c차단") + "§e되었습니다.");
                    player.closeInventory();
                    new LockMenuGUI(plugin, player, block).open();
                    break;

                case 16: // Redstone toggle
                    boolean newRedstoneState = !protection.isAllowRedstone();
                    plugin.getDataHandler().updateSettings(block, protection.isAllowHopper(), newRedstoneState);
                    player.sendMessage(plugin.getMessageWithoutPrefix("prefix") +
                            " §e레드스톤이 " + (newRedstoneState ? "§a허용" : "§c차단") + "§e되었습니다.");
                    player.closeInventory();
                    new LockMenuGUI(plugin, player, block).open();
                    break;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;

        // Unregister this listener when inventory is closed
        HandlerList.unregisterAll(this);
    }
}
