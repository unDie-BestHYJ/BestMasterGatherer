package com.plugins.besthyj.bestmastergatherer.util.attributeGui;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.constant.VariableConstant;
import com.plugins.besthyj.bestmastergatherer.integration.util.mythicmobs.MythicMobsUtils;
import com.plugins.besthyj.bestmastergatherer.model.attributeGui.AttributeGuiItem;
import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;
import com.plugins.besthyj.bestmastergatherer.util.collectGui.PlayerDataStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeGuiItemUtil {
    private static BestMasterGatherer plugin;

    public static void init(BestMasterGatherer pluginInstance) {
        plugin = pluginInstance;
    }

    public static Map<String, AttributeGuiItem> loadItems(String folder) {
        Map<String, AttributeGuiItem> itemMap = new HashMap<>();

        File directory = new File(plugin.getDataFolder(), folder);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                // 获取 items 部分
                if (config.contains("items")) {
                    for (String itemId : config.getConfigurationSection("items").getKeys(false)) {
                        AttributeGuiItem item = new AttributeGuiItem();
                        item.setItemId(itemId);

                        item.setItemTypeId(config.getInt("items." + itemId + ".Id", 0)); // 默认值为 0
                        item.setItemTypeData(config.getInt("items." + itemId + ".Data", 0)); // 默认值为 0
                        item.setItemName(config.getString("items." + itemId + ".Display", ""));

                        List<String> lores = config.getStringList("items." + itemId + ".Lores");
                        item.setLoresList(lores.isEmpty() ? Arrays.asList() : lores); // 空列表

                        List<String> mmItems = config.getStringList("items." + itemId + ".mmItems");
                        item.setMmItemsList(mmItems.isEmpty() ? Arrays.asList() : mmItems); // 空列表

                        // 设置 attributes
                        Map<Integer, List<String>> attributesMap = new HashMap<>();
                        if (config.contains("items." + itemId + ".attributes")) {
                            for (String attrKey : config.getConfigurationSection("items." + itemId + ".attributes").getKeys(false)) {
                                List<String> attrList = config.getStringList("items." + itemId + ".attributes." + attrKey);
                                attributesMap.put(Integer.parseInt(attrKey), attrList);
                            }
                        }
                        item.setAttributesMap(attributesMap);

                        itemMap.put(itemId, item);
                    }
                }
            }
        }
        return itemMap;
    }

    /**
     * 根据 AttributeGuiItem 创建 ItemStack
     *
     * @param attributeItem
     * @return
     */
    public static ItemStack createGuiItemFromAttributeItem(AttributeGuiItem attributeItem, int count) {
        int id = attributeItem.getItemTypeId();
        int data = attributeItem.getItemTypeData();

        ItemStack itemStack = new ItemStack(Material.getMaterial(id), 1, (short) data);
        ItemMeta meta = itemStack.getItemMeta();

        String displayName = ColorUtil.translateColorCode(attributeItem.getItemName());
        meta.setDisplayName(displayName);

        List<String> lores = attributeItem.getLoresList();
        List<String> translatedLores = new ArrayList<>();
        for (String lore : lores) {
            Pattern pattern = Pattern.compile(VariableConstant.IS_USEFUL_REGEX);
            Matcher matcher = pattern.matcher(lore);

            StringBuffer updatedLore = new StringBuffer();
            while (matcher.find()) {
                int number = Integer.parseInt(matcher.group(1));
                String replacement = (count >= number) ? CommonConstant.COLLECTED : CommonConstant.UNCOLLECTED;

                matcher.appendReplacement(updatedLore, replacement);
            }
            matcher.appendTail(updatedLore);

            translatedLores.add(ColorUtil.translateColorCode(updatedLore.toString()));
        }

        meta.setLore(translatedLores);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * 根据 FileConfiguration 创建 ItemStack
     *
     * @param config
     * @param itemId
     * @return
     */
    public static ItemStack createGuiItem(FileConfiguration config, String itemId) {
        int id = config.getInt("items." + itemId + ".Id");
        int data = config.getInt("items." + itemId + ".Data", 0);

        ItemStack itemStack = new ItemStack(Material.getMaterial(id), 1, (short) data);
        ItemMeta meta = itemStack.getItemMeta();

        String displayName = ColorUtil.translateColorCode(config.getString("items." + itemId + ".Display"));

        meta.setDisplayName(displayName);

        List<String> lores = config.getStringList("items." + itemId + ".Lores");
        List<String> translatedLores = new ArrayList<>();
        for (String lore : lores) {
            translatedLores.add(ColorUtil.translateColorCode(lore));
        }
        meta.setLore(translatedLores);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * 获取 mm物品名 集合
     *
     * @param item
     * @return
     */
    public static Set<String> getDisplaySet(AttributeGuiItem item) {
        List<String> mmItems = item.getMMItemsList();
        if (mmItems == null || mmItems.isEmpty()) {
            return null;
        }

        Set<String> displaySet = new HashSet<>();

        for (String mmItem : mmItems) {
            String displayName = MythicMobsUtils.getMythicItemDisplayName(mmItem);

            displaySet.add(displayName);
        }

        Bukkit.getLogger().info("" + displaySet.size());
        Bukkit.getLogger().info(displaySet.toString());

        return displaySet;
    }

    /**
     * 获取已经收集的物品数量
     *
     * @param player
     * @param attributeGuiItem
     * @return
     */
    public static Integer getCollectedCount(Player player, AttributeGuiItem attributeGuiItem) {
        Set<String> displaySet = getDisplaySet(attributeGuiItem);

        if (displaySet != null) {
            Bukkit.getLogger().info("displaySet Size " + displaySet.size());
        } else {
            Bukkit.getLogger().info("displaySet Size " + 0);
        }

        Map<String, Integer> stringIntegerMap = PlayerDataStorageUtil.readItems(player.getName());

        Set<String> itemSet  = stringIntegerMap.keySet();

//        if (itemSet != null) {
//            Bukkit.getLogger().info("itemSet Size " + itemSet.size());
//        } else {
//            Bukkit.getLogger().info("itemSet Size " + 0);
//        }

        int count = 0;

        if (displaySet != null) {
            for (String displayName : displaySet) {
                if (itemSet.contains(displayName)) {
                    count++;
                }
            }
        }

        return count;
    }
}