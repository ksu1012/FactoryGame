package com.ksu1012.factory;

public enum TerrainType {
    DIRT(null),

    COPPER_ORE(ItemType.COPPER_ORE),
    IRON_ORE(ItemType.IRON_ORE);

    public final ItemType minedItem;

    TerrainType(ItemType minedItem) {
        this.minedItem = minedItem;
    }
}
