package com.ksu1012.factory;

public class Drill extends Building {

    private float timer = 0f;
    private float miningSpeed; // At first, set to definition base speed. Then calculate scaled speed and remain unchanged
    private int quantity;
    private ItemType resource;

    public Drill(int x, int y, DrillDef def) {
        super(x, y);
        this.width = def.width;
        this.height = def.height;
        this.miningSpeed = def.miningSpeed;
        this.quantity = def.quantity;

        setGlobalCap(10); // Stores up to 10 items
    }

    public void onPlaced(Tile[][] grid) {
        int resourceCount = 0;
        ItemType foundType = null;

        // Loop through area below Drill
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Check bounds
                if (x + i < grid.length && y + j < grid[0].length) {
                    Tile t = grid[x + i][y + j];

                    if (t.resource != null) {
                        resourceCount++;
                        // For now, just use the first ItemType found. May implement some form of smart selection in the future
                        if (foundType == null) foundType = t.resource.minedItem;
                    }
                }
            }
        }

        this.resource = foundType;

        // Divide mining time by the number of resource tiles beneath
        if (resourceCount > 0) {
            this.miningSpeed /= (float) resourceCount;
            System.out.println("Drill configured: Found " + resourceCount + " tiles. Speed: " + miningSpeed);
        } else {
            this.resource = null; // Turns the drill off
        }
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        timer += delta;
        if (timer >= miningSpeed) {
            timer -= miningSpeed;

            mineResource();
        }
        tryPushItem(grid);
    }

    private void mineResource() {
        addInternalItem(this.resource, quantity);
    }
}
