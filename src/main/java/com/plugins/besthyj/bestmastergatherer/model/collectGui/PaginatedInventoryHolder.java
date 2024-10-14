package com.plugins.besthyj.bestmastergatherer.model.collectGui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PaginatedInventoryHolder implements InventoryHolder {
    private int currentPage;

    public PaginatedInventoryHolder(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * 实现 InventoryHolder 必须有这个方法
     *
     * @return
     */
    @Override
    public Inventory getInventory() {
        return null;
    }
}
