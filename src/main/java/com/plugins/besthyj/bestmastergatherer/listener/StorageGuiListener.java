package com.plugins.besthyj.bestmastergatherer.listener;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.manager.StorageGuiManager;
import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StorageGuiListener implements Listener {

    private final BestMasterGatherer plugin;
    private final Map<String, String> guiNames = new HashMap<>(); // 保存 GUI 的 ID 和名称映射

    public StorageGuiListener(BestMasterGatherer plugin) {
        this.plugin = plugin;
        loadGUINames();
    }

    /**
     * 加载所有 GUI 配置文件中的 guiName
     */
    private void loadGUINames() {
        File guiFolder = new File(plugin.getDataFolderPath(), "gui");
        if (guiFolder.exists() && guiFolder.isDirectory()) {
            File[] guiFiles = guiFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (guiFiles != null) {
                for (File guiFile : guiFiles) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
                    String guiId = guiFile.getName().replace(".yml", "");

                    // 获取 guiName，如果不存在，则用 guiId 替代
                    String guiName = config.getString(guiId + ".guiName", guiId);
                    String translatedGuiName = ColorUtil.translateColorCode(guiName);

                    // 存储 GUI 的带颜色名称作为 key
                    guiNames.put(translatedGuiName, guiId);
                }
            }
        }
    }

    /**
     * 处理点击事件
     *
     * @param event 点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();

        // 获取带颜色的界面标题
        String inventoryTitle = view.getTitle();

        // 检查是否点击的是自定义 GUI
        if (guiNames.containsKey(inventoryTitle)) {  // 只用带颜色的标题进行匹配
            if (clickedInventory != null && clickedInventory.equals(view.getTopInventory())) {
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    // 取消点击事件，因为槽位中已有物品
                    event.setCancelled(true);
                } else {
                    // 允许在空槽位放置和取出物品
                    event.setCancelled(false);
                }
                StorageGuiManager.handleInventoryClick(event); // 处理 GUI 内部点击逻辑
            }
        }
    }

    /**
     * 处理拖拽事件
     *
     * @param event 拖拽事件
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryView view = event.getView();
        Inventory draggedInventory = event.getInventory();

        // 获取带颜色的界面标题
        String inventoryTitle = view.getTitle();

        // 检查是否拖动的是自定义 GUI
        if (guiNames.containsKey(inventoryTitle)) {  // 只用带颜色的标题进行匹配
            if (draggedInventory.equals(view.getTopInventory())) {
                event.setCancelled(true); // 禁止拖拽操作
            }
        }
    }
}