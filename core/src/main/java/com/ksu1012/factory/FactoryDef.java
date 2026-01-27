package com.ksu1012.factory;

import java.util.Arrays;
import java.util.List;

public class FactoryDef extends BuildingDef {
    // Store a list of Recipes that the factory can use
    public List<Recipe> possibleRecipes;
    public float craftingSpeed;

    // Store how many of each item type the Factory can hold
    public int itemCapacity;

    public FactoryDef(int width, int height, float craftingSpeed, int itemCapacity, Recipe... recipes) {
        super(width, height);
        this.craftingSpeed = craftingSpeed;
        this.itemCapacity = itemCapacity; // Store the limit
        this.possibleRecipes = Arrays.asList(recipes);
    }
}
