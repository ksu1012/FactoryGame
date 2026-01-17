package com.ksu1012.factory;

public class Drill extends Building {

    private float timer = 0f;
    private float miningSpeed = 1.0f;

    public Drill(int x, int y) {
        super(x, y);
        setGlobalCap(10); // 10 item cap
    }

    @Override
    public void update(float delta, Tile tile) {
        timer += delta;
        if (timer >= miningSpeed) {
            timer -= miningSpeed;
            mineResource(tile);
        }
    }

    private void mineResource(Tile tile) {
        if (tile.type.minedItem != null) {
            boolean success = addInternalItem(tile.type.minedItem, 1);

            if (success) {
                System.out.println("Mined " + tile.type.minedItem + " (" + getItemCount(tile.type.minedItem) + ")");
            }
        }
    }

    @Override
    public boolean acceptsItem(ItemType type) {
        return false;
    }
}
