package com.plugins.besthyj.bestmastergatherer.integration.attribute;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.integration.attribute.attributeplus.AttributePlusHandler;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.AttributeGuiManager;
import com.plugins.besthyj.bestmastergatherer.model.attributeGui.AttributeGuiItem;
import com.plugins.besthyj.bestmastergatherer.util.attributeGui.AttributeGuiItemUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerAttribute {
    private BestMasterGatherer plugin;

    public PlayerAttribute(BestMasterGatherer plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取 ItemStack 列表
     *
     * @param player
     * @return
     */
//    public List<ItemStack> getItemStackList(Player player) {
//        AttributeGuiManager attributeGuiManager = plugin.getAttributeGuiManager();
//        Map<String, FileConfiguration> guiConfigs = attributeGuiManager.getGuiConfigs();
//        AttributeGuiItemUtil attributeGuiItemUtil = plugin.getAttributeGuiItemUtil();
//
//        // 使用 CompletableFuture 来异步处理每个 guiId 的操作
//        List<CompletableFuture<List<ItemStack>>> futures = new ArrayList<>();
//
//        for (String guiId : guiConfigs.keySet()) {
//            CompletableFuture<List<ItemStack>> future = CompletableFuture.supplyAsync(() -> {
//                List<ItemStack> items = new ArrayList<>();
//                Map<String, AttributeGuiItem> itemMap = attributeGuiItemUtil.loadItems(CommonConstant.ATTRIBUTE_FOLDER, guiId);
//
//                for (AttributeGuiItem attributeGuiItem : itemMap.values()) {
//                    int count = attributeGuiItemUtil.getCollectedCount(player, attributeGuiItem);
//                    ItemStack itemStack = attributeGuiItemUtil.getAttributeItemStack(attributeGuiItem, count);
//                    items.add(itemStack);
//                }
//                return items;
//            });
//
//            futures.add(future);
//        }
//
//        // 将所有 CompletableFuture 结果合并
//        List<ItemStack> allItems = futures.stream()
//                .map(CompletableFuture::join)  // 等待所有异步任务完成
//                .flatMap(List::stream)         // 将每个返回的 List<ItemStack> 展开
//                .collect(Collectors.toList()); // 收集到最终的 List<ItemStack>
//
//        return allItems;
//    }

    public List<ItemStack> getItemStackList(Player player) {
        List<ItemStack> items = new ArrayList<>();
        AttributeGuiItemUtil attributeGuiItemUtil = plugin.getAttributeGuiItemUtil();
        List<Map<String, AttributeGuiItem>> itemMapLists = new ArrayList<>();
        loadItems(itemMapLists);
        for (Map<String, AttributeGuiItem> itemMap: itemMapLists) {
            for (AttributeGuiItem attributeGuiItem: itemMap.values()) {
                int count = attributeGuiItemUtil.getCollectedCount(player, attributeGuiItem);
                ItemStack itemStack = attributeGuiItemUtil.getAttributeItemStack(attributeGuiItem, count);
                items.add(itemStack);
            }
        }
        return items;
    }

    private void loadItems(List<Map<String, AttributeGuiItem>> itemMapLists) {
        AttributeGuiManager attributeGuiManager = plugin.getAttributeGuiManager();
        Map<String, FileConfiguration> guiConfigs = attributeGuiManager.getGuiConfigs();
        AttributeGuiItemUtil attributeGuiItemUtil = plugin.getAttributeGuiItemUtil();
        guiConfigs.keySet().forEach(guiId -> {
            CompletableFuture.supplyAsync(() -> {
                // 异步加载物品
                return attributeGuiItemUtil.loadItems(CommonConstant.ATTRIBUTE_FOLDER, guiId);
            }).thenAccept(itemMap -> {
                // 回到主线程，添加结果到 itemMapLists
                synchronized (itemMapLists) {
                    itemMapLists.add(itemMap);
                }
            });
        });
    }

    public void addAttributeToPlayer(Player player) {
        List<ItemStack> items = getItemStackList(player);
        AttributePlusHandler.addAttributesItemStack(player, items);
    }
}
