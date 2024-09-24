package com.plugins.besthyj.bestmastergatherer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileGetUtil {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static BestMasterGatherer plugin;

    public static void init(BestMasterGatherer instance) {
        plugin = instance;
    }

    /**
     * 从指定玩家的指定页数JSON文件中读取物品数据
     *
     * @param playerName 玩家名
     * @param page 页数
     * @return 返回一个Map，key为槽位ID，value为物品的详细信息
     */
    public static Map<String, Map<String, Object>> readItemData(String playerName, int page) {
        File file = new File(plugin.getDataFolderPath() + "/storage" + File.separator + playerName, "page_" + page + ".json");
        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, HashMap.class); // 使用 Gson 反序列化为 Map
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
}
