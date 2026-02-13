package com.ksu1012.factory;

public class BatteryDef extends BuildingDef {
    public BatteryDef(int x, int y, float capacity) {
        super(x, y);
        this.energyCapacity = capacity;
    }
}
