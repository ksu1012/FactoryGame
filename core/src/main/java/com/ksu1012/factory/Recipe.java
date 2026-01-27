package com.ksu1012.factory;

import java.util.HashMap;

public class Recipe {
    // ItemType, Amount Required
    public HashMap<ItemType, Integer> inputs = new HashMap<>();

    // ItemType, Amount Produced (can be multiple)
    public HashMap<ItemType, Integer> outputs = new HashMap<>();

    public float craftTime;

    public Recipe(float craftTime) {
        this.craftTime = craftTime;
    }

    // Builder methods allow us to chain calls like: new Recipe(1f).addInput(...).addOutput(...)
    public Recipe addInput(ItemType type, int amount) {
        inputs.put(type, amount);
        return this;
    }

    public Recipe addOutput(ItemType type, int amount) {
        outputs.put(type, amount);
        return this;
    }
}
