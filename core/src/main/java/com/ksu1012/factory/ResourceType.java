package com.ksu1012.factory;

import com.badlogic.gdx.graphics.Color;

public enum ResourceType {
    COPPER_ORE(ItemType.COPPER_ORE, new Color(0.8f, 0.5f, 0.2f, 1f), 0.0006f, 8),
    IRON_ORE(ItemType.IRON_ORE, new Color(0.7f, 0.7f, 0.7f, 1f), 0.00045f, 8),
    COAL_ORE(ItemType.COAL, new Color(0.1f, 0.1f, 0.1f, 1f), 0.0008f, 8);

    public final ItemType minedItem;
    public final Color color;
    public final float density;
    public final int averageSize;

    ResourceType(ItemType minedItem, Color color, float density, int averageSize) {
        this.minedItem = minedItem;
        this.color = color;
        this.density = density;
        this.averageSize = averageSize;
    }
}
