package com.plugins.besthyj.bestmastergatherer.commands;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.AttributeGuiManager;
import com.plugins.besthyj.bestmastergatherer.manager.collectGui.CollectGuiManager;
import com.plugins.besthyj.bestmastergatherer.util.CommandUsageUtil;
import com.plugins.besthyj.bestmastergatherer.util.PlayerMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class BestMasterGathererCommand implements CommandExecutor {

    private final BestMasterGatherer plugin;

    public BestMasterGathererCommand(BestMasterGatherer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            PlayerMessage.sendMessage(sender, "&c请输入子命令！");
            CommandUsageUtil.showHelpMessage(sender);
            return false;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        if (subCommand.equalsIgnoreCase("collect")) {
            return handleCollectCommand(sender, subArgs);
        } else if (subCommand.equalsIgnoreCase("attribute")) {
            return handleAttributeCommand(sender, subArgs);
        } else if (subCommand.equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            PlayerMessage.sendMessage(sender, "&a配置文件已重新加载！");
            return true;
        }

        CommandUsageUtil.showHelpMessage(sender);
        return false;
    }

    private boolean handleCollectCommand(CommandSender sender, String[] subArgs) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            CollectGuiManager collectGuiManager = plugin.getCollectGuiManager();

            if (subArgs.length == 2 && subArgs[0].equalsIgnoreCase("opengui")) {
                String guiId = subArgs[1];
                collectGuiManager.openGui(player, guiId, 1);
                return true;
            }
        }
        return false;
    }

    private boolean handleAttributeCommand(CommandSender sender, String[] subArgs) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (subArgs.length == 2 && subArgs[0].equalsIgnoreCase("opengui")) {
                String guiId = subArgs[1];
                AttributeGuiManager attributeGuiManager = plugin.getAttributeGuiManager();
                attributeGuiManager.openGui(player, guiId);
                return true;
            }
        }
        return false;
    }
}
