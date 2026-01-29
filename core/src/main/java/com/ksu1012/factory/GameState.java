package com.ksu1012.factory;

import java.util.HashMap;

public class GameState {
    public static GameState instance = new GameState();

    // Inventory
    public HashMap<ItemType, Integer> resources = new HashMap<>();

    public GameState() {
    }

    public void addResource(ItemType type, int amount) {
        resources.put(type, resources.getOrDefault(type, 0) + amount);
        System.out.println("Inventory: " + type + " = " + resources.get(type));
    }
}
