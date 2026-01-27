package com.ksu1012.factory;

import com.badlogic.gdx.graphics.Color;

public enum ItemType {
    // We define the items and their colors here
    COPPER_ORE(new Color(0.72f, 0.45f, 0.18f, 1f)),
    COPPER(new Color(0.88f, 0.55f, 0.22f, 1f)),
    IRON_ORE(new Color(0.63f, 0.63f, 0.63f, 1f)),
    IRON(new Color(0.77f, 0.77f, 0.77f, 1f)),
    COAL(new Color(0.05f, 0.05f, 0.05f, 1f));

    public final Color color;

    ItemType(Color color) {
        this.color = color;
    }
}
