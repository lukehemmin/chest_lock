package com.chestlock.gui;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import com.chestlock.model.BlockProtection.FriendPermission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

/**
 * GUI to select online players to add as friends
 */
public class OnlinePlayerSelectorGUI implements Listener {

    private final ChestLock plugin;
    private final Player player;
    private final Block block;
    private final Inventory inventory;

    public OnlinePlayerSelectorGUI(ChestLock plugin, Player player, Block block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6플레이어 선택");

        setupInventory();
    }

    private void setupInventory() {
        // Back button
        setItem(49, Material.ARROW, "§c뒤로 가기", "§7친구 메뉴로 돌아갑니다.");

        // Show online players
        int slot = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            if (onlinePlayer.equals(player)) continue; // Skip self

            BlockProtection protection = plugin.getDataHandler().getProtection(block);
            if (protection != null && protection.isFriend(onlinePlayer.getUniqueId())) {
                continue; // Skip already added friends
            }

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(onlinePlayer);
            meta.setDisplayName("§e" + onlinePlayer.getName());
            meta.setLore(Arrays.asList(
                    "§7클릭하여 친구로 추가",
                    "§7기본 권한: §e읽기 전용"
            ));
            skull.setItemMeta(meta);

            inventory.setItem(slot, skull);
            slot++;
        }

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 49; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private void setItem(int slot, Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    public void open() {
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        // Back button
        if (slot == 49) {
            player.closeInventory();
            new FriendMenuGUI(plugin, player, block).open();
            return;
        }

        // Player skull clicked
        if (slot < 45 && event.getCurrentItem().getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
            Player selectedPlayer = meta.getOwningPlayer() instanceof Player ? (Player) meta.getOwningPlayer() : null;

            if (selectedPlayer != null) {
                plugin.getDataHandler().addFriend(block, selectedPlayer.getUniqueId(), FriendPermission.READ_ONLY);
                player.sendMessage(plugin.getMessage("friend-added")
                        .replace("%player%", selectedPlayer.getName()));
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
