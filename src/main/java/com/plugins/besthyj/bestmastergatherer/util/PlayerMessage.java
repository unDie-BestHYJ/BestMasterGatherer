package com.plugins.besthyj.bestmastergatherer.util;

import org.bukkit.command.CommandSender;

public class PlayerMessage {
    /**
     * 封装的发送消息函数
     *
     * @param sender
     * @param message
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.translateColorCode(message));
    }
}
