package com.ksu1012.factory;

import java.util.HashMap;

public abstract class BuildingDef {
    public int width;
    public int height;

    public HashMap<ItemType, Integer> cost = new HashMap<>();

    public BuildingDef(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public BuildingDef addCost(ItemType item, int amount) {
        cost.put(item, amount);
        return this;
    }
}
