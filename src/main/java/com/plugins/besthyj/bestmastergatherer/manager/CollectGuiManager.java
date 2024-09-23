package com.plugins.besthyj.bestmastergatherer.manager;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.constant.VariableConstant;
import com.plugins.besthyj.bestmastergatherer.listener.CollectGuiListener;
import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;
import com.plugins.besthyj.bestmastergatherer.util.PaginatedInventoryHolder;
import com.plugins.besthyj.bestmastergatherer.util.PlayerMessage;
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
            Bukkit.getLogger().info(guiConfigs.size() + "");
            PlayerMessage.sendMessage(player, "&c找不到对应的 GUI 配置文件！");
            return;
        }

        String guiName = ColorUtil.translateColorCode(config.getString("guiName"));
        List<String> layout = config.getStringList("layout");

        int totlePage = config.getInt("pages");
        if (page > totlePage || page < 1) {
            PlayerMessage.sendMessage(player, "&c该页不存在！");
            return;
        }

        Inventory inventory = Bukkit.createInventory(new PaginatedInventoryHolder(page), 9 * 6, guiName);

        Map<String, ItemStack> itemStackMap = new HashMap<>();
        ItemStack defaultItem = itemStackMap.computeIfAbsent("D", id -> createGuiItem(config, "D", page));

        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char itemChar = line.charAt(col);
                String itemId = String.valueOf(itemChar);

                if (config.contains("items." + itemId)) {
                    ItemStack item = itemStackMap.computeIfAbsent(itemId, id -> createGuiItem(config, id, page));

                    if ("L".equals(itemId) && page == 1) {
                        inventory.setItem(row * 9 + col, defaultItem);
                    } else if ("N".equals(itemId) && page == totlePage) {
                        inventory.setItem(row * 9 + col, defaultItem);
                    } else {
                        inventory.setItem(row * 9 + col, item);
                    }
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
        int totlePage = config.getInt("pages");
        if (page > totlePage || page < 1) {
            PlayerMessage.sendMessage(player, "&c该页不存在！");
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();  // 获取当前打开的 GUI

        // 如果当前不是 PaginatedInventoryHolder 的界面，直接返回
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof PaginatedInventoryHolder)) {
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
        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char itemChar = line.charAt(col);
                String itemId = String.valueOf(itemChar);

                if (config.contains("items." + itemId)) {
                    ItemStack item = itemStackMap.computeIfAbsent(itemId, id -> createGuiItem(config, id, page));

                    if ("L".equals(itemId) && page == 1) {
                        inventory.setItem(row * 9 + col, defaultItem);
                    } else if ("N".equals(itemId) && page == totlePage) {
                        inventory.setItem(row * 9 + col, defaultItem);
                    } else {
                        inventory.setItem(row * 9 + col, item);
                    }
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

        // 检查是否点击了“上一页”或“下一页”
        if (clickedItemName.equals(ColorUtil.translateColorCode(guiConfigs.get(guiId).getString("items.L.Display")))) {
            // 处理“上一页”
            int currentPage = getCurrentPage(event);
            if (currentPage > 1) {
//                event.getWhoClicked().closeInventory();
                updateGui((Player) event.getWhoClicked(), guiId, currentPage - 1);
            }
        } else if (clickedItemName.equals(ColorUtil.translateColorCode(guiConfigs.get(guiId).getString("items.N.Display")))) {
            // 处理“下一页”
            int currentPage = getCurrentPage(event);
            int totalPages = guiConfigs.get(guiId).getInt("pages");

            if (currentPage < totalPages) {
//                event.getWhoClicked().closeInventory();
                updateGui((Player) event.getWhoClicked(), guiId, currentPage + 1);
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