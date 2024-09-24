package com.plugins.besthyj.bestmastergatherer.integration.attribute.attributeplus;

import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.serverct.ersha.AttributePlus;
import org.serverct.ersha.api.AttributeAPI;
import org.serverct.ersha.attribute.data.AttributeData;

import java.util.ArrayList;
import java.util.List;

public class AttributePlusHandler {

    /**
     * 增加新的属性源
     *
     * @param player
     * @param item
     */
    public static void addAttributesItemStack(Player player, ItemStack item) {
        removeAttributeSource(player);
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
    public static void addAttributesItemStack(Player player, List<ItemStack> itemStackList) {
        removeAttributeSource(player);
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
}
