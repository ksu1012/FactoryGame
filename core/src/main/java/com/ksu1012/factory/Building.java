package com.ksu1012.factory;

import java.util.HashMap;
import java.util.HashSet;

public abstract class Building {
    public int x, y;

    protected HashMap<ItemType, Integer> inventory = new HashMap<>();

    // Collective item capacity (primarily for conveyors). -1 if no limit
    protected int globalMax = -1;

    // Item-specific limits
    protected HashMap<ItemType, Integer> itemMaxes = new HashMap<>();

    protected int currentTotalItemCount = 0;

    protected HashSet<ItemType> acceptedItems = new HashSet<>();
    protected boolean acceptsAll = false;

    public Building(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(float delta, Tile tile);

    public boolean acceptsItem(ItemType type) {
        return acceptedItems.contains(type) || acceptsAll;
    }

    protected void setGlobalCap(int max) {
        this.globalMax = max;
    }

    protected void setItemCap(ItemType type, int max) {
        itemMaxes.put(type, max);
    }

     // Tries to add an item from an EXTERNAL source.
    public boolean addItem(ItemType type, int amount) {
        if (!acceptsItem(type)) return false;
        return addInternalItem(type, amount);
    }

     // Internal logic that checks both the Global Max and the Individual Max.
    protected boolean addInternalItem(ItemType type, int amount) {
        // Check Global Limit
        if (globalMax != -1) {
            if (currentTotalItemCount + amount > globalMax) {
                return false; // Collective limit reached
            }
        }

        // Check Individual Limit (if it exists for this specific item)
        if (itemMaxes.containsKey(type)) {
            int currentSpecific = inventory.getOrDefault(type, 0);
            int maxSpecific = itemMaxes.get(type);

            if (currentSpecific + amount > maxSpecific) {
                return false; // Specific slot limit reached
            }
        }

        // Add the item if checks passed
        inventory.put(type, inventory.getOrDefault(type, 0) + amount);
        currentTotalItemCount += amount;
        return true;
    }

    // --- HELPERS ---

    public int getItemCount(ItemType type) {
        return inventory.getOrDefault(type, 0);
    }

    public ItemType getFirstItem() {
        if (inventory.isEmpty()) return null;
        return inventory.keySet().iterator().next();
    }
}
