package com.ksu1012.factory;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Abstract base class representing any placeable entity in the game world.
 * <p>
 * This class handles core building logic, including:
 * <ul>
 *   <li>Multi-tile footprints (Width/Height)</li>
 *   <li>Inventory management with slot-specific capacity limits</li>
 *   <li>Directional item transport logic ({@code tryPushItem})</li>
 * </ul>
 */

public abstract class Building {
    public int x, y;
    public int width = 1;
    public int height = 1;
    public Direction facing = Direction.NORTH;

    protected BuildingDef definition;

    protected HashMap<ItemType, Integer> inventory = new HashMap<>();

    // Collective item capacity (primarily for conveyors). -1 if no limit
    protected int globalMax = -1;

    // Item-specific limits
    protected HashMap<ItemType, Integer> itemMaxes = new HashMap<>();

    protected int currentTotalItemCount = 0;

    protected HashSet<ItemType> acceptedItems = new HashSet<>();
    protected boolean acceptsAll = false;

    // Types of terrain the Building can be placed on
    protected HashSet<TerrainType> validTerrain = new HashSet<>();

    public float energyCapacity = 0f;
    public float currentEnergy = 0f;
    public float powerProduction = 0f;   // Current output
    public float powerConsumption = 0f;  // Current demand

    public PowerNetwork network = null;
    public boolean hasPower = false;

    public Building(int x, int y, BuildingDef def) {
        this.x = x;
        this.y = y;
        this.definition = def;

        this.width = def.width;
        this.height = def.height;

        this.energyCapacity = def.energyCapacity;
        this.powerConsumption = def.powerConsumption;

        // Allow building on dirt by default
        validTerrain.add(TerrainType.DIRT);
    }

    public abstract void update(float delta, Tile[][] grid);

    // Check if Building can be placed on the terrain below
    public boolean canBuildOn(TerrainType terrain) {
        return validTerrain.contains(terrain);
    }

    // Pushes the first item in line
    protected boolean tryPushItem(Tile[][] grid) {
        return tryPushItem(grid, getFirstItem());
    }

    /**
     * Iterates through the output edge of the building based on its dimensions and orientation.
     * Attempts to push items into valid neighboring inventories.
     *
     * @param grid The game map for neighbor lookups.
     * @return true if an item was successfully transferred.
     */
    protected boolean tryPushItem(Tile[][] grid, ItemType itemToMove) {
        if (itemToMove == null || inventory.getOrDefault(itemToMove, 0) <= 0) return false;

        // Determine loop limits based on orientation
        boolean isVertical = (facing == Direction.NORTH || facing == Direction.SOUTH);
        int limit = isVertical ? width : height;

        // Loop through output edge
        for (int i = 0; i < limit; i++) {
            int targetX = x;
            int targetY = y;

            switch (facing) {
                case NORTH: targetX = x + i; targetY = y + height; break;
                case SOUTH: targetX = x + i; targetY = y - 1; break;
                case EAST:  targetX = x + width; targetY = y + i; break;
                case WEST:  targetX = x - 1; targetY = y + i; break;
            }

            if (targetX < 0 || targetX >= grid.length || targetY < 0 || targetY >= grid[0].length) {
                continue;
            }

            Building neighbor = grid[targetX][targetY].building;
            if (neighbor != null) {
                // Try to insert the SPECIFIC item we asked for
                if (neighbor.addItem(itemToMove, 1, this.facing)) {
                    inventory.put(itemToMove, inventory.get(itemToMove) - 1);
                    if (inventory.get(itemToMove) <= 0) inventory.remove(itemToMove);
                    currentTotalItemCount--;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addItem(ItemType type, int amount, Direction incomingDirection) {

        // Block items coming from the direction this Building is facing
        if (incomingDirection == this.facing.opposite()) {
            return false;
        }

        // Filter Check
        if (!acceptsItem(type)) {
            System.out.println("Rejected " + type + ". Allowed: " + acceptedItems);
            return false;
        }

        return addInternalItem(type, amount);
    }

    // Override to ignore direction check
    public boolean addItem(ItemType type, int amount) {
        return addItem(type, amount, this.facing);
    }

    // Internal logic that checks both the Global Max and the Individual Max.
    protected boolean addInternalItem(ItemType type, int amount) {
        // Check Item-Specific limit
        if (itemMaxes.containsKey(type)) {
            int currentSpecific = inventory.getOrDefault(type, 0);
            int maxSpecific = itemMaxes.get(type);

            if (currentSpecific + amount > maxSpecific) {
                return false;
            }
        }

        // Check global limit
        else if (globalMax != -1) {
            if (currentTotalItemCount + amount > globalMax) {
                return false;
            }
        }

        // Add the item
        inventory.put(type, inventory.getOrDefault(type, 0) + amount);
        currentTotalItemCount += amount;
        return true;
    }

    public void setFacing(Direction newFacing) {
        // Check if we are changing orientation (Vertical <-> Horizontal)
        boolean isCurrentlyVertical = (this.facing == Direction.NORTH || this.facing == Direction.SOUTH);
        boolean isToBeVertical = (newFacing == Direction.NORTH || newFacing == Direction.SOUTH);

        if (isCurrentlyVertical != isToBeVertical) {
            // Swap dimensions
            int temp = this.width;
            this.width = this.height;
            this.height = temp;
        }

        this.facing = newFacing;
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

    public void onPlaced(Tile[][] grid) {
        // Override in subclasses
    }

    public float getConnectionRadius() {
        return 1.5f; // Default to requiring contact
    }

    public BuildingDef getDefinition() {
        return definition;
    }

    public boolean isBattery() {
        return definition.energyCapacity > 0;
    }

    public boolean connectsToPower() {
        return energyCapacity > 0 || definition.powerGeneration > 0 || powerConsumption > 0 || this instanceof PowerPole;
    }
}
