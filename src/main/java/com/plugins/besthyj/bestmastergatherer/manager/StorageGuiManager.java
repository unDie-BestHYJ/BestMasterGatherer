package com.plugins.besthyj.bestmastergatherer.manager;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;
import com.plugins.besthyj.bestmastergatherer.util.PlayerMessage;
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

public class StorageGuiManager {

    private static BestMasterGatherer plugin;
    private final static Map<String, FileConfiguration> guiConfigs = new HashMap<>();

    public static void init(BestMasterGatherer pluginInstance) {
        plugin = pluginInstance;
        File pluginFolderPath = plugin.getDataFolderPath();
        loadGuiConfigs(pluginFolderPath.getAbsolutePath());
    }

    /**
     * 加载所有 GUI 配置文件
     */
    private static void loadGuiConfigs(String pluginFolderPath) {
        File guiFolder = new File(pluginFolderPath, "collectGUI");
        if (guiFolder.getAbsoluteFile().exists() && guiFolder.getAbsoluteFile().isDirectory()) {
            File[] guiFiles = guiFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (guiFiles != null) {
                for (File guiFile : guiFiles) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
                    String guiId = guiFile.getName().replace(".yml", "");

                    Bukkit.getLogger().info(guiId);

                    guiConfigs.put(guiId, config);
                }
            }
        }
    }

    /**
     * 打开分页 GUI
     *
     * @param player 玩家对象
     * @param guiId  GUI 的 ID
     * @param page   页码
     */
    public static void openGui(Player player, String guiId, int page) {
        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
            Bukkit.getLogger().info(guiConfigs.size() + "");
            PlayerMessage.sendMessage(player, "&c找不到对应的 GUI 配置文件！");
            return;
        }

        String guiName = ColorUtil.translateColorCode(config.getString("guiName"));
        List<String> layout = config.getStringList("layout");

        // 每页对应的布局
        if (page > config.getInt("pages") || page < 1) {
            PlayerMessage.sendMessage(player, "&c该页不存在！");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 9 * 6, guiName);

        // 设置每个槽位的物品
        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char itemChar = line.charAt(col);
                String itemId = String.valueOf(itemChar);

                // 根据配置文件中的 itemId 获取对应物品
                if (config.contains("items." + itemId)) {
                    ItemStack item = createGuiItem(config, itemId);
                    inventory.setItem(row * 9 + col, item);
                }
            }
        }

        // 打开 GUI
        player.openInventory(inventory);
    }

    /**
     * 根据配置创建物品
     *
     * @param config 配置文件
     * @param itemId 物品 ID
     * @return 生成的物品
     */
    private static ItemStack createGuiItem(FileConfiguration config, String itemId) {
        Material material = Material.getMaterial(config.getInt("items." + itemId + ".Id"));
        int data = config.getInt("items." + itemId + ".Data", 0);
        String displayName = ColorUtil.translateColorCode(config.getString("items." + itemId + ".Display"));
        List<String> lores = config.getStringList("items." + itemId + ".Lores");
        List<String> translatedLores = new ArrayList<>();
        for (String lore : lores) {
            translatedLores.add(ColorUtil.translateColorCode(lore));
        }

        ItemStack item = new ItemStack(material, 1, (short) data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lores);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 处理 GUI 内部点击逻辑
     *
     * @param event 点击事件
     */
    public static void handleInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        // 在这里处理点击事件逻辑，例如切换页面等操作
    }
}