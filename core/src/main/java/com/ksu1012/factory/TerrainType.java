package com.ksu1012.factory;

import com.badlogic.gdx.graphics.Color;

public enum TerrainType {
    // Dirt: Uses two slightly different shades for the checkerboard effect. The rest are currently the same for both
    DIRT(true, new Color(0.15f, 0.15f, 0.15f, 1f), new Color(0.18f, 0.18f, 0.18f, 1f)),

    // Water
    WATER(false, new Color(0.2f, 0.4f, 0.8f, 1f), new Color(0.2f, 0.4f, 0.8f, 1f)),

    // Lava
    LAVA(false, new Color(0.8f, 0.2f, 0.1f, 1f), new Color(0.8f, 0.2f, 0.1f, 1f)),

    // Wall
    WALL(false, new Color(0.1f, 0.1f, 0.1f, 1f), new Color(0.05f, 0.05f, 0.05f, 1f));

    public final boolean isBuildable; // REMOVE IN FUTURE WHEN WORLD GENERATION IMPLEMENTED PROPERLY
    public final Color color1; // Even tiles
    public final Color color2; // Odd tiles

    TerrainType(boolean isBuildable, Color c1, Color c2) {
        this.isBuildable = isBuildable;
        this.color1 = c1;
        this.color2 = c2;
    }
}
