package com.ksu1012.factory;

import java.util.HashMap;

public abstract class BuildingDef {
    public int width;
    public int height;

    public HashMap<ItemType, Integer> cost = new HashMap<>();

    public float powerGeneration = 0f;
    public float powerConsumption = 0f;
    public float energyCapacity = 0f;

    public BuildingDef(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public BuildingDef addCost(ItemType item, int amount) {
        cost.put(item, amount);
        return this;
    }

    public BuildingDef setPowerGeneration(float amount) {
        this.powerGeneration = amount;
        return this;
    }
    public BuildingDef setPowerConsumption(float amount) {
        this.powerConsumption = amount;
        return this;
    }

    public BuildingDef setEnergyCapacity(float amount) {
        this.energyCapacity = amount;
        return this;
    }
}
