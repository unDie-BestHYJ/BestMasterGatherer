package com.plugins.besthyj.bestmastergatherer.manager.attributeGui;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
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
    public List<ItemStack> getItemStackList(Player player) {
        AttributeGuiManager attributeGuiManager = plugin.getAttributeGuiManager();
        Map<String, FileConfiguration> guiConfigs = attributeGuiManager.getGuiConfigs();

        List<ItemStack> items = new ArrayList<ItemStack>();

        AttributeGuiItemUtil attributeGuiItemUtil = plugin.getAttributeGuiItemUtil();

        for (String guiId: guiConfigs.keySet()) {
            Map<String, AttributeGuiItem> itemMap = attributeGuiItemUtil.loadItems(CommonConstant.ATTRIBUTE_FOLDER, guiId);

            for (AttributeGuiItem attributeGuiItem: itemMap.values()) {
                int count = attributeGuiItemUtil.getCollectedCount(player, attributeGuiItem);

                ItemStack itemStack = attributeGuiItemUtil.getAttributeItemStack(attributeGuiItem, count);

                items.add(itemStack);
            }
        }

        return items;
    }

    public void addAttributeToPlayer(Player player) {
        List<ItemStack> items = getItemStackList(player);

        AttributePlusHandler.addAttributesItemStack(player, items);
    }
}
