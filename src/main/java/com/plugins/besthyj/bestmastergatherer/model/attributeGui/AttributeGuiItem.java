package com.plugins.besthyj.bestmastergatherer.model.attributeGui;

import com.plugins.besthyj.bestmastergatherer.util.ColorUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttributeGuiItem {

    private String itemId;

    private Integer itemTypeId;
    private Integer itemTypeData;

    private String itemName;

    private List<String> loresList;

    private List<String> mmItemsList;

    private Map<Integer, List<String>> attributesMap;

    // getter and setter
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public Integer getItemTypeData() {
        return itemTypeData;
    }

    public void setItemTypeData(Integer itemTypeData) {
        this.itemTypeData = itemTypeData;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public List<String> getLoresList() {
        return loresList.stream()
                .map(ColorUtil::translateColorCode) // 处理颜色代码
                .collect(Collectors.toList());
    }

    public void setLoresList(List<String> loresList) {
        this.loresList = loresList;
    }

    public List<String> getMMItemsList() {
        return mmItemsList.stream()
                .map(ColorUtil::translateColorCode)
                .collect(Collectors.toList());
    }

    public void setMmItemsList(List<String> mmItemsList) {
        this.mmItemsList = mmItemsList;
    }

    public Map<Integer, List<String>> getAttributesMap() {
        return attributesMap;
    }

    public void setAttributesMap(Map<Integer, List<String>> attributesMap) {
        this.attributesMap = attributesMap;
    }
}
