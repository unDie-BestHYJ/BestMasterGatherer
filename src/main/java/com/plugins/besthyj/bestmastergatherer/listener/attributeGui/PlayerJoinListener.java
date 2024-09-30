package com.plugins.besthyj.bestmastergatherer.listener.attributeGui;

import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;
import com.plugins.besthyj.bestmastergatherer.integration.attribute.attributeplus.AttributePlusHandler;
import com.plugins.besthyj.bestmastergatherer.manager.attributeGui.PlayerAttribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class PlayerJoinListener implements Listener {
    private BestMasterGatherer plugin;

    public PlayerJoinListener(BestMasterGatherer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerAttribute playerAttribute = plugin.getPlayerAttribute();
        playerAttribute.addAttributeToPlayer(player);

        Bukkit.getLogger().info("已为玩家 " + player.getName() + " 更新属性。");
    }
}
