package com.plugins.besthyj.bestmastergatherer;

import com.plugins.besthyj.bestmastergatherer.commands.BestMasterGathererCommand;
import com.plugins.besthyj.bestmastergatherer.listener.attributeGui.AttributeGuiListener;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.AttributeGuiManager;
import com.plugins.besthyj.bestmastergatherer.manager.collectGui.CollectGuiManager;
import com.plugins.besthyj.bestmastergatherer.listener.collectGui.CollectGuiListener;
import com.plugins.besthyj.bestmastergatherer.util.GUIFileUtil;
import com.plugins.besthyj.bestmastergatherer.util.attributeGui.AttributeGuiItemUtil;
import com.plugins.besthyj.bestmastergatherer.util.collectGui.PlayerDataStorageUtil;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BestMasterGatherer extends JavaPlugin {

    private File dataFolder;
    private CollectGuiListener collectGuiListener;
    private AttributeGuiListener attributeGuiListener;

    private GUIFileUtil guiFileUtil;

    public GUIFileUtil getGuiFileUtil() {
        return guiFileUtil;
    }

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

        // manager
        CollectGuiManager.init(this);
        AttributeGuiManager.init(this);

        // util
        AttributeGuiItemUtil.init(this);
        PlayerDataStorageUtil.init(this);
        guiFileUtil = new GUIFileUtil(this);

        // command
        this.getCommand("BestMasterGatherer").setExecutor(new BestMasterGathererCommand(this));

        // listener
        collectGuiListener = new CollectGuiListener(this);
        getServer().getPluginManager().registerEvents(collectGuiListener, this);
        attributeGuiListener = new AttributeGuiListener(this);
        getServer().getPluginManager().registerEvents(attributeGuiListener, this);

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

    /**
     * 初始化文件
     *
     * @param dataFolder
     */
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
