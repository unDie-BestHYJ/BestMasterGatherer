package com.plugins.besthyj.bestmastergatherer;

import com.plugins.besthyj.bestmastergatherer.commands.InventoryCommand;
import com.plugins.besthyj.bestmastergatherer.manager.StorageGuiManager;
import com.plugins.besthyj.bestmastergatherer.listener.StorageGuiListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BestMasterGatherer extends JavaPlugin {

    private File dataFolder;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataFolder = getDataFolderPath();

//        getLogger().info("数据存储路径: " + dataFolder.getAbsolutePath());

        File collectGuiFolder = new File(dataFolder, "collectGUI");
        if (!collectGuiFolder.exists()) {
            collectGuiFolder.mkdir();
            File exampleGuiFile = new File(collectGuiFolder, "示例gui.yml");
            if (!exampleGuiFile.exists()) {
                saveResource("collectGUI/示例gui.yml", false);
            }
        }

        File attributeGUIFolder = new File(dataFolder, "attributeGUI");
        if (!attributeGUIFolder.exists()) {
            attributeGUIFolder.mkdir();
            File exampleGuiFile = new File(attributeGUIFolder, "示例gui.yml");
            if (!exampleGuiFile.exists()) {
                saveResource("attributeGUI/示例gui.yml", false);
            }
        }

        File storageFolder = new File(dataFolder, "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdir();
        }

        StorageGuiManager.init(this);

        this.getCommand("BestMasterGatherer").setExecutor(new InventoryCommand(this));

        getServer().getPluginManager().registerEvents(new StorageGuiListener(this), this);

        getLogger().info("BestMasterGatherer 插件已启用！");
    }

    @Override
    public void onDisable() {
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
}
