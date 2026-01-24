package com.ksu1012.factory;

public enum TerrainType {
    // true if buildable by default, false for special cases
    DIRT(true),
    SAND(true),  // Example of a future addition
    GRASS(true), // Example of a future addition

    WATER(false),
    LAVA(false),
    WALL(false);

    public final boolean isBuildable;

    TerrainType(boolean isBuildable) {
        this.isBuildable = isBuildable;
    }
}
