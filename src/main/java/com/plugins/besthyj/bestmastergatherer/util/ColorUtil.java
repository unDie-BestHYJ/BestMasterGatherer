package com.plugins.besthyj.bestmastergatherer.util;

import org.bukkit.ChatColor;

/**
 * 颜色代码转换工具
 */
public class ColorUtil {
    public static String translateColorCode(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
