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

            Tile myTile = grid[x][y];
            mineResource(myTile);
        }

        // Output
        tryPushItem(grid);
    }

    private void mineResource(Tile tile) {
        if (tile.type.minedItem != null) {
            boolean success = addInternalItem(tile.type.minedItem, 1);

            if (success) {
                System.out.println("Mined " + tile.type.minedItem);
            }
        }
    }
}
