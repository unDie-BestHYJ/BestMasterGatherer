package com.plugins.besthyj.bestmastergatherer.listener.collectGui;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.PlayerAttribute;
import com.plugins.besthyj.bestmastergatherer.manager.collectGui.CollectGuiManager;
import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;
import com.plugins.besthyj.bestmastergatherer.util.collectGui.PlayerDataStorageUtil;
import com.plugins.besthyj.bestmastergatherer.model.collectGui.PaginatedInventoryHolder;
import com.plugins.besthyj.bestmastergatherer.util.PlayerMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectGuiListener implements Listener {

    private final BestMasterGatherer plugin;
    private final Map<String, String> guiNames = new HashMap<>(); // 保存 GUI 的 ID 和名称映射

    public CollectGuiListener(BestMasterGatherer plugin) {
        this.plugin = plugin;
        loadGUINames();
    }

    /**
     * 加载所有 GUI 配置文件中的 guiName
     */
    private void loadGUINames() {
        File guiFolder = new File(plugin.getDataFolderPath(), "collectGUI");
        if (guiFolder.exists() && guiFolder.isDirectory()) {
            File[] guiFiles = guiFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (guiFiles != null) {
                for (File guiFile : guiFiles) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
                    String guiId = guiFile.getName().replace(".yml", "");

                    // 获取 guiName，如果不存在，则用 guiId 替代
                    String guiName = config.getString("guiName", guiId);
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

        // 获取带颜色的界面标题
        String inventoryTitle = view.getTitle();

        PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();

        CollectGuiManager collectGuiManager = plugin.getCollectGuiManager();

        // 检查是否点击的是自定义 GUI
        if (guiNames.containsKey(inventoryTitle)) {

            if (clickedInventory != null && clickedInventory.equals(view.getTopInventory())) {
                List<Integer> filledSlots = playerDataStorageUtil.getFilledSlots(guiNames.get(inventoryTitle), "collectGUI");

                int clickedSlot = event.getSlot();
                if (filledSlots.contains(clickedSlot)) {
                    event.setCancelled(true);
                } else {
                    event.setCancelled(false);
                }
                collectGuiManager.handleInventoryClick(event); // 处理 GUI 内部点击逻辑
            }
        }
    }

    /**
     * 处理界面关闭事件，统一保存所有物品数据
     *
     * @param event 界面关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof PaginatedInventoryHolder) {
            Player player = (Player) event.getPlayer();
            Inventory closedInventory = event.getInventory();
            InventoryView view = event.getView();
            String inventoryTitle = view.getTitle();
            String guiId = guiNames.get(inventoryTitle);

            int page = ((PaginatedInventoryHolder) closedInventory.getHolder()).getCurrentPage();

            // 删除旧文件
            PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();
            playerDataStorageUtil.deleteItemData(player.getName(), page);

            for (int slot = 0; slot < closedInventory.getSize(); slot++) {
                if (playerDataStorageUtil.getFilledSlots(guiId, "collectGUI").contains(slot)) {continue;}
                ItemStack item = closedInventory.getItem(slot);
                if (item != null) {

                    playerDataStorageUtil.saveItemData(player, item, page, slot);
                }
            }

            PlayerMessage.sendMessage(player, "&a你的仓库物品已保存！");

            PlayerAttribute playerAttribute = plugin.getPlayerAttribute();
            playerAttribute.addAttributeToPlayer(player);

            PlayerMessage.sendMessage(player, "&6你的属性已更新！");
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
        if (guiNames.containsKey(inventoryTitle)) {
            if (draggedInventory.equals(view.getTopInventory())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 根据 GUI 的名称获取对应的 GUI ID
     *
     * @param guiName 带颜色的 GUI 名称
     * @return GUI ID，如果不存在返回 null
     */
    public String getGuiIdByName(String guiName) {
        return guiNames.get(guiName);
    }

    /**
     * 清理所有资源
     */
    public void clearResources() {
        guiNames.clear();

}