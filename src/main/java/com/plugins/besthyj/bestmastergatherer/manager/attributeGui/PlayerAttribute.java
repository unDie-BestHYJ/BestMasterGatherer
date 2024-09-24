package com.plugins.besthyj.bestmastergatherer.manager.attributeGui;

import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.integration.attribute.attributeplus.AttributePlusHandler;
import com.plugins.besthyj.bestmastergatherer.model.attributeGui.AttributeGuiItem;
import com.plugins.besthyj.bestmastergatherer.util.attributeGui.AttributeGuiItemUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerAttribute {
    /**
     * 获取 ItemStack 列表
     *
     * @param player
     * @return
     */
    public static List<ItemStack> getItemStackList(Player player) {
        Map<String, FileConfiguration> guiConfigs = AttributeGuiManager.getGuiConfigs();

        List<ItemStack> items = new ArrayList<ItemStack>();

        for (String guiId: guiConfigs.keySet()) {
            Map<String, AttributeGuiItem> itemMap = AttributeGuiItemUtil.loadItems(CommonConstant.ATTRIBUTE_FOLDER, guiId);

            for (AttributeGuiItem attributeGuiItem: itemMap.values()) {
                int count = AttributeGuiItemUtil.getCollectedCount(player, attributeGuiItem);

                ItemStack itemStack = AttributeGuiItemUtil.getAttributeItemStack(attributeGuiItem, count);

                items.add(itemStack);
            }
        }

        return items;
    }

    public static void addAttributeToPlayer(Player player) {
        List<ItemStack> items = getItemStackList(player);

        AttributePlusHandler.addAttributesItemStack(player, items);
    }
}
