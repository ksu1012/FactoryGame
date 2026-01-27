package com.ksu1012.factory;

public class Drill extends Building {

    private float timer = 0f;
    private float miningSpeed;
    private int quantity;

    public Drill(int x, int y, DrillDef def) {
        super(x, y);
        this.width = def.width;
        this.height = def.height;
        this.miningSpeed = def.miningSpeed;
        this.quantity = def.quantity;

        setGlobalCap(10); // Stores up to 10 items
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        timer += delta;
        if (timer >= miningSpeed) {
            timer -= miningSpeed;

            // In the future, scale mining speed with how many ore tiles the drill is over
            Tile myTile = grid[x][y];
            mineResource(myTile);
        }
        tryPushItem(grid);
    }

    private void mineResource(Tile tile) {
        if (tile.resource != null) {
            addInternalItem(tile.resource.minedItem, quantity);
        }
    }
}
