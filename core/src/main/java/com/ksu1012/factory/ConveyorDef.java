package com.ksu1012.factory;

public class ConveyorDef extends BuildingDef {
    public float speed; // Items moved per second

    public ConveyorDef(float speed) {
        super(1, 1); // Conveyors are usually 1x1
        this.speed = speed;
    }
}
