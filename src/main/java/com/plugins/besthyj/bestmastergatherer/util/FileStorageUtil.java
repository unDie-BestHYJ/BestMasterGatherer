//package com.plugins.besthyj.bestmastergatherer.util;
//
//import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Material;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.plugin.Plugin;
//import org.bukkit.enchantments.Enchantment;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//
//public class FileStorageUtil {
//
//    private static BestMasterGatherer plugin;
//
//    /**
//     * 初始化静态类
//     *
//     * @param pluginInstance
//     */
//    public static void init(BestMasterGatherer pluginInstance) {
//        plugin = pluginInstance;
//    }
//
//    /**
//     * 将饰品数据存入本地文件中
//     *
//     * @param player
//     * @param guiId
//     * @param itemId
//     * @param item
//     */
//    public static void saveItemToStorage(Player player, String guiId, int slot, String itemId, ItemStack item) {
//        // 获取玩家名并构建存储文件路径
//        File playerStorageFile = new File(plugin.getDataFolderPath(), "storage/" + player.getName() + ".yml");
//        FileConfiguration storageConfig = YamlConfiguration.loadConfiguration(playerStorageFile);
//
//        // 获取物品的元数据
//        ItemMeta meta = item.getItemMeta();
//        if (meta == null) {
//            return; // 如果物品没有元数据，直接返回
//        }
//
//        // 创建路径：gui-ID > slot > itemId
//        String path = guiId + "." + slot + "." + itemId;
//
//        // 保存物品ID和各种数据
//        storageConfig.set(path + ".Material", item.getType().name());
//        storageConfig.set(path + ".Amount", item.getAmount());
//
//        // 保存物品的数据值（如果适用）
//        if (item.getDurability() != 0) {
//            storageConfig.set(path + ".Data", item.getDurability());
//        }
//
//        // 保存物品的显示名
//        if (meta.hasDisplayName()) {
//            storageConfig.set(path + ".Display", meta.getDisplayName());
//        }
//
//        // 保存物品的lore（描述）
//        if (meta.hasLore()) {
//            storageConfig.set(path + ".Lores", meta.getLore());
//        }
//
//        // 保存附魔信息
//        if (meta.hasEnchants()) {
//            Map<Enchantment, Integer> enchantments = meta.getEnchants();
//            Map<String, Integer> enchantMap = new HashMap<>();
//            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
//                enchantMap.put(entry.getKey().getName(), entry.getValue());
//            }
//            storageConfig.set(path + ".Enchantments", enchantMap);
//        }
//
//        // 保存配置文件到磁盘
//        try {
//            storageConfig.save(playerStorageFile);
////            player.sendMessage(ChatColor.GREEN + "物品数据已成功保存到 " + playerStorageFile.getName());
//        } catch (IOException e) {
//            e.printStackTrace();
//            player.sendMessage(ColorUtil.translateColorCode("&c保存物品数据时出现错误！请联系腐竹处理！"));
//        }
//    }
//
//    /**
//     * 从存储文件夹中加载物品信息
//     *
//     * @param playerName
//     * @param guiId
//     * @return
//     */
//    public static Map<Integer, ItemStack> loadStoredItems(String playerName, String guiId) {
//        File playerFile = new File(plugin.getDataFolder(), "storage/" + playerName + ".yml");
//        if (!playerFile.exists()) {
//            return Collections.emptyMap();  // 如果文件不存在，返回空Map
//        }
//
//        FileConfiguration storageConfig = YamlConfiguration.loadConfiguration(playerFile);
//        if (!storageConfig.contains(guiId)) {
//            return Collections.emptyMap();  // 如果没有找到对应的GUI ID，返回空Map
//        }
//
//        Map<Integer, ItemStack> itemMap = new HashMap<>();
//
//        // 遍历存储的物品槽位
//        for (String slotKey : storageConfig.getConfigurationSection(guiId).getKeys(false)) {
//            int slot = Integer.parseInt(slotKey);
//
//            // 获取物品的ID部分 (例如 '测试1')
//            ConfigurationSection slotSection = storageConfig.getConfigurationSection(guiId + "." + slotKey);
//
//            // 遍历 itemId
//            for (String itemId : slotSection.getKeys(false)) {
//                ConfigurationSection itemSection = slotSection.getConfigurationSection(itemId);
//
//                String materialStr = itemSection.getString("Material");
//                if (materialStr == null) {
//                    Bukkit.getLogger().severe("槽位 " + slotKey + " 的物品 " + itemId + " 没有指定 Material！");
//                    continue;
//                }
//
//                Material material = Material.getMaterial(materialStr.toUpperCase());
//                if (material == null) {
//                    Bukkit.getLogger().severe("无效的物品类型: " + materialStr + " 在槽位 " + slotKey + " 的物品 " + itemId);
//                    continue;
//                }
//
//                int amount = itemSection.getInt("Amount", 1);
//                String display = itemSection.getString("Display");
//                List<String> lores = itemSection.getStringList("Lores");
//
//                Bukkit.getLogger().info(itemId + " " + materialStr + " " + amount + " " + display);
//
//                // 创建物品
//                ItemStack item = new ItemStack(material, amount);
//                ItemMeta meta = item.getItemMeta();
//                if (meta != null) {
//                    if (display != null) {
//                        meta.setDisplayName(display);
//                    }
//                    if (lores != null) {
//                        meta.setLore(lores);
//                    }
//                    item.setItemMeta(meta);
//                }
//
//                // 将物品放入Map中，以槽位为key，ItemStack为value
//                itemMap.put(slot, item);
//            }
//        }
//
//        return itemMap;
//    }
//
//    public static Map<String, List<ItemStack>> loadAllStoredItems() {
//        Map<String, List<ItemStack>> playerItemsMap = new HashMap<>();
//
//        // 定位 storage 文件夹
//        File storageFolder = new File(plugin.getDataFolder(), "storage");
//        if (!storageFolder.exists() || !storageFolder.isDirectory()) {
//            Bukkit.getLogger().severe("存储文件夹不存在或不是文件夹！");
//            return playerItemsMap;  // 返回空的 map
//        }
//
//        // 遍历存储文件夹中的所有文件（假设文件名是玩家名）
//        for (File playerFile : storageFolder.listFiles()) {
//            if (!playerFile.getName().endsWith(".yml")) {
//                continue;  // 只处理 .yml 文件
//            }
//
//            String playerName = playerFile.getName().replace(".yml", "");  // 获取玩家名
//
//            // 加载每个玩家的文件配置
//            FileConfiguration storageConfig = YamlConfiguration.loadConfiguration(playerFile);
//
//            // 存储这个玩家的所有 ItemStack 列表
//            List<ItemStack> playerItems = new ArrayList<>();
//
//            // 遍历这个玩家文件中的所有 GUI
//            for (String guiId : storageConfig.getKeys(false)) {
//
//                // 遍历存储的物品槽位
//                for (String slotKey : storageConfig.getConfigurationSection(guiId).getKeys(false)) {
//                    int slot = Integer.parseInt(slotKey);
//
//                    // 获取物品的ID部分
//                    ConfigurationSection slotSection = storageConfig.getConfigurationSection(guiId + "." + slotKey);
//
//                    // 遍历 itemId
//                    for (String itemId : slotSection.getKeys(false)) {
//                        ConfigurationSection itemSection = slotSection.getConfigurationSection(itemId);
//
//                        String materialStr = itemSection.getString("Material");
//                        if (materialStr == null) {
//                            Bukkit.getLogger().severe("槽位 " + slotKey + " 的物品 " + itemId + " 没有指定 Material！");
//                            continue;
//                        }
//
//                        Material material = Material.getMaterial(materialStr.toUpperCase());
//                        if (material == null) {
//                            Bukkit.getLogger().severe("无效的物品类型: " + materialStr + " 在槽位 " + slotKey + " 的物品 " + itemId);
//                            continue;
//                        }
//
//                        int amount = itemSection.getInt("Amount", 1);
//                        String display = itemSection.getString("Display");
//                        List<String> lores = itemSection.getStringList("Lores");
//
//                        // 创建物品
//                        ItemStack item = new ItemStack(material, amount);
//                        ItemMeta meta = item.getItemMeta();
//                        if (meta != null) {
//                            if (display != null) {
//                                meta.setDisplayName(display);
//                            }
//                            if (lores != null) {
//                                meta.setLore(lores);
//                            }
//                            item.setItemMeta(meta);
//                        }
//
//                        // 将物品添加到玩家的物品列表中
//                        playerItems.add(item);
//                    }
//                }
//            }
//
//            // 将玩家的物品列表存储到 Map 中
//            playerItemsMap.put(playerName, playerItems);
//        }
//
//        return playerItemsMap;
//    }
//
//    /**
//     * 从玩家的存储文件中移除指定槽位的物品。
//     *
//     * @param playerName 玩家名称
//     * @param guiId GUI的标识
//     * @param slot 槽位编号
//     */
//    public static void removeItemFromStorage(String playerName, String guiId, int slot) {
//        File playerFile = new File(plugin.getDataFolderPath(), "storage/" + playerName + ".yml");
//        if (!playerFile.exists()) {
//            return; // 如果文件不存在，直接返回
//        }
//
//        FileConfiguration storageConfig = YamlConfiguration.loadConfiguration(playerFile);
//
//        // 检查是否存在该 GUI ID 和槽位
//        if (storageConfig.contains(guiId) && storageConfig.getConfigurationSection(guiId).contains(String.valueOf(slot))) {
//            storageConfig.set(guiId + "." + slot, null); // 删除该槽位的信息
//            try {
//                storageConfig.save(playerFile); // 保存更改
//            } catch (IOException e) {
//                Bukkit.getLogger().severe("无法保存玩家存储文件: " + e.getMessage());
//            }
//        }
//    }
//}
