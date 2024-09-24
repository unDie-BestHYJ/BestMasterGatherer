package com.plugins.besthyj.bestmastergatherer;

import com.plugins.besthyj.bestmastergatherer.commands.InventoryCommand;
import com.plugins.besthyj.bestmastergatherer.manager.CollectGuiManager;
import com.plugins.besthyj.bestmastergatherer.listener.CollectGuiListener;
import com.plugins.besthyj.bestmastergatherer.util.FileStorageUtil;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BestMasterGatherer extends JavaPlugin {

    private File dataFolder;
    private CollectGuiListener collectGuiListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataFolder = getDataFolderPath();

        // 文件初始化
        ensureGuiFilesExist(dataFolder);

        File storageFolder = new File(dataFolder, "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdir();
        }

        // 方法、事件初始化
        CollectGuiManager.init(this);
        FileStorageUtil.init(this);

        this.getCommand("BestMasterGatherer").setExecutor(new InventoryCommand(this));

        collectGuiListener = new CollectGuiListener(this);
        getServer().getPluginManager().registerEvents(collectGuiListener, this);

        getLogger().info("BestMasterGatherer 插件已启用！");
    }

    @Override
    public void onDisable() {
        clearResources();

        getLogger().info("BestMasterGatherer 插件已禁用！");
    }

    /**
     * 获取数据存储路径的函数
     *
     * @return
     */
    public File getDataFolderPath() {
        String softSyncPath = getConfig().getString("soft-sync", "");

        if (!softSyncPath.isEmpty()) {
            File customPath = new File(softSyncPath);
            if (!customPath.isAbsolute()) {
                getLogger().severe("soft-sync 必须为绝对路径！插件将使用默认路径。");
                return getDataFolder();
            } else {
                return customPath;
            }
        } else {
            return getDataFolder();
        }
    }

    public void ensureGuiFilesExist(File dataFolder) {
        createFolderAndCopyDefault(dataFolder, "collectGUI", "示例gui.yml");
        createFolderAndCopyDefault(dataFolder, "attributeGUI", "示例gui.yml");
    }

    private void createFolderAndCopyDefault(File dataFolder, String folderName, String fileName) {
        File folder = new File(dataFolder, folderName);
        if (!folder.exists()) {
            folder.mkdirs(); // 使用 mkdirs 以确保创建所有父文件夹
        }

        File exampleFile = new File(folder, fileName);
        if (!exampleFile.exists()) {
            saveResource(folderName + "/" + fileName, false); // 保存嵌入资源
        }
    }

    /**
     * 清理所有资源
     */
    public void clearResources() {
        CollectGuiManager.clearResources();

        this.getCommand("BestMasterGatherer").setExecutor(null);

        HandlerList.unregisterAll(collectGuiListener);
        collectGuiListener.clearResources();
    }
}
