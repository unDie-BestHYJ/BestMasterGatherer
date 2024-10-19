package com.plugins.besthyj.bestmastergatherer.integration.attribute;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.integration.attribute.attributeplus.AttributePlusHandler;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.AttributeGuiManager;
import com.plugins.besthyj.bestmastergatherer.model.attributeGui.AttributeGuiItem;
import com.plugins.besthyj.bestmastergatherer.util.attributeGui.AttributeGuiItemUtil;
import org.bukkit.Bukkit;
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

    public CompletableFuture<List<ItemStack>> getItemStackListAsync(Player player) {
        AttributeGuiItemUtil attributeGuiItemUtil = plugin.getAttributeGuiItemUtil();
        List<Map<String, AttributeGuiItem>> itemMapLists = new ArrayList<>();

        return CompletableFuture.supplyAsync(() -> {
            loadItems(itemMapLists); // 假设这是同步阻塞的
            return itemMapLists;
        }).thenApply(itemMapListsResult -> {
            List<ItemStack> items = new ArrayList<>();
            for (Map<String, AttributeGuiItem> itemMap : itemMapListsResult) {
                for (AttributeGuiItem attributeGuiItem : itemMap.values()) {
                    int count = attributeGuiItemUtil.getCollectedCount(player, attributeGuiItem);
                    ItemStack itemStack = attributeGuiItemUtil.getAttributeItemStack(attributeGuiItem, count);
                    items.add(itemStack);
                }
            }
            return items;
        });
    }

    private void loadItems(List<Map<String, AttributeGuiItem>> itemMapLists) {
        AttributeGuiManager attributeGuiManager = plugin.getAttributeGuiManager();
        Map<String, FileConfiguration> guiConfigs = attributeGuiManager.getGuiConfigs();
        AttributeGuiItemUtil attributeGuiItemUtil = plugin.getAttributeGuiItemUtil();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        guiConfigs.keySet().forEach(guiId -> {
            Map<String, AttributeGuiItem> itemMap = attributeGuiItemUtil.loadItems(CommonConstant.ATTRIBUTE_FOLDER, guiId);
            itemMapLists.add(itemMap);
        });
        if (itemMapLists.isEmpty()) {
            Bukkit.getLogger().info("itemMapLists is empty");
        }
        itemMapLists.forEach(itemMap -> {
            itemMap.forEach((key, value) -> {
                Bukkit.getLogger().info("Key: " + key + ", Value: " + value.toString());
            });
        });
    }

    public void addAttributeToPlayer(Player player) {
        getItemStackListAsync(player).thenAccept(items -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                AttributePlusHandler.addAttributesItemStack(player, items);
                Bukkit.getLogger().info("玩家属性更新完毕");
            });
        });
    }
}
