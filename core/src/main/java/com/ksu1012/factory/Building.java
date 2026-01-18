package com.ksu1012.factory;

import java.util.HashMap;
import java.util.HashSet;

public abstract class Building {
    public int x, y;
    public Direction facing = Direction.NORTH;

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

    public abstract void update(float delta, Tile[][] grid);

    // Move an item from this Building to the one in front. Returns true if successful
    protected boolean tryPushItem(Tile[][] grid) {
        if (inventory.isEmpty()) return false;

        // Coordinates of the tile in front
        int targetX = x + facing.dx;
        int targetY = y + facing.dy;

        // Check Bounds
        if (targetX < 0 || targetX >= grid.length || targetY < 0 || targetY >= grid[0].length) {
            return false;
        }

        // Check neighboring Building
        Building neighbor = grid[targetX][targetY].building;
        if (neighbor != null) {
            ItemType itemToMove = getFirstItem();

            // Try to insert
            if (neighbor.addItem(itemToMove, 1)) {
                // Remove from current building if successful
                inventory.put(itemToMove, inventory.get(itemToMove) - 1);
                if (inventory.get(itemToMove) <= 0) inventory.remove(itemToMove);
                currentTotalItemCount--;
                return true;
            }
        }
        return false;
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

    public boolean acceptsItem(ItemType type) {
        return acceptedItems.contains(type) || acceptsAll;
    }

    protected void setAcceptsAnyItem(boolean value) { this.acceptsAll = value; }

    protected void setGlobalCap(int max) {
        this.globalMax = max;
    }

    protected void setItemCap(ItemType type, int max) {
        itemMaxes.put(type, max);
    }

}
