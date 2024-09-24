package com.plugins.besthyj.bestmastergatherer.listener;

import com.plugins.besthyj.bestmastergatherer.integration.attribute.attributeplus.AttributePlusHandler;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.PlayerAttribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 获取上线的玩家
        Player player = event.getPlayer();

        // 调用 AttributePlusHandler 中的 updateAttributeByPlayer 方法
        PlayerAttribute.addAttributeToPlayer(player);

        // 可选：输出日志或提示信息
        Bukkit.getLogger().info("已为玩家 " + player.getName() + " 更新属性。");
    }
}
