package com.ksu1012.factory;

public class GeneratorDef extends FactoryDef {
    public float powerOutput; // Energy generated per second

    public GeneratorDef(int width, int height, float speed, int itemCapacity, float powerOutput, float energyCapacity, Recipe... fuelRecipes) {
        super(width, height, speed, itemCapacity, fuelRecipes);
        this.powerOutput = powerOutput;
        this.energyCapacity = energyCapacity;
    }
}
