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
    private final BestMasterGatherer plugin;

    public AttributeGuiItemUtil(BestMasterGatherer plugin) {
        this.plugin = plugin;
    }

    public Map<String, AttributeGuiItem> loadItems(String folder, String guiId) {
        Map<String, AttributeGuiItem> itemMap = new HashMap<>();

        File directory = new File(plugin.getDataFolder(), folder);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".yml") && file.getName().replace(".yml", "").equals(guiId)) {
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
    public ItemStack createGuiItemFromAttributeItem(AttributeGuiItem attributeItem, int count, Player player) {
        int id = attributeItem.getItemTypeId();
        int data = attributeItem.getItemTypeData();

        ItemStack itemStack = new ItemStack(Material.getMaterial(id), 1, (short) data);
        ItemMeta meta = itemStack.getItemMeta();

        String displayName = ColorUtil.translateColorCode(attributeItem.getItemName());
        meta.setDisplayName(displayName);

        List<String> lores = attributeItem.getLoresList();
        List<String> translatedLores = new ArrayList<>();
        for (String lore : lores) {
            if (lore.contains(VariableConstant.COLLECTION_INFO)) {
                Set<String> collectionNameList = getDisplaySet(attributeItem);
                Set<String> collectedNameList = getCollectedSet(player, attributeItem);
                List<String> collectionLores = new ArrayList<>();
                for (String mmItemName : collectionNameList) {
                    if (collectedNameList.contains(mmItemName)) {
                        collectionLores.add(ColorUtil.translateColorCode(mmItemName + " " + CommonConstant.COLLECTED));
                    } else {
                        collectionLores.add(ColorUtil.translateColorCode(mmItemName + " " + CommonConstant.UNCOLLECTED));
                    }
                }
                lore = String.join("\n", collectionLores);
            }

            Pattern usefulPattern = Pattern.compile(VariableConstant.IS_USEFUL_REGEX);
            Matcher usefulMatcher = usefulPattern.matcher(lore);

            StringBuffer updatedLore = new StringBuffer();

            while (usefulMatcher.find()) {
                int number = Integer.parseInt(usefulMatcher.group(1));

                String replacement = (count >= number) ? CommonConstant.COLLECTED : CommonConstant.UNCOLLECTED;

                usefulMatcher.appendReplacement(updatedLore, replacement);
            }
            usefulMatcher.appendTail(updatedLore);

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
    public ItemStack createGuiItem(FileConfiguration config, String itemId) {
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

    public ItemStack getAttributeItemStack(AttributeGuiItem attributeItem, int count) {
        int id = attributeItem.getItemTypeId();
        int data = attributeItem.getItemTypeData();

        ItemStack itemStack = new ItemStack(Material.getMaterial(id), 1, (short) data);
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName = ColorUtil.translateColorCode(attributeItem.getItemName());
        itemMeta.setDisplayName(displayName);

        Map<Integer, List<String>> attributesMap = attributeItem.getAttributesMap();

        List<String> attrLores = new ArrayList<>();

        for (Map.Entry<Integer, List<String>> entry : attributesMap.entrySet()) {
            Integer number = entry.getKey();
            List<String> attrList = entry.getValue();

            if (count >= number) {
                for (String attr : attrList) {
                    attrLores.add(ColorUtil.translateColorCode(attr));
                }
            }
        }

        itemMeta.setLore(attrLores);
        itemStack.setItemMeta(itemMeta);

        Bukkit.getLogger().info(itemStack.toString());

        return itemStack;
    }

    /**
     * 获取 mm物品名 集合
     *
     *
     *
     * @param item
     * @return
     */
    // TODO
    // 这两个方法都会读取玩家的物品数据并检查是否与 MythicMobs 的物品集合匹配。建议缓存这些集合，避免频繁读取和处理。特别是在高并发的情况下，频繁从文件中读取和操作会导致性能瓶颈。
    // 建议：可以考虑为每个玩家缓存 displaySet 和 collectedDisplaySet，只在玩家数据更新或物品发生变化时重新生成。
    public LinkedHashSet<String> getDisplaySet(AttributeGuiItem item) {
        List<String> mmItems = item.getMMItemsList();
        if (mmItems == null || mmItems.isEmpty()) {
            return null;
        }

        LinkedHashSet<String> displaySet = new LinkedHashSet<>();

        for (String mmItem : mmItems) {
            String displayName = MythicMobsUtils.getMythicItemDisplayName(mmItem);

            displaySet.add(displayName);
        }

        return displaySet;
    }

    /**
     * 获取已经收集的物品数量
     *
     * @param player
     * @param attributeGuiItem
     * @return
     */
    public Integer getCollectedCount(Player player, AttributeGuiItem attributeGuiItem) {
        Set<String> displaySet = getDisplaySet(attributeGuiItem);

        PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();
        Map<String, Integer> stringIntegerMap = playerDataStorageUtil.readItems(player.getName());

        Set<String> itemSet  = null;
        if (stringIntegerMap != null) {
            itemSet = stringIntegerMap.keySet();
        }

        int count = 0;

        if (displaySet != null && itemSet != null) {
            for (String displayName : displaySet) {
                if (itemSet.contains(displayName)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * 获取已经收集的物品集合
     *
     * @param player
     * @param attributeGuiItem
     * @return
     */
    public Set<String> getCollectedSet(Player player, AttributeGuiItem attributeGuiItem) {
        Set<String> displaySet = getDisplaySet(attributeGuiItem);
        Set<String> collectedDisplaySet = new HashSet<>();

        PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();
        Map<String, Integer> stringIntegerMap = playerDataStorageUtil.readItems(player.getName());

        Set<String> itemSet  = null;
        if (stringIntegerMap != null) {
            itemSet = stringIntegerMap.keySet();
        }

        if (displaySet != null && itemSet != null) {
            for (String displayName : displaySet) {
                if (itemSet.contains(displayName)) {
                    collectedDisplaySet.add(displayName);
                }
            }
        }

        return collectedDisplaySet;
    }
}
