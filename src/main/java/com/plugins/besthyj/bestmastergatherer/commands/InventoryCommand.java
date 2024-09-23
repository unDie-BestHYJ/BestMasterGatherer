package com.plugins.besthyj.bestmastergatherer.commands;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.manager.CollectGuiManager;
import com.plugins.besthyj.bestmastergatherer.util.PlayerMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryCommand implements CommandExecutor {

    private final BestMasterGatherer plugin;

    public InventoryCommand(BestMasterGatherer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            showHelpMessage(sender);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            PlayerMessage.sendMessage(sender, "&a配置文件已重新加载！");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length >= 2 && args[0].equalsIgnoreCase("collectgui")) {
                String guiId = args[1];
                CollectGuiManager.openGui(player, guiId, 1);
                return true;
            }
        }

        showHelpMessage(sender);

        return false;
    }

    /**
     * 封装的帮助信息显示函数
     *
     * @param sender
     */
    private void showHelpMessage(CommandSender sender) {
        String[] helpMessages = {
                "&a----- BestInventory 帮助 -----",
                "&6/bestmastergatherer collectgui <guiId> - 打开指定的GUI收集界面",
                "&6/bestmastergatherer reload - 重新加载插件配置",
                "&6/bestmastergatherer help - 显示帮助信息",
                "&a------------------------------"
        };

        for (String message : helpMessages) {
            PlayerMessage.sendMessage(sender, message);
        }
    }
}