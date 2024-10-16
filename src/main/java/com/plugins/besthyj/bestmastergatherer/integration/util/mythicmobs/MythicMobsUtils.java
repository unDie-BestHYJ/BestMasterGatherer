package com.plugins.besthyj.bestmastergatherer.integration.util.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.utils.config.file.FileConfiguration;
import io.lumine.xikage.mythicmobs.utils.config.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class MythicMobsUtils {

    /**
     * 根据 物品ID 查找mm物品
     *
     * @param itemId
     * @return
     */
    public static ItemStack getMythicItem(String itemId) {
        MythicMobs mythicMobs = (MythicMobs) Bukkit.getServer().getPluginManager().getPlugin("MythicMobs");

        if (mythicMobs == null) {
            throw new IllegalStateException("MythicMobs 插件未找到");
        }

        Optional<MythicItem> mythicItemOptional = mythicMobs.getItemManager().getItem(itemId);

        if (mythicItemOptional.isPresent()) {
            MythicItem mythicItem = mythicItemOptional.get();
            return BukkitAdapter.adapt(mythicItem.generateItemStack(1));
        } else {
            throw new IllegalArgumentException("未找到指定的 MythicItem: " + itemId);
        }
    }

    /**
     * 根据物品id查找物品名
     *
     * @param itemId
     * @return
     */
    public static String getMythicItemDisplayName(String itemId) {
        MythicMobs mythicMobs = (MythicMobs) Bukkit.getServer().getPluginManager().getPlugin("MythicMobs");

        if (mythicMobs == null) {
            throw new IllegalStateException("MythicMobs 插件未找到");
        }

        Optional<MythicItem> mythicItemOptional = mythicMobs.getItemManager().getItem(itemId);

        if (mythicItemOptional.isPresent()) {
            MythicItem mythicItem = mythicItemOptional.get();
            ItemStack itemStack = BukkitAdapter.adapt(mythicItem.generateItemStack(1));
            String displayName = itemStack.getItemMeta().getDisplayName();

//            Bukkit.getLogger().info(displayName);

            return displayName;
        } else {
            throw new IllegalArgumentException("未找到指定的 MythicItem: " + itemId);
        }
    }

    /**
     * 返回所有的mm物品
     *
     * @return
     */
    public static List<MythicItem> getAllMythicItems() {
        MythicMobs mythicMobs = (MythicMobs) Bukkit.getServer().getPluginManager().getPlugin("MythicMobs");
        Collection<MythicItem> mythicItems = mythicMobs.getItemManager().getItems();
        return new ArrayList<>(mythicItems);
    }

    /**
     * 查找文件夹内的所有mm物品
     *
     * @param folderName
     * @return
     */
    public static Map<String, List<String>> getItemIdsFromFilesInFolder(String folderName) {
        Map<String, List<String>> fileToItemIdsMap = new HashMap<>();
        MythicMobs mythicMobs = (MythicMobs) Bukkit.getServer().getPluginManager().getPlugin("MythicMobs");

        if (mythicMobs == null) {
            throw new IllegalStateException("MythicMobs 插件未找到");
        }

        File itemsFolder = new File(mythicMobs.getDataFolder(), "items/" + folderName);

        if (itemsFolder.exists() && itemsFolder.isDirectory()) {
            for (File file : itemsFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

                    for (String itemId : fileConfig.getKeys(false)) {
                        int id = fileConfig.getInt(itemId + ".Id");
                        fileToItemIdsMap.computeIfAbsent(file.getName(), k -> new ArrayList<>()).add(itemId);
                    }
                }
            }
        }

        return fileToItemIdsMap;
    }

    /**
     * 返回 items 目录下所有的子目录
     *
     * @return
     */
    public static List<String> getAllItemFolders() {
        MythicMobs mythicMobs = (MythicMobs) Bukkit.getServer().getPluginManager().getPlugin("MythicMobs");
        File itemsFolder = new File(mythicMobs.getDataFolder(), "items");

        List<String> folderNames = new ArrayList<>();

        if (itemsFolder.exists() && itemsFolder.isDirectory()) {
            File[] folders = itemsFolder.listFiles(File::isDirectory);
            if (folders != null) {
                for (File folder : folders) {
                    folderNames.add(folder.getName());
                }
            }
        }
        return folderNames;
    }

    /**
     * 根据物品显示名称获取 MythicItem 的 ID
     *
     * @param displayName
     * @return
     */
    public static String getMythicItemIdByDisplayName(String displayName) {
        List<MythicItem> allItems = getAllMythicItems();

        for (MythicItem item : allItems) {
            String itemDisplayName = item.getDisplayName();
            if (itemDisplayName != null && itemDisplayName.equals(displayName)) {
                return item.getInternalName();  // 返回物品的内部 ID
            }
        }

        return null;
    }
}
