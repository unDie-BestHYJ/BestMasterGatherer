package com.plugins.besthyj.bestmastergatherer.attribute;

import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.util.FileStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.serverct.ersha.AttributePlus;
import org.serverct.ersha.api.AttributeAPI;
import org.serverct.ersha.attribute.data.AttributeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributePlusHandler {

    /**
     * 增加新的属性源
     *
     * @param player
     * @param item
     */
    private static void addAttributesItemStack(Player player, ItemStack item) {
        AttributeData attributeData = AttributePlus.attributeManager.getAttributeData(player);
        List<ItemStack> itemStackList = new ArrayList<>();
        itemStackList.add(item);
        AttributeAPI.addSourceAttributeFromItems(attributeData, CommonConstant.PLUGIN_NAME, itemStackList);

    }

    /**
     * 增加新的属性源
     *
     * @param player
     * @param itemStackList
     */
    private static void addAttributesItemStack(Player player, List<ItemStack> itemStackList) {
        AttributeData attributeData = AttributePlus.attributeManager.getAttributeData(player);
        AttributeAPI.addSourceAttributeFromItems(attributeData, CommonConstant.PLUGIN_NAME, itemStackList);
    }

    /**
     * 删除已有的属性源
     *
     * @param player
     */
    private static void removeAttributeSource(Player player) {
        AttributeData attributeData = AttributePlus.attributeManager.getAttributeData(player);
        AttributeAPI.takeSourceAttribute(attributeData, CommonConstant.PLUGIN_NAME);
    }

    /**
     * 对外接口
     * 更新该插件属性源
     *
     * @param player
     */
    public static void updateAttributeByPlayer(Player player) {
        removeAttributeSource(player);
        Map<String, List<ItemStack>> playerItemsMap = FileStorageUtil.loadAllStoredItems();
        String playerName = player.getName();
        if (playerItemsMap.containsKey(playerName)) {
            List<ItemStack> itemStacks = playerItemsMap.get(playerName);
            addAttributesItemStack(player, itemStacks);
        }
    }
}
