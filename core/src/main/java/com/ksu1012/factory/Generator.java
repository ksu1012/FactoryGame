package com.ksu1012.factory;

import java.util.Map;

public class Generator extends Factory {

    private float currentBurnTime = 0f;
    private float maxBurnTime = 0f; // Used for drawing the progress bar

    public Generator(int x, int y, GeneratorDef def) {
        super(x, y, def);
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        if (currentBurnTime <= 0) {
            // Not operating
            this.powerProduction = 0f;

            // Get the fuel recipe from the definition
            // (GeneratorDef passes it as the first recipe in the list)
            Recipe fuelRecipe = ((FactoryDef)getDefinition()).possibleRecipes.get(0);

            if (hasFuel(fuelRecipe)) {
                consumeFuel(fuelRecipe);

                // Add the time!
                this.currentBurnTime += fuelRecipe.craftTime;
                this.maxBurnTime = fuelRecipe.craftTime; // For visual bar
            }
        }

        // Burn Logic
        if (currentBurnTime > 0) {
            currentBurnTime -= delta;

            this.powerProduction = ((GeneratorDef) definition).powerOutput;
        }
    }

    private boolean hasFuel(Recipe r) {
        for (Map.Entry<ItemType, Integer> entry : r.inputs.entrySet()) {
            if (getItemCount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void consumeFuel(Recipe r) {
        for (Map.Entry<ItemType, Integer> entry : r.inputs.entrySet()) {
            ItemType type = entry.getKey();
            int amount = entry.getValue();

            inventory.put(type, inventory.get(type) - amount);
            if (inventory.get(type) <= 0) inventory.remove(type);
            currentTotalItemCount -= amount;
        }
    }
}
