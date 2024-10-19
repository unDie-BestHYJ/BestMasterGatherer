package com.plugins.besthyj.bestmastergatherer;

import com.plugins.besthyj.bestmastergatherer.commands.BestMasterGathererCommand;
import com.plugins.besthyj.bestmastergatherer.listener.attributeGui.AttributeGuiListener;
import com.plugins.besthyj.bestmastergatherer.listener.attributeGui.PlayerJoinListener;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.AttributeGuiManager;
import com.plugins.besthyj.bestmastergatherer.integration.attribute.PlayerAttribute;
import com.plugins.besthyj.bestmastergatherer.manager.collectGui.CollectGuiManager;
import com.plugins.besthyj.bestmastergatherer.listener.collectGui.CollectGuiListener;
import com.plugins.besthyj.bestmastergatherer.util.attributeGui.AttributeGuiItemUtil;
import com.plugins.besthyj.bestmastergatherer.util.collectGui.PlayerDataStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class BestMasterGatherer extends JavaPlugin {

    private File dataFolder;
    private CollectGuiListener collectGuiListener;
    private AttributeGuiListener attributeGuiListener;
    private PlayerJoinListener playerJoinListener;

    private AttributeGuiItemUtil attributeGuiItemUtil;
    private PlayerDataStorageUtil playerDataStorageUtil;

    private CollectGuiManager collectGuiManager;
    private AttributeGuiManager attributeGuiManager;
    private PlayerAttribute playerAttribute;

    public AttributeGuiItemUtil getAttributeGuiItemUtil() {
        return attributeGuiItemUtil;
    }

    public PlayerDataStorageUtil getPlayerDataStorageUtil() {
        return playerDataStorageUtil;
    }

    public CollectGuiManager getCollectGuiManager() {
        return collectGuiManager;
    }

    public AttributeGuiManager getAttributeGuiManager() {
        return attributeGuiManager;
    }

    public PlayerAttribute getPlayerAttribute() {
        return playerAttribute;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataFolder = getDataFolderPath();

        // 文件初始化
        ensureGuiFilesExist(dataFolder);

        // util
        attributeGuiItemUtil = new AttributeGuiItemUtil(this);
        playerDataStorageUtil = new PlayerDataStorageUtil(this);

        // manager
        collectGuiManager = new CollectGuiManager(this);
        attributeGuiManager = new AttributeGuiManager(this);

        // attribute
        playerAttribute = new PlayerAttribute(this);

        // command
        this.getCommand("BestMasterGatherer").setExecutor(new BestMasterGathererCommand(this));

        // listener
        collectGuiListener = new CollectGuiListener(this);
        getServer().getPluginManager().registerEvents(collectGuiListener, this);
        attributeGuiListener = new AttributeGuiListener(this);
        getServer().getPluginManager().registerEvents(attributeGuiListener, this);
        playerJoinListener = new PlayerJoinListener(this);
        getServer().getPluginManager().registerEvents(playerJoinListener, this);

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
        File storageFolder = new File(dataFolder, "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdir();
        }
    }

    private void createFolderAndCopyDefault(File dataFolder, String folderName, String fileName) {
        File folder = new File(dataFolder, folderName);
        if (!folder.exists()) {
            boolean folderCreated = folder.mkdirs();
            if (!folderCreated) {
                Bukkit.getLogger().warning("创建文件夹失败: " + folder.getPath());
                return;
            }
        }

        File exampleFile = new File(folder, fileName);

        if (!exampleFile.exists()) {
            try (InputStream inputStream = this.getResource(folderName + "/" + fileName)) {
                if (inputStream != null) {
                    Files.copy(inputStream, exampleFile.toPath());
                    Bukkit.getLogger().info("默认资源已复制到: " + exampleFile.getPath());
                } else {
                    Bukkit.getLogger().warning("在 JAR 中未找到资源: " + folderName + "/" + fileName);
                }
            } catch (IOException e) {
                Bukkit.getLogger().severe("复制默认资源到 " + exampleFile.getPath() + " 时失败");
                e.printStackTrace();
            }
        }
    }

    /**
     * 清理所有资源
     */
    public void clearResources() {
        HandlerList.unregisterAll(this);
        this.getCommand("BestMasterGatherer").setExecutor(null);
        this.getServer().getScheduler().cancelTasks(this);

        attributeGuiItemUtil.clearResources();

        collectGuiManager.clearResources();
        attributeGuiManager.clearResources();

        collectGuiListener.clearResources();
        attributeGuiListener.clearResources();
    }
}
