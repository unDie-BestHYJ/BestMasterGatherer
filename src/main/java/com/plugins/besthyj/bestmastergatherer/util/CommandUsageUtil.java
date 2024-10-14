package com.plugins.besthyj.bestmastergatherer.util;

import org.bukkit.command.CommandSender;

public class CommandUsageUtil {
    /**
     * 封装的帮助信息显示函数
     *
     * @param sender
     */
    public static void showHelpMessage(CommandSender sender) {
        String[] helpMessages = {
                "&a----- BestMasterGatherer 帮助 -----",
                "&6/bestmastergatherer collect opengui <guiId> - 打开指定的收集界面",
                "&6/bestmastergatherer attribute opengui <guiId> - 打开指定的属性界面",
                "&6/bestmastergatherer reload - 重新加载插件配置",
                "&6/bestmastergatherer help - 显示帮助信息",
                "&a------------------------------"
        };

        for (String message : helpMessages) {
            PlayerMessage.sendMessage(sender, message);
        }
    }
}
