package com.chestlock.gui;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import com.chestlock.model.BlockProtection.FriendPermission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Friend management GUI
 */
public class FriendMenuGUI {

    private final ChestLock plugin;
    private final Player player;
    private final Block block;
    private final Inventory inventory;

    public FriendMenuGUI(ChestLock plugin, Player player, Block block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6친구 관리");

        setupInventory();
    }

    private void setupInventory() {
        BlockProtection protection = plugin.getDataHandler().getProtection(block);
        if (protection == null) return;

        // Add friend button
        setItem(49, Material.EMERALD, "§a온라인 플레이어 추가",
                "§7온라인 플레이어를",
                "§7친구로 추가합니다.");

        // Back button
        setItem(45, Material.ARROW, "§c뒤로 가기",
                "§7이전 메뉴로 돌아갑니다.");

        // Display current friends
        int slot = 0;
        for (Map.Entry<UUID, FriendPermission> entry : protection.getFriends().entrySet()) {
            if (slot >= 45) break; // Don't overflow

            UUID friendUuid = entry.getKey();
            FriendPermission perm = entry.getValue();

            OfflinePlayer friend = Bukkit.getOfflinePlayer(friendUuid);
            String permDisplay = perm == FriendPermission.READ_ONLY ? "§e읽기 전용" : "§a읽기/쓰기";

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(friend);
            meta.setDisplayName("§e" + (friend.getName() != null ? friend.getName() : "Unknown"));
            meta.setLore(Arrays.asList(
                    "§7권한: " + permDisplay,
                    "§7",
                    "§e좌클릭: §f권한 변경",
                    "§c우클릭: §f제거"
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

        for (int i = 0; i < 45; i++) {
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

        // Register click handler
        Bukkit.getPluginManager().registerEvents(new FriendMenuClickListener(plugin, player, block, inventory), plugin);
    }
}
