package com.ksu1012.factory;

public class Tile {
    public int x, y;

    // --- LAYER 1: BASE TERRAIN ---
    // Defaults to DIRT, non-null
    public TerrainType terrain = TerrainType.DIRT;

    // --- LAYER 2: RESOURCE OVERLAY ---
    // Resource that exists in the tile (if any, can be null)
    public ResourceType resource = null;

    // --- LAYER 3: BUILDING ---
    // Building occupying the Tile
    public Building building = null;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
