package com.plugins.besthyj.bestmastergatherer.util.collectGui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.plugins.besthyj.bestinventory.api.InventoryItemAPI;
import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PlayerDataStorageUtil {

    private final Gson gson;
    private final BestMasterGatherer plugin;

    public PlayerDataStorageUtil(BestMasterGatherer plugin) {
        this.plugin = plugin;
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * 从 ItemStack 中提取 NBT 数据，忽略 "display" 标签，并返回处理过的 NBT 数据字符串
     *
     * @param item 目标物品
     * @return 返回物品的 NBT 数据字符串，忽略 "display" 标签
     */
    private String getNbtData(ItemStack item) {
        StringBuilder nbtDataString = new StringBuilder();
        if (item == null) {
            return nbtDataString.toString();
        }
        NBTItem nbtItem = new NBTItem(item);
        for (String nbtKey : nbtItem.getKeys()) {
            if (nbtKey.equals("display")) {
                continue;
            }
            if (nbtItem.hasKey(nbtKey)) {
                switch (nbtItem.getType(nbtKey)) {
                    case NBTTagString:
                        String strValue = nbtItem.getString(nbtKey);
                        nbtDataString.append(nbtKey).append(":").append(strValue).append(",");
                        break;
                    case NBTTagInt:
                        int intValue = nbtItem.getInteger(nbtKey);
                        nbtDataString.append(nbtKey).append(":").append(intValue).append(",");
                        break;
                    case NBTTagDouble:
                        double doubleValue = nbtItem.getDouble(nbtKey);
                        nbtDataString.append(nbtKey).append(":").append(doubleValue).append(",");
                        break;
                    case NBTTagFloat:
                        float floatValue = nbtItem.getFloat(nbtKey);
                        nbtDataString.append(nbtKey).append(":").append(floatValue).append(",");
                        break;
                    case NBTTagLong:
                        long longValue = nbtItem.getLong(nbtKey);
                        nbtDataString.append(nbtKey).append(":").append(longValue).append(",");
                        break;
                    case NBTTagByte:
                        byte byteValue = nbtItem.getByte(nbtKey);
                        nbtDataString.append(nbtKey).append(":").append(byteValue).append(",");
                        break;
                    default:
                        Bukkit.getLogger().warning("无法识别的 NBT 数据类型: " + nbtKey);
                        break;
                }
            }
        }
        return nbtDataString.toString();
    }

    /**
     * 保存物品数据到 JSON 文件
     * 封装过的供外部调用的接口
     *
     * @param player  玩家对象
     * @param item    物品对象
     * @param page    页数
     * @param slot  槽位 ID
     */
    public void saveItemData(Player player, ItemStack item, int page, int slot) {
        if (item == null || !item.hasItemMeta()) return;

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("amount", item.getAmount());
        itemData.put("itemType", item.getType().name());
        itemData.put("itemName", item.getItemMeta().getDisplayName());
        itemData.put("itemLore", item.getItemMeta().getLore());
        itemData.put("nbtData", getNbtData(item));

        Map<Integer, Map<String, Object>> inventoryData = new HashMap<>();
        inventoryData.put(slot, itemData);

        saveItemData(player.getName(), page, inventoryData);
    }

    /**
     * 保存仓库内物品数据到指定页数的JSON文件
     *
     * @param playerName
     * @param page
     * @param newInventoryData
     */
    private void saveItemData(String playerName, int page, Map<Integer, Map<String, Object>> newInventoryData) {
        File file = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName, "page_" + page + ".json");
        file.getParentFile().mkdirs();

        Map<Integer, Map<String, Object>> inventoryData = new HashMap<>();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                inventoryData = gson.fromJson(reader, new TypeToken<Map<Integer, Map<String, Object>>>() {}.getType());
            } catch (IOException e) {
                Bukkit.getLogger().severe("加载仓库存储数据失败: " + e.getMessage());
            }
        }

        inventoryData.putAll(newInventoryData);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(inventoryData, writer);
        } catch (IOException e) {
            Bukkit.getLogger().severe("保存仓库存储数据失败: " + e.getMessage());
        }
    }

    /**
     * 删除仓库内指定槽位的物品数据
     *
     * @param playerName        玩家名
     * @param page              页数
     * @param slot              需要删除的槽位编号
     */
    public void deleteItemData(String playerName, int page, int slot) {
        File file = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName, "page_" + page + ".json");
        file.getParentFile().mkdirs();

        Map<Integer, Map<String, Object>> inventoryData = new HashMap<>();
        if (file.exists()) {
            // 读取现有的仓库数据
            try (FileReader reader = new FileReader(file)) {
                inventoryData = gson.fromJson(reader, new TypeToken<Map<Integer, Map<String, Object>>>() {}.getType());
            } catch (IOException e) {
                Bukkit.getLogger().severe("加载仓库存储数据失败: " + e.getMessage());
                return;
            }

            // 删除指定槽位的数据
            if (inventoryData.containsKey(slot)) {
                inventoryData.remove(slot);
            } else {
                Bukkit.getLogger().info("槽位 " + slot + " 没有找到物品数据，无法删除。");
                return;
            }

            // 保存更新后的数据
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(inventoryData, writer);
            } catch (IOException e) {
                Bukkit.getLogger().severe("保存仓库存储数据失败: " + e.getMessage());
            }
        } else {
            Bukkit.getLogger().info("页面 " + page + " 的仓库数据文件不存在。");
        }
    }

    /**
     * 删除某个玩家指定页数的物品数据文件
     *
     * @param playerName 玩家名
     * @param page 页数
     * @return 如果文件成功删除返回true，否则返回false
     */
    public boolean deleteItemData(String playerName, int page) {
        File file = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName, "page_" + page + ".json");
        return file.exists() && file.delete();
    }

    /**
     * 从指定玩家的指定页数JSON文件中读取物品数据
     *
     * @param playerName 玩家名
     * @param page 页数
     * @return 返回一个Map，key为槽位ID，value为物品的详细信息
     */
    public Map<String, Map<String, Object>> readItemData(String playerName, int page) {
        File file = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName, "page_" + page + ".json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, new TypeToken<Map<String, Map<String, Object>>>() {}.getType());
        } catch (IOException e) {
            Bukkit.getLogger().severe("读取仓库存储数据失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取物品数量Map
     *
     * @param playerName
     * @return
     */
    public Map<String, Integer> readItems(String playerName) {
        File playerFolder = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName);
        if (!playerFolder.exists() || !playerFolder.isDirectory()) {
            return null;
        }

        Map<String, Integer> itemsMap = new HashMap<>();

        for (File file : playerFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                try (FileReader reader = new FileReader(file)) {
                    Map<String, Map<String, Object>> fileData = gson.fromJson(reader, HashMap.class);

                    for (Map.Entry<String, Map<String, Object>> entry : fileData.entrySet()) {
                        Map<String, Object> itemData = entry.getValue();

                        String itemName = (String) itemData.get("itemName");
                        Double itemCount = (Double) itemData.get("amount");

                        if (itemName != null && itemCount != null) {
                            int totalCount = itemCount.intValue();
                            itemsMap.merge(itemName, totalCount, Integer::sum);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 将背包里的物品添加到itemsMap中
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    String itemName = item.getItemMeta().getDisplayName();
                    int itemCount = item.getAmount();

                    itemsMap.merge(itemName, itemCount, Integer::sum);
                }
            }
        }

        // 将饰品背包里的物品添加到itemsMap中
        Map<String, Integer> inventoryItemMap = InventoryItemAPI.getItemsMap(playerName);

        for (Map.Entry<String, Integer> entry : inventoryItemMap.entrySet()) {
            String itemName = entry.getKey();
            int itemCount = entry.getValue();

            if (itemsMap.containsKey(itemName)) {
                itemsMap.put(itemName, itemsMap.get(itemName) + itemCount);
            } else {
                itemsMap.put(itemName, itemCount);
            }
        }

        return itemsMap;
    }

    /**
     * 将nbtData添加到ItemStack上
     *
     * @param item
     * @param nbtData
     * @return
     */
    public ItemStack applyNbtData(ItemStack item, String nbtData) {
//        Bukkit.getLogger().info(nbtData);
        try {
            NBTItem nbtItem = new NBTItem(item);

            boolean isCurlyBracketFormat = nbtData.startsWith("{") && nbtData.endsWith("}");

            if (isCurlyBracketFormat) {
                nbtData = nbtData.substring(1, nbtData.length() - 1);
            }

            String[] nbtEntries = nbtData.split(",");

            for (String entry : nbtEntries) {
                String[] keyValue;
                if (isCurlyBracketFormat) {
                    keyValue = entry.split(":");
                } else {
                    keyValue = entry.split(":");
                }

                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    if (value.equals("true") || value.equals("false")) {
                        boolean boolValue = Boolean.parseBoolean(value);
                        nbtItem.setBoolean(key, boolValue);
                    } else if (value.endsWith("b")) {
                        byte byteValue = Byte.parseByte(value.substring(0, value.length() - 1));
                        nbtItem.setByte(key, byteValue);
                    } else if (value.endsWith("d")) {
                        double doubleValue = Double.parseDouble(value.substring(0, value.length() - 1));
                        nbtItem.setDouble(key, doubleValue);
                    } else if (value.endsWith("f")) {
                        float floatValue = Float.parseFloat(value.substring(0, value.length() - 1));
                        nbtItem.setFloat(key, floatValue);
                    } else if (value.endsWith("L")) {
                        long longValue = Long.parseLong(value.substring(0, value.length() - 1));
                        nbtItem.setLong(key, longValue);
                    } else {
                        try {
                            int intValue = Integer.parseInt(value);
                            nbtItem.setInteger(key, intValue);
                        } catch (NumberFormatException e) {
                            nbtItem.setString(key, value);
                        }
                    }
                } else {
                    Bukkit.getLogger().warning("无法解析的 NBT 数据条目: " + entry);
                }
            }

            return nbtItem.getItem();

        } catch (Exception e) {
            Bukkit.getLogger().severe("添加NBT失败: " + e.getMessage());
            return item;
        }
    }

    /**
     * 获取已填充的槽位信息
     *
     * @param guiId GUI 的 ID
     * @return 已填充的槽位列表
     */
    public List<Integer> getFilledSlots(String guiId, String guiFolder) {
        List<Integer> filledSlots = new ArrayList<>();
        File guiFile = new File(plugin.getDataFolderPath(), guiFolder + File.separator + guiId + ".yml");

        if (guiFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            List<String> layout = config.getStringList("layout");

            for (int row = 0; row < layout.size(); row++) {
                String line = layout.get(row);
                for (int col = 0; col < line.length(); col++) {
                    if (line.charAt(col) != ' ') {
                        filledSlots.add(row * 9 + col);
                    }
                }
            }
        }
        return filledSlots;
    }
}
