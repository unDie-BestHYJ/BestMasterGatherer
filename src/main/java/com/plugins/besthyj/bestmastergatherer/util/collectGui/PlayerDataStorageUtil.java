package com.plugins.besthyj.bestmastergatherer.util.collectGui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataStorageUtil {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static BestMasterGatherer plugin;

    public static void init(BestMasterGatherer instance) {
        plugin = instance;
    }

    /**
     * 保存物品数据到 JSON 文件
     * 封装过的供外部调用的接口
     *
     * @param player  玩家对象
     * @param item    物品对象
     * @param page    页数
     * @param slotId  槽位 ID
     */
    public static void saveItemData(Player player, ItemStack item, int page, int slotId) {
        if (item == null || !item.hasItemMeta()) return; // 检查 item 是否为 null 或没有 Meta

        Bukkit.getLogger().info("saveItemData " + item.getItemMeta().getDisplayName());

        // 创建一个存储物品数据的 Map，key 为槽位ID，value 为物品详细信息
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("amount", item.getAmount());
        itemData.put("itemType", item.getType().name()); // 物品类型
        itemData.put("itemName", item.getItemMeta().getDisplayName()); // 物品显示名称
        itemData.put("itemLore", item.getItemMeta().getLore());
        itemData.put("nbtData", getNbtData(item));

        // 将物品数据添加到 inventoryData
        Map<Integer, Map<String, Object>> inventoryData = new HashMap<>();
        inventoryData.put(slotId, itemData); // 使用传入的槽位ID

        // 保存物品数据到指定页的 JSON 文件
        PlayerDataStorageUtil.saveItemData(player.getName(), page, inventoryData);
    }

    /**
     * 获取nbt标签(除了display)
     *
     * @param item
     * @return
     */
    private static String getNbtData(ItemStack item) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        net.minecraft.server.v1_12_R1.NBTTagCompound tag = nmsItem.getTag();

        // 创建一个新的 NBTTagCompound 用于保存除了 display 之外的所有 NBT 数据
        net.minecraft.server.v1_12_R1.NBTTagCompound filteredTag = new net.minecraft.server.v1_12_R1.NBTTagCompound();

        if (tag != null) {
            // 获取 NBT 数据的所有键
            for (String key : tag.c()) { // `tag.c()` 获取所有 NBT 键
                if (!"display".equals(key)) { // 排除 `display` 键
                    filteredTag.set(key, tag.get(key)); // 将非 `display` 的键值对保存到 filteredTag
                }
            }
        }

        return filteredTag.toString(); // 返回过滤后的 NBT 数据的字符串表示
    }

    /**
     * 保存仓库内物品数据到指定页数的JSON文件
     *
     * @param playerName
     * @param page
     * @param newInventoryData
     */
    private static void saveItemData(String playerName, int page, Map<Integer, Map<String, Object>> newInventoryData) {
        // 定义文件路径，以玩家名为文件夹，以页数作为文件名
        File file = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName, "page_" + page + ".json");
        file.getParentFile().mkdirs(); // 如果目录不存在则创建

        Map<Integer, Map<String, Object>> inventoryData = new HashMap<>();

        // 如果文件存在，先读取旧数据
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                inventoryData = gson.fromJson(reader, new TypeToken<Map<Integer, Map<String, Object>>>() {}.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 合并新数据
        inventoryData.putAll(newInventoryData);

        // 将合并后的数据写入文件
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(inventoryData, writer); // 使用 Gson 将合并后的物品数据序列化为 JSON 并写入文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除某个玩家指定页数的物品数据文件
     *
     * @param playerName 玩家名
     * @param page 页数
     * @return 如果文件成功删除返回true，否则返回false
     */
    public static boolean deleteItemData(String playerName, int page) {
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
    public static Map<String, Map<String, Object>> readItemData(String playerName, int page) {
        File file = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName, "page_" + page + ".json");
        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取物品数量Map
     *
     * @param playerName
     * @return
     */
    public static Map<String, Integer> readItems(String playerName) {
        File playerFolder = new File(plugin.getDataFolderPath() + File.separator + "storage" + File.separator + playerName);
        if (!playerFolder.exists() || !playerFolder.isDirectory()) {
            return null; // 如果玩家文件夹不存在，则返回 null
        }

        Map<String, Integer> itemsMap = new HashMap<>();

        // 遍历玩家文件夹中的所有文件
        for (File file : playerFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                try (FileReader reader = new FileReader(file)) {
                    // 读取 JSON 文件内容并解析为 Map
                    Map<String, Map<String, Object>> fileData = gson.fromJson(reader, HashMap.class);

                    // 遍历当前文件中的所有槽位数据
                    for (Map.Entry<String, Map<String, Object>> entry : fileData.entrySet()) {
                        Map<String, Object> itemData = entry.getValue();

                        // 获取物品名称和数量
                        String itemName = (String) itemData.get("itemName");
                        Double itemCount = (Double) itemData.get("amount"); // 读取数量，注意这是 Double 类型

                        // 将物品名称和数量添加到 itemsMap
                        if (itemName != null && itemCount != null) {
                            int totalCount = itemCount.intValue(); // 转换为 int 类型
                            itemsMap.merge(itemName, totalCount, Integer::sum); // 累加相同物品的数量
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // 处理异常
                }
            }
        }

        return itemsMap; // 返回物品名称和数量的映射
    }

    /**
     * 应用nbt标签
     *
     * @param item
     * @param nbtData
     */
    public static void applyNbtData(ItemStack item, String nbtData) {
        try {
            // 获取 NMS 版本的 ItemStack
            net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            net.minecraft.server.v1_12_R1.NBTTagCompound tag = nmsItem.getTag() != null ? nmsItem.getTag() : new net.minecraft.server.v1_12_R1.NBTTagCompound();

            // 使用反射访问 MojangsonParser.parse 方法
            Class<?> mojangsonParserClass = Class.forName("net.minecraft.server.v1_12_R1.MojangsonParser");
            java.lang.reflect.Method parseMethod = mojangsonParserClass.getDeclaredMethod("parse", String.class);
            parseMethod.setAccessible(true);  // 确保我们能够访问 private 方法

            // 调用 parse 方法将 NBT 字符串转换为 NBTTagCompound
            net.minecraft.server.v1_12_R1.NBTTagCompound nbtParsed = (net.minecraft.server.v1_12_R1.NBTTagCompound) parseMethod.invoke(null, nbtData);

            // 合并现有的 NBT 数据
            tag.a(nbtParsed); // 将新解析的 NBT 数据合并到现有的 NBT 数据中

            // 将 NBT 数据设置回物品
            nmsItem.setTag(tag);

            // 更新 Bukkit ItemStack
            item.setItemMeta(CraftItemStack.getItemMeta(nmsItem));

        } catch (Exception e) {
            e.printStackTrace();
            // 处理可能的异常，例如反射或 NBT 格式错误
        }
    }

    /**
     * 获取已填充的槽位信息
     *
     * @param guiId GUI 的 ID
     * @return 已填充的槽位列表
     */
    public static List<Integer> getFilledSlots(String guiId, String guiFolder) {
        List<Integer> filledSlots = new ArrayList<>();
        File guiFile = new File(plugin.getDataFolderPath(), guiFolder + "/" + guiId + ".yml");

        if (guiFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            List<String> layout = config.getStringList("layout");

            // 解析布局，填充槽位
            for (int row = 0; row < layout.size(); row++) {
                String line = layout.get(row);
                for (int col = 0; col < line.length(); col++) {
                    char itemChar = line.charAt(col);
                    if (itemChar != ' ') { // 非空格字符表示有物品
                        filledSlots.add(row * 9 + col); // 计算槽位索引
                    }
                }
            }
        }
        return filledSlots;
    }
}
