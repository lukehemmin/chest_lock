package com.chestlock.listeners;

import com.chestlock.ChestLock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

/**
 * Prevents explosions from destroying protected blocks
 */
public class ExplosionListener implements Listener {

    private final ChestLock plugin;

    public ExplosionListener(ChestLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (plugin.isLockable(block.getType()) && plugin.getDataHandler().isProtected(block)) {
                iterator.remove();
            }
        }
    }
}
