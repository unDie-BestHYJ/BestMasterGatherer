package com.plugins.besthyj.bestmastergatherer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        File file = new File(plugin.getDataFolderPath() + "/storage" + File.separator + playerName, "page_" + page + ".json");
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
     * 移除物品数据（删除 JSON 文件）
     *
     * @param player
     * @param item
     */

    /**
     * 删除某个玩家指定页数的物品数据文件
     *
     * @param playerName 玩家名
     * @param page 页数
     * @return 如果文件成功删除返回true，否则返回false
     */
    public static boolean deleteItemData(String playerName, int page) {
        File file = new File(plugin.getDataFolderPath() + "/storage" + File.separator + playerName, "page_" + page + ".json");
        return file.exists() && file.delete();
    }

    /**
     * 将ItemStack转换为物品详细信息的Map
     *
     * @param item 物品堆
     * @return 包含物品数量、材质、名称和描述的Map
     */
    public static Map<String, Object> convertItemToData(ItemStack item) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", item.getAmount());
        data.put("material", item.getType().toString());
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                data.put("name", item.getItemMeta().getDisplayName());
            }
            if (item.getItemMeta().hasLore()) {
                data.put("lore", item.getItemMeta().getLore());
            }
        }
        return data;
    }

    /**
     * 将物品详细信息的Map转换为ItemStack
     *
     * @param data 包含物品数量、材质、名称和描述的Map
     * @return ItemStack物品堆
     */
    public static ItemStack convertDataToItem(Map<String, Object> data) {
        ItemStack item = new ItemStack(Material.valueOf((String) data.get("material")), ((Double) data.get("amount")).intValue());
        ItemMeta meta = item.getItemMeta();
        if (data.containsKey("name")) {
            meta.setDisplayName((String) data.get("name"));
        }
        if (data.containsKey("lore")) {
            meta.setLore((List<String>) data.get("lore"));
        }
        item.setItemMeta(meta);
        return item;
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
