package com.ksu1012.factory;

public enum ResourceType {
    COPPER_ORE(ItemType.COPPER_ORE);

    public final ItemType minedItem;

    ResourceType(ItemType minedItem) {
        this.minedItem = minedItem;
    }
}
