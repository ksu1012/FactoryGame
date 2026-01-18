package com.ksu1012.factory;

public enum TerrainType {
    // Ground
    DIRT(null, true),
    COPPER_ORE(ItemType.COPPER_ORE, true),
    IRON_ORE(ItemType.IRON_ORE, true),

    // Liquids/Obstacles (Not Buildable for normal machines)
    WATER(null, false),
    LAVA(null, false);

    public final ItemType minedItem;
    public final boolean isBuildable; // true = solid ground, false = liquid/wall

    TerrainType(ItemType minedItem, boolean isBuildable) {
        this.minedItem = minedItem;
        this.isBuildable = isBuildable;
    }
}
