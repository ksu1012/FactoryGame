package com.ksu1012.factory;

public class Battery extends Building {
    public Battery(int x, int y, BuildingDef def) {
        super(x, y, def);
        setAcceptsAnyItem(false);
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        // Do nothing. Logic handled by PowerNetwork
    }
}
