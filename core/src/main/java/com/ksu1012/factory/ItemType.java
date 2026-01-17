package com.ksu1012.factory;

import com.badlogic.gdx.graphics.Color;

public enum ItemType {
    // We define the items and their colors here
    COPPER_ORE(new Color(0.8f, 0.5f, 0.2f, 1f)),
    IRON_ORE(new Color(0.7f, 0.7f, 0.7f, 1f));

    public final Color color;

    ItemType(Color color) {
        this.color = color;
    }
}
