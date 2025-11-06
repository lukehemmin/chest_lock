package com.chestlock;

import com.chestlock.commands.ChestLockCommand;
import com.chestlock.data.BlockDataHandler;
import com.chestlock.listeners.*;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main plugin class for ChestLock
 * A block protection system without external dependencies
 */
public class ChestLock extends JavaPlugin {

    private static ChestLock instance;
    private BlockDataHandler dataHandler;
    private Set<Material> lockableBlocks;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize data handler
        dataHandler = new BlockDataHandler(this);
        dataHandler.loadAll();

        // Load lockable blocks from config
        loadLockableBlocks();

        // Register commands
        getCommand("chestlock").setExecutor(new ChestLockCommand(this));

        // Register listeners
        registerListeners();

        getLogger().info("ChestLock has been enabled!");
        getLogger().info("Loaded " + lockableBlocks.size() + " lockable block types");
    }

    @Override
    public void onDisable() {
        // Save all location-based protections
        if (dataHandler != null) {
            dataHandler.saveAll();
        }

        getLogger().info("ChestLock has been disabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);
        getServer().getPluginManager().registerEvents(new PistonListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
    }

    private void loadLockableBlocks() {
        lockableBlocks = new HashSet<>();

        List<String> containers = getConfig().getStringList("lockable-blocks.containers");
        List<String> shulkerBoxes = getConfig().getStringList("lockable-blocks.shulker-boxes");
        List<String> doors = getConfig().getStringList("lockable-blocks.doors");
        List<String> trapdoors = getConfig().getStringList("lockable-blocks.trapdoors");
        List<String> gates = getConfig().getStringList("lockable-blocks.gates");

        loadMaterialList(containers);
        loadMaterialList(shulkerBoxes);
        loadMaterialList(doors);
        loadMaterialList(trapdoors);
        loadMaterialList(gates);
    }

    private void loadMaterialList(List<String> materials) {
        for (String materialName : materials) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                lockableBlocks.add(material);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid material in config: " + materialName);
            }
        }
    }

    public boolean isLockable(Material material) {
        return lockableBlocks.contains(material);
    }

    public BlockDataHandler getDataHandler() {
        return dataHandler;
    }

    public static ChestLock getInstance() {
        return instance;
    }

    public String getMessage(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + path, "");
        return colorize(prefix + " " + message);
    }

    public String getMessageWithoutPrefix(String path) {
        return colorize(getConfig().getString("messages." + path, ""));
    }

    private String colorize(String message) {
        return message.replace("&", "§");
    }

    public void reloadConfiguration() {
        reloadConfig();
        loadLockableBlocks();
        dataHandler.loadAll();
    }
}
