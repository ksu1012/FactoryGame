package com.ksu1012.factory;

public class DrillDef extends BuildingDef {
    public float miningSpeed; // Time to mine
    public int quantity;

    public DrillDef(int width, int height, float miningSpeed, int quantity) {
        super(width, height);
        this.miningSpeed = miningSpeed;
        this.quantity = quantity;
    }
}
