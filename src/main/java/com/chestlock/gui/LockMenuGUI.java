package com.chestlock.gui;

import com.chestlock.ChestLock;
import com.chestlock.model.BlockProtection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Main lock menu GUI for managing block protection
 */
public class LockMenuGUI {

    private final ChestLock plugin;
    private final Player player;
    private final Block block;
    private final Inventory inventory;

    public LockMenuGUI(ChestLock plugin, Player player, Block block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 27, "§6ChestLock 메뉴");

        setupInventory();
    }

    private void setupInventory() {
        BlockProtection protection = plugin.getDataHandler().getProtection(block);

        if (protection == null) {
            // Not locked - show lock option
            setItem(13, Material.TRIPWIRE_HOOK, "§a블록 잠그기",
                    "§7이 블록을 잠가서",
                    "§7다른 사람이 접근하지",
                    "§7못하도록 합니다.");
        } else {
            // Already locked
            if (protection.getOwner().equals(player.getUniqueId())) {
                // Owner - show management options
                setItem(10, Material.TRIPWIRE_HOOK, "§c블록 잠금 해제",
                        "§7블록의 잠금을 해제합니다.");

                setItem(12, Material.PLAYER_HEAD, "§e친구 관리",
                        "§7친구를 추가하거나",
                        "§7제거합니다.",
                        "§7",
                        "§e현재 친구: §f" + protection.getFriends().size() + "명");

                String hopperStatus = protection.isAllowHopper() ? "§a허용됨" : "§c차단됨";
                setItem(14, Material.HOPPER, "§e호퍼 설정",
                        "§7호퍼의 아이템 이동을",
                        "§7허용하거나 차단합니다.",
                        "§7",
                        "§e현재: " + hopperStatus);

                String redstoneStatus = protection.isAllowRedstone() ? "§a허용됨" : "§c차단됨";
                setItem(16, Material.REDSTONE, "§e레드스톤 설정",
                        "§7레드스톤 신호를",
                        "§7허용하거나 차단합니다.",
                        "§7",
                        "§e현재: " + redstoneStatus);
            } else if (protection.canAccess(player.getUniqueId())) {
                // Friend - show info
                setItem(13, Material.IRON_DOOR, "§e보호된 블록",
                        "§7이 블록은 다른 플레이어가",
                        "§7소유하고 있습니다.",
                        "§7",
                        "§a당신은 접근 권한이 있습니다.");
            } else {
                // No access
                setItem(13, Material.IRON_DOOR, "§c접근 불가",
                        "§7이 블록은 보호되어 있습니다.");
            }
        }

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
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
        Bukkit.getPluginManager().registerEvents(new LockMenuClickListener(plugin, player, block, inventory), plugin);
    }
}
