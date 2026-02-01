package com.ksu1012.factory;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    public static GameState instance = new GameState();

    // Inventory
    public HashMap<ItemType, Integer> resources = new HashMap<>();

    public GameState() {
        for (ItemType type : ItemType.values()) {
            resources.put(type, 0);
        }
    }

    public void addResource(ItemType type, int amount) {
        resources.put(type, resources.getOrDefault(type, 0) + amount);
        System.out.println("Inventory: " + type + " = " + resources.get(type)); // Temporary print statement
    }

    public boolean canAfford(HashMap<ItemType, Integer> cost) {
        for (Map.Entry<ItemType, Integer> entry : cost.entrySet()) {
            int owned = resources.getOrDefault(entry.getKey(), 0);
            if (owned < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    // Deduct resources
    public void payCost(HashMap<ItemType, Integer> cost) {
        for (Map.Entry<ItemType, Integer> entry : cost.entrySet()) {
            int current = resources.getOrDefault(entry.getKey(), 0);
            resources.put(entry.getKey(), current - entry.getValue());
        }
    }
}
