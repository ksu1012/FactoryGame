package com.ksu1012.factory;

import com.badlogic.gdx.graphics.Color;

public enum ResourceType {
    COPPER_ORE(ItemType.COPPER_ORE, new Color(0.8f, 0.5f, 0.2f, 1f)),
    IRON_ORE(ItemType.IRON_ORE, new Color(0.7f, 0.7f, 0.7f, 1f)),
    COAL(ItemType.COAL, new Color(0.05f, 0.05f, 0.05f, 1f));

    public final ItemType minedItem;
    public final Color color;

    // Assign color to resource type (temporary)
    ResourceType(ItemType minedItem, Color color) {
        this.minedItem = minedItem;
        this.color = color;
    }
}
