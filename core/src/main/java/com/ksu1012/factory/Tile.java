package com.ksu1012.factory;

/**
 * Represents a single position in the spatial grid.
 * <p>
 * Composed of three distinct layers:
 * 1. <b>Terrain:</b> Base layer of dirt, water, etc.
 * 2. <b>Resource:</b> Ores laid on top of the Terrain.
 * 3. <b>Building:</b> Buildings placed above both.
 */

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
