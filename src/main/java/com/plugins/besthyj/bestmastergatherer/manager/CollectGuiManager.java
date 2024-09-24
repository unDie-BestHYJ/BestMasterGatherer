package com.plugins.besthyj.bestmastergatherer.manager;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.VariableConstant;
import com.plugins.besthyj.bestmastergatherer.listener.CollectGuiListener;
import com.plugins.besthyj.bestmastergatherer.util.*;
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
    public static void openGui(Player player, String guiId, int page) {
        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
//            Bukkit.getLogger().info(guiConfigs.size() + "");
            PlayerMessage.sendMessage(player, "&c找不到对应的 GUI 配置文件！");
            return;
        }

        String guiName = ColorUtil.translateColorCode(config.getString("guiName"));
        List<String> layout = config.getStringList("layout");

        int totalPage = config.getInt("pages");
        if (page > totalPage || page < 1) {
            PlayerMessage.sendMessage(player, "&c该页不存在！");
            return;
        }

        Inventory inventory = Bukkit.createInventory(new PaginatedInventoryHolder(page), 9 * 6, guiName);

        Map<String, Map<String, Object>> inventoryData = FileGetUtil.readItemData(player.getName(), page);

        Map<String, ItemStack> itemStackMap = new HashMap<>();
        ItemStack defaultItem = itemStackMap.computeIfAbsent("D", id -> createGuiItem(config, "D", page));

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

                    // 将物品数量从 Double 转换为 int
                    int amount = ((Double) itemData.get("amount")).intValue();
                    String itemType = (String) itemData.get("itemType"); // 物品类型
                    String itemName = (String) itemData.get("itemName"); // 物品显示名称
                    List<String> itemLore = (List<String>) itemData.get("itemLore"); // 物品描述
                    String nbtData = (String) itemData.get("nbtData");

                    // 使用物品类型创建 ItemStack
                    ItemStack loadedItem = new ItemStack(Material.valueOf(itemType), amount);
                    ItemMeta meta = loadedItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(itemName); // 设置显示名称
                        meta.setLore(itemLore); // 设置物品描述
                        loadedItem.setItemMeta(meta);
                    }

                    if (nbtData != null && !nbtData.isEmpty()) {
                        FileGetUtil.applyNbtData(loadedItem, nbtData); // 调用应用 NBT 的方法
                    }

                    inventory.setItem(slot, loadedItem); // 将读取的物品设置到库存中
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
    public static void updateGui(Player player, String guiId, int page) {
        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
            PlayerMessage.sendMessage(player, "&c找不到对应的 GUI 配置文件！");
            return;
        }

        List<String> layout = config.getStringList("layout");
        int totalPage = config.getInt("pages");
        if (page > totalPage || page < 1) {
            PlayerMessage.sendMessage(player, "&c该页不存在！");
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();  // 获取当前打开的 GUI

        // 如果当前不是 PaginatedInventoryHolder 的界面，直接返回
        if (!(inventory.getHolder() instanceof PaginatedInventoryHolder)) {
            PlayerMessage.sendMessage(player, "&c当前界面无法更新！");
            return;
        }

        // 更新当前 holder 的页面
        PaginatedInventoryHolder holder = (PaginatedInventoryHolder) inventory.getHolder();
        holder.setCurrentPage(page);  // 更新当前页数

        Map<String, ItemStack> itemStackMap = new HashMap<>();
        ItemStack defaultItem = itemStackMap.computeIfAbsent("D", id -> createGuiItem(config, "D", page));

        // 清空现有物品
        inventory.clear();

        // 更新布局中的物品
        Map<String, Map<String, Object>> inventoryData = FileGetUtil.readItemData(player.getName(), page); // 从存储中读取当前页的数据

        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char itemChar = line.charAt(col);
                String itemId = String.valueOf(itemChar);
                int slot = row * 9 + col; // 计算当前槽位

                // 根据配置文件创建物品
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

                // 更新当前槽位的物品
                if (inventoryData != null && inventoryData.containsKey(String.valueOf(slot))) {
                    Map<String, Object> itemData = inventoryData.get(String.valueOf(slot));

                    // 将物品数量从 Double 转换为 int
                    int amount = ((Double) itemData.get("amount")).intValue();
                    String itemType = (String) itemData.get("itemType"); // 物品类型
                    String itemName = (String) itemData.get("itemName"); // 物品显示名称
                    List<String> itemLore = (List<String>) itemData.get("itemLore"); // 物品描述
                    String nbtData = (String) itemData.get("nbtData");

                    // 使用物品类型创建 ItemStack
                    ItemStack loadedItem = new ItemStack(Material.valueOf(itemType), amount);
                    ItemMeta meta = loadedItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(itemName); // 设置显示名称
                        meta.setLore(itemLore); // 设置物品描述
                        loadedItem.setItemMeta(meta);
                    }

                    if (nbtData != null && !nbtData.isEmpty()) {
                        FileGetUtil.applyNbtData(loadedItem, nbtData); // 调用应用 NBT 的方法
                    }

                    inventory.setItem(slot, loadedItem); // 将读取的物品设置到库存中
                }
            }
        }

        player.updateInventory();  // 刷新玩家的界面
    }

    /**
     * 根据配置创建物品
     *
     * @param config 配置文件
     * @param itemId 物品 ID
     * @return 生成的物品
     */
    private static ItemStack createGuiItem(FileConfiguration config, String itemId, int page) {
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
    public static void handleInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !currentItem.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = currentItem.getItemMeta();
        String clickedItemName = itemMeta != null ? itemMeta.getDisplayName() : "";
        String inventoryTitle = event.getView().getTitle();
        String guiId = new CollectGuiListener(plugin).getGuiIdByName(inventoryTitle); // 获取 GUI ID

        if (guiId == null) {
            return; // 如果没有找到 GUI ID，则退出
        }

        Player player = (Player) event.getWhoClicked();

        // 检查是否点击了“上一页”或“下一页”
        if (clickedItemName.equals(ColorUtil.translateColorCode(guiConfigs.get(guiId).getString("items.L.Display")))) {
            // 处理“上一页”
            int currentPage = getCurrentPage(event);
            if (currentPage > 1) {
                saveCurrentPageData(player, guiId, currentPage);
                updateGui((Player) event.getWhoClicked(), guiId, currentPage - 1);
            }
        } else if (clickedItemName.equals(ColorUtil.translateColorCode(guiConfigs.get(guiId).getString("items.N.Display")))) {
            // 处理“下一页”
            int currentPage = getCurrentPage(event);
            int totalPages = guiConfigs.get(guiId).getInt("pages");

            if (currentPage < totalPages) {
                saveCurrentPageData(player, guiId, currentPage);
                updateGui((Player) event.getWhoClicked(), guiId, currentPage + 1);
            }
        }
    }

    private static void saveCurrentPageData(Player player, String guiId, int currentPage) {
        // 删除旧文件
        PlayerDataStorageUtil.deleteItemData(player.getName(), currentPage);

        FileConfiguration config = guiConfigs.get(guiId);
        if (config == null) {
//            Bukkit.getLogger().info(guiConfigs.size() + "");
            PlayerMessage.sendMessage(player, "&c找不到对应的 GUI 配置文件！");
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();

        // 遍历当前打开的库存，保存每个槽位的数据
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (PlayerDataStorageUtil.getFilledSlots(guiId, "collectGUI").contains(slot)) {
                continue;
            }

            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                PlayerDataStorageUtil.saveItemData(player, item, currentPage, slot);
            }
        }
    }

    /**
     * 获取当前 GUI 的页码
     *
     * @param event InventoryClickEvent 对象
     * @return 当前页面编号，默认为 1
     */
    public static int getCurrentPage(InventoryClickEvent event) {
        InventoryView view = event.getView(); // 从事件中获取 InventoryView
        Inventory topInventory = view.getTopInventory(); // 获取顶层的 Inventory

        // 检查 Inventory 的 Holder 是否为自定义的 PaginatedInventoryHolder
        if (topInventory.getHolder() instanceof PaginatedInventoryHolder) {
            PaginatedInventoryHolder holder = (PaginatedInventoryHolder) topInventory.getHolder();
            return holder.getCurrentPage(); // 返回当前页面
        }

        // 如果没有找到当前页面信息，返回默认的第一页
        return 1;
    }

    /**
     * 清理所有资源
     */
    public static void clearResources() {
        // 清空 GUI 配置
        guiConfigs.clear();
        plugin = null; // 清除插件引用
    }
}