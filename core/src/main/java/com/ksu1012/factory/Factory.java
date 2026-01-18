package com.ksu1012.factory;

public class Factory extends Building {

    public Factory(int x, int y) {
        super(x, y);
        this.width = 2;   // It takes up a 2x2 space
        this.height = 2;

        setAcceptsAnyItem(true);
        setGlobalCap(20); // Can hold more items
    }

    @Override
    public void update(float delta, Tile[][] grid) {

    }
}
