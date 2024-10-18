package com.plugins.besthyj.bestmastergatherer.manager.collectGui;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.CommonConstant;
import com.plugins.besthyj.bestmastergatherer.constant.VariableConstant;
import com.plugins.besthyj.bestmastergatherer.listener.collectGui.CollectGuiListener;
import com.plugins.besthyj.bestmastergatherer.model.collectGui.PaginatedInventoryHolder;
import com.plugins.besthyj.bestmastergatherer.util.*;
import com.plugins.besthyj.bestmastergatherer.util.collectGui.PlayerDataStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectGuiManager {

    private final BestMasterGatherer plugin;
    private final Map<String, FileConfiguration> guiConfigs;

    public CollectGuiManager(BestMasterGatherer plugin) {
        this.plugin = plugin;
        guiConfigs = new HashMap<>();
        File pluginFolderPath = plugin.getDataFolderPath();
        loadGuiConfigs(pluginFolderPath.getAbsolutePath());
    }

    /**
     * 加载所有 GUI 配置文件
     */
    private void loadGuiConfigs(String pluginFolderPath) {
        File guiFolder = new File(pluginFolderPath, "collectGUI");
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

    /**
     * 打开分页 GUI
     *
     * @param player 玩家对象
     * @param guiId  GUI 的 ID
     * @param page   页码
     */
    public void openGui(Player player, String guiId, int page) {
        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c找不到对应的 GUI 配置文件！");
            return;
        }

        String guiName = ColorUtil.translateColorCode(config.getString("guiName"));
        List<String> layout = config.getStringList("layout");

        int totalPage = config.getInt("pages");
        if (page > totalPage || page < 1) {
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c该页不存在！");
            return;
        }

        Inventory inventory = Bukkit.createInventory(new PaginatedInventoryHolder(page), 9 * 6, guiName);

        PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();
        Map<String, Map<String, Object>> inventoryData = playerDataStorageUtil.readItemData(player.getName(), page);

        Map<String, ItemStack> itemStackMap = new HashMap<>();
        ItemStack defaultItem = itemStackMap.computeIfAbsent("D", id -> createGuiItem(config, "D", page));

        PlayerDataStorageUtil playerDataStorageUtil1 = plugin.getPlayerDataStorageUtil();

        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char itemChar = line.charAt(col);
                String itemId = String.valueOf(itemChar);

                int slot = row * 9 + col;

                if (config.contains("items." + itemId)) {
                    ItemStack item = itemStackMap.computeIfAbsent(itemId, id -> createGuiItem(config, id, page));

                    if ("L".equals(itemId) && page == 1) {
                        inventory.setItem(slot, defaultItem);
                    } else if ("N".equals(itemId) && page == totalPage) {
                        inventory.setItem(slot, defaultItem);
                    } else {
                        inventory.setItem(slot, item);
                    }
                }

                if (inventoryData != null && inventoryData.containsKey(String.valueOf(slot))) {
                    Map<String, Object> itemData = inventoryData.get(String.valueOf(slot));

                    int amount = ((Double) itemData.get("amount")).intValue();
                    String itemType = (String) itemData.get("itemType");
                    String itemName = (String) itemData.get("itemName");
                    List<String> itemLore = (List<String>) itemData.get("itemLore");
                    String nbtData = (String) itemData.get("nbtData");

                    ItemStack loadedItem = new ItemStack(Material.valueOf(itemType), amount);
                    ItemMeta meta = loadedItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(itemName);
                        meta.setLore(itemLore);
                        loadedItem.setItemMeta(meta);
                    }

                    if (nbtData != null && !nbtData.isEmpty()) {
                        loadedItem = playerDataStorageUtil1.applyNbtData(loadedItem, nbtData);
                    }

                    inventory.setItem(slot, loadedItem);
                }
            }
        }

        player.openInventory(inventory);
    }

    /**
     * 更新界面
     *
     * @param player
     * @param guiId
     * @param page
     */
    public void updateGui(Player player, String guiId, int page) {
        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c找不到对应的 GUI 配置文件！");
            return;
        }

        List<String> layout = config.getStringList("layout");
        int totalPage = config.getInt("pages");
        if (page > totalPage || page < 1) {
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c该页不存在！");
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();

        if (!(inventory.getHolder() instanceof PaginatedInventoryHolder)) {
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c当前界面无法更新！");
            return;
        }

        PaginatedInventoryHolder holder = (PaginatedInventoryHolder) inventory.getHolder();
        holder.setCurrentPage(page);

        Map<String, ItemStack> itemStackMap = new HashMap<>();
        ItemStack defaultItem = itemStackMap.computeIfAbsent("D", id -> createGuiItem(config, "D", page));

        inventory.clear();

        PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();

        Map<String, Map<String, Object>> inventoryData = playerDataStorageUtil.readItemData(player.getName(), page);

        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char itemChar = line.charAt(col);
                String itemId = String.valueOf(itemChar);
                int slot = row * 9 + col;

                if (config.contains("items." + itemId)) {
                    ItemStack item = itemStackMap.computeIfAbsent(itemId, id -> createGuiItem(config, id, page));

                    if ("L".equals(itemId) && page == 1) {
                        inventory.setItem(slot, defaultItem);
                    } else if ("N".equals(itemId) && page == totalPage) {
                        inventory.setItem(slot, defaultItem);
                    } else {
                        inventory.setItem(slot, item);
                    }
                }

                if (inventoryData != null && inventoryData.containsKey(String.valueOf(slot))) {
                    Map<String, Object> itemData = inventoryData.get(String.valueOf(slot));

                    int amount = ((Double) itemData.get("amount")).intValue();
                    String itemType = (String) itemData.get("itemType");
                    String itemName = (String) itemData.get("itemName");
                    List<String> itemLore = (List<String>) itemData.get("itemLore");
                    String nbtData = (String) itemData.get("nbtData");

                    ItemStack loadedItem = new ItemStack(Material.valueOf(itemType), amount);
                    ItemMeta meta = loadedItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(itemName);
                        meta.setLore(itemLore);
                        loadedItem.setItemMeta(meta);
                    }

                    if (nbtData != null && !nbtData.isEmpty()) {
                        loadedItem = playerDataStorageUtil.applyNbtData(loadedItem, nbtData);
                    }

                    inventory.setItem(slot, loadedItem);
                }
            }
        }

        player.updateInventory();
    }

    /**
     * 根据配置创建物品
     *
     * @param config 配置文件
     * @param itemId 物品 ID
     * @return 生成的物品
     */
    private ItemStack createGuiItem(FileConfiguration config, String itemId, int page) {
        Material material = Material.getMaterial(config.getInt("items." + itemId + ".Id"));
        int data = config.getInt("items." + itemId + ".Data", 0);
        String displayName = ColorUtil.translateColorCode(config.getString("items." + itemId + ".Display"));
        List<String> lores = config.getStringList("items." + itemId + ".Lores");
        List<String> translatedLores = new ArrayList<>();
        for (String lore : lores) {
            if (lore.contains(VariableConstant.CURRENT_PAGE)) {
                lore = lore.replace(VariableConstant.CURRENT_PAGE, String.valueOf(page));
            }

            translatedLores.add(ColorUtil.translateColorCode(lore));
        }

        ItemStack item = new ItemStack(material, 1, (short) data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(translatedLores);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 处理 GUI 内部点击逻辑
     *
     * @param event 点击事件
     */
    public void handleInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !currentItem.hasItemMeta()) {
            return;
        }
        ItemMeta itemMeta = currentItem.getItemMeta();
        String clickedItemName = itemMeta != null ? itemMeta.getDisplayName() : "";
        String inventoryTitle = event.getView().getTitle();
        String guiId = new CollectGuiListener(plugin).getGuiIdByName(inventoryTitle);

        if (guiId == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (clickedItemName.equals(ColorUtil.translateColorCode(guiConfigs.get(guiId).getString("items.L.Display")))) {
            int currentPage = getCurrentPage(event);
            if (currentPage > 1) {
                updateGui(player, guiId, currentPage - 1);
            }
        } else if (clickedItemName.equals(ColorUtil.translateColorCode(guiConfigs.get(guiId).getString("items.N.Display")))) {
            int currentPage = getCurrentPage(event);
            int totalPages = guiConfigs.get(guiId).getInt("pages");
            if (currentPage < totalPages) {
                updateGui(player, guiId, currentPage + 1);
            }
        }
    }

    /**
     * 保存当前页的所有数据
     *
     * @param player
     * @param guiId
     * @param currentPage
     */
    private void saveCurrentPageData(Player player, String guiId, int currentPage) {
        PlayerDataStorageUtil playerDataStorageUtil = plugin.getPlayerDataStorageUtil();
        playerDataStorageUtil.deleteItemData(player.getName(), currentPage);

        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
            PlayerMessage.sendMessage(player, CommonConstant.PLUGIN_NAME_PREFIX + "&c找不到对应的 GUI 配置文件！");
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (playerDataStorageUtil.getFilledSlots(guiId, "collectGUI").contains(slot)) {
                continue;
            }

            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                playerDataStorageUtil.saveItemData(player, item, currentPage, slot);
            }
        }
    }

    /**
     * 获取当前 GUI 的页码
     *
     * @param event InventoryClickEvent 对象
     * @return 当前页面编号，默认为 1
     */
    public int getCurrentPage(InventoryClickEvent event) {
        InventoryView view = event.getView();
        Inventory topInventory = view.getTopInventory();

        if (topInventory.getHolder() instanceof PaginatedInventoryHolder) {
            PaginatedInventoryHolder holder = (PaginatedInventoryHolder) topInventory.getHolder();
            return holder.getCurrentPage();
        }

        return 1;
    }

    /**
     * 清理所有资源
     */
    public void clearResources() {
        guiConfigs.clear();
    }
}