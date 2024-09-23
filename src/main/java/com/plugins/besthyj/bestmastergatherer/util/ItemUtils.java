//package com.plugins.besthyj.bestmastergatherer.util;
//
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Material;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ItemUtils {
//
//    private static final Map<String, List<String>> itemMatchLores = new HashMap<>();
//
//    public static ItemStack getItemFromConfig(Map<String, Object> items, String itemId) {
//        ConfigurationSection itemConfig = (ConfigurationSection) items.get(itemId);
//        int id = itemConfig.getInt("Id");
//        short data = (short) itemConfig.getInt("Data", 0);
//        String displayName = ChatColor.translateAlternateColorCodes('&', itemConfig.getString("Display"));
//        List<String> lores = itemConfig.getStringList("Lores");
//
//        List<String> matchLores = itemConfig.getStringList("MatchLores");
//        if (matchLores != null && !matchLores.isEmpty()) {
//            itemMatchLores.put(itemId, matchLores);
//        }
//
//        ItemStack item = new ItemStack(Material.getMaterial(id), 1, data);
//        ItemMeta meta = item.getItemMeta();
//        if (meta != null) {
//            meta.setDisplayName(displayName);
//
//            List<String> coloredLores = new ArrayList<>();
//            for (String lore : lores) {
//                coloredLores.add(ChatColor.translateAlternateColorCodes('&', lore));
//            }
//            meta.setLore(coloredLores);
//            item.setItemMeta(meta);
//        }
//
//        return item;
//    }
//
//    public static void setItemLores(ItemStack item, List<String> lores) {
//        ItemMeta meta = item.getItemMeta();
//        if (meta != null) {
//            List<String> coloredLores = new ArrayList<>();
//            for (String lore : lores) {
//                coloredLores.add(ChatColor.translateAlternateColorCodes('&', lore));
//            }
//            meta.setLore(coloredLores);
//            item.setItemMeta(meta);
//        }
//    }
//
//    public static List<String> getMatchLoresByItemId(String itemId) {
//        return itemMatchLores.get(itemId);
//    }
//}
