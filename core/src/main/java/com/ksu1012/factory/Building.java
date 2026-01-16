package com.ksu1012.factory;

import java.util.HashMap;

public abstract class Building {
    public int x, y;

    protected HashMap<ItemType, Integer> inventory = new HashMap<>();

    public Building(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(float delta);

    public void addItem(ItemType type, int amount) {
        inventory.put(type, inventory.getOrDefault(type, 0) + amount);
    }

    // Helper to check item count (for debugging)
    public int getItemCount(ItemType type) {
        return inventory.getOrDefault(type, 0);
    }}
