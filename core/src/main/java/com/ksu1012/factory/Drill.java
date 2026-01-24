package com.ksu1012.factory;

public class Drill extends Building {

    private float timer = 0f;
    private float miningSpeed = 1.0f; // Seconds per item

    public Drill(int x, int y) {
        super(x, y);
        setGlobalCap(10); // Stores 10 items max before stopping
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        // Mining
        timer += delta;
        if (timer >= miningSpeed) {
            timer -= miningSpeed;
            Tile tile = grid[x][y];
            mineResource(tile);
        }

        // Attempt to output the inventory
        tryPushItem(grid);
    }

    private void mineResource(Tile tile) {
        // Check resource layer of the Tile
        if (tile.resource != null) {
            addInternalItem(tile.resource.minedItem, 1);
        }
    }
}
