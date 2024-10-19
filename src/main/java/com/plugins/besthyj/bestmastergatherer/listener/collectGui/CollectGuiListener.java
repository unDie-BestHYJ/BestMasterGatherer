package com.plugins.besthyj.bestmastergatherer.listener.collectGui;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.integration.attribute.PlayerAttribute;
import com.plugins.besthyj.bestmastergatherer.manager.collectGui.CollectGuiManager;
import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;
import com.plugins.besthyj.bestmastergatherer.util.collectGui.PlayerDataStorageUtil;
import com.plugins.besthyj.bestmastergatherer.model.collectGui.PaginatedInventoryHolder;
import com.plugins.besthyj.bestmastergatherer.util.PlayerMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.concurrent.CompletableFuture;

public class CollectGuiListener implements Listener {

    private final BestMasterGatherer plugin;
    private final Map<String, String> guiNames = new HashMap<>();
    private final Map<String, List<Integer>> filledSlotsMap = new HashMap<>();

    private Map<Player, Long> lastClickTime = new HashMap<>();
    private static final long CLICK_INTERVAL = 500;

    public CollectGuiListener(BestMasterGatherer plugin) {
        this.plugin = plugin;
        loadGUINames();
        loadGUILayoutSlots();
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
                    String guiName = config.getString("guiName", guiId);
                    String translatedGuiName = ColorUtil.translateColorCode(guiName);
                    guiNames.put(translatedGuiName, guiId);
                }
            }
        }
    }

    private void loadGUILayoutSlots() {
        File guiFolder = new File(plugin.getDataFolderPath(), "collectGUI");
        if (guiFolder.exists() && guiFolder.isDirectory()) {
            File[] guiFiles = guiFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (guiFiles != null) {
                for (File guiFile : guiFiles) {
                    String guiId = guiFile.getName().replace(".yml", "");
                    List<Integer> filledSlots = plugin.getPlayerDataStorageUtil().getFilledSlots(guiId, "collectGUI");
                    filledSlotsMap.put(guiId, filledSlots);
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
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        InventoryView view = event.getView();
        String inventoryTitle = view.getTitle();

        if (!guiNames.containsKey(inventoryTitle)) {
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c禁止 shift+鼠标 的点击操作");
            return;
        }

        String guiId = guiNames.get(inventoryTitle);

        PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();
        List<Integer> filledSlots = filledSlotsMap.get(guiId);

        CollectGuiManager collectGuiManager = plugin.getCollectGuiManager();
        collectGuiManager.handleInventoryClick(event);

        if (guiNames.containsKey(inventoryTitle)) {

            long currentTime = System.currentTimeMillis();
            if (lastClickTime.containsKey(player)) {
                long lastTime = lastClickTime.get(player);
                if (currentTime - lastTime < CLICK_INTERVAL) {
                    PlayerMessage.sendMessage(player, "&c你点击太快了，请稍后再试！");
                    event.setCancelled(true);
                    return;
                }
            }

            lastClickTime.put(player, currentTime);

            if (clickedInventory != null && clickedInventory.equals(view.getTopInventory())) {
                if (filledSlots.contains(event.getSlot())) {
                    event.setCancelled(true);
                    return;
                }
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    if (player.getInventory().firstEmpty() != -1) {
                        clickedInventory.setItem(event.getSlot(), null);
                        int page = ((PaginatedInventoryHolder) clickedInventory.getHolder()).getCurrentPage();
                        CompletableFuture.runAsync(() -> {
                            playerDataStorageUtil.deleteItemData(player.getName(), page, event.getSlot());
                        }).thenRun(() -> {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.getInventory().addItem(clickedItem);
                                PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&a物品已放入背包！");
                            });
                        });
                    } else {
                        PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c背包已满，请先清理背包！");
                    }
                }
            } else if (clickedInventory != null && clickedInventory.equals(player.getInventory())) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    Inventory topInventory = view.getTopInventory();
                    int emptySlot = topInventory.firstEmpty();
                    if (emptySlot != -1) {
                        event.setCancelled(true);
                        int page = ((PaginatedInventoryHolder) topInventory.getHolder()).getCurrentPage();
                        player.getInventory().setItem(event.getSlot(), null);
                        CompletableFuture.runAsync(() -> {
                            playerDataStorageUtil.saveItemData(player, clickedItem, page, emptySlot);
                        }).thenRun(() -> {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                topInventory.setItem(emptySlot, clickedItem);
                                PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&a物品已移入仓库！");
                            });
                        });
                    } else {
                        event.setCancelled(true);
                        PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c该页仓库已满，无法放入更多物品！");
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    /**
     * 处理界面关闭事件，统一保存所有物品数据
     *
     * @param event 界面关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        String inventoryTitle = view.getTitle();

        if (!guiNames.containsKey(inventoryTitle)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (event.getInventory().getHolder() instanceof PaginatedInventoryHolder) {
            PlayerAttribute playerAttribute = plugin.getPlayerAttribute();
            playerAttribute.addAttributeToPlayer(player);
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&6你的属性已更新！");
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
        String inventoryTitle = view.getTitle();

        if (guiNames.containsKey(inventoryTitle)) {
            if (draggedInventory.equals(view.getTopInventory())) {
                event.setCancelled(true);
                event.getWhoClicked().setItemOnCursor(null);
                return;
            }

            if (draggedInventory.equals(event.getWhoClicked().getInventory())) {
                event.setCancelled(true);
                event.getWhoClicked().setItemOnCursor(null);
                return;
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
}