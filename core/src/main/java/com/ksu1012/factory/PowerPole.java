package com.ksu1012.factory;

public class PowerPole extends Building {
    public PowerPole(int x, int y, BuildingDef def) {
        super(x, y, def);
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        // Do nothing
    }

    @Override
    public float getConnectionRadius() {
        return 5.5f;
    }
}
