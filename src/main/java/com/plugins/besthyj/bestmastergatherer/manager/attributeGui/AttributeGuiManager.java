package com.plugins.besthyj.bestmastergatherer.manager.attributeGui;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.model.attributeGui.AttributeGuiItem;
import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;
import com.plugins.besthyj.bestmastergatherer.util.PlayerMessage;
import com.plugins.besthyj.bestmastergatherer.util.attributeGui.AttributeGuiItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeGuiManager {
    private final BestMasterGatherer plugin;
    private final Map<String, FileConfiguration> guiConfigs;

    public AttributeGuiManager(BestMasterGatherer plugin) {
        this.plugin = plugin;
        this.guiConfigs = new HashMap<>();
        File pluginFolderPath = plugin.getDataFolderPath();
        loadGuiConfigs(pluginFolderPath.getAbsolutePath());
    }

    /**
     * 加载所有 GUI 配置文件
     */
    private void loadGuiConfigs(String pluginFolderPath) {
        File guiFolder = new File(pluginFolderPath, "attributeGUI");
        if (guiFolder.exists() && guiFolder.isDirectory()) {
            File[] guiFiles = guiFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (guiFiles != null) {
                for (File guiFile : guiFiles) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
                    String guiId = guiFile.getName().replace(".yml", "");
                    guiConfigs.put(guiId, config);
                }
            }
        }
    }

    public Map<String, FileConfiguration> getGuiConfigs() {
        return guiConfigs;
    }

    /**
     * 打开gui
     *
     * @param player
     * @param guiId
     */
    public void openGui(Player player, String guiId) {
        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c找不到对应的 GUI 配置文件！");
            return;
        }

        String guiName = ColorUtil.translateColorCode(config.getString("guiName"));
        List<String> layout = config.getStringList("layout");
        Inventory inventory = Bukkit.createInventory(null, 9 * (!layout.isEmpty() ? layout.size() : 1 ), guiName);
        AttributeGuiItemUtil attributeGuiItemUtil = plugin.getAttributeGuiItemUtil();
        Map<String, AttributeGuiItem> itemMap = attributeGuiItemUtil.loadItems(CommonConstant.ATTRIBUTE_FOLDER, guiId);

        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            int col = 0;

            for (int i = 0; i < line.length(); i++) {
                char itemChar = line.charAt(i);
                String itemId = "";

                if (itemChar == '`') {
                    int endIndex = line.indexOf('`', i + 1);
                    if (endIndex != -1) {
                        itemId = line.substring(i + 1, endIndex);
                        i = endIndex; // 跳过反引号部分
                    }
                } else {
                    itemId = String.valueOf(itemChar);
                }

                if (config.contains("items." + itemId)) {
                    AttributeGuiItem attributeItem = null;
                    if (itemMap != null) {
                        attributeItem = itemMap.get(itemId);
                    }
                    if (attributeItem == null) {
                        continue;
                    }

                    int count = attributeGuiItemUtil.getCollectedCount(player, attributeItem);
//                    Bukkit.getLogger().info(attributeItem.getItemName());
                    ItemStack itemStack = attributeGuiItemUtil.createGuiItemFromAttributeItem(attributeItem, count, player);
                    int slot = row * 9 + col;
                    inventory.setItem(slot, itemStack);
                }
                col++;
            }
        }
        player.openInventory(inventory);
    }

    /**
     * 清理所有资源
     */
    public void clearResources() {
        guiConfigs.clear();
    }
}
