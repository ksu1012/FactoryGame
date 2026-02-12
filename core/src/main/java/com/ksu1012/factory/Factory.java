package com.ksu1012.factory;

import java.util.Map;

public class Factory extends Building {

    FactoryDef definition;
    private Recipe activeRecipe = null; // The recipe in progress
    private float progressTimer = 0f;

    public Factory(int x, int y, FactoryDef def) {
        super(x, y, def);
        this.definition = def;

        setAcceptsAnyItem(false);
        setGlobalCap(-1);

        // Accept all items that the Factory can use
        for (Recipe r : def.possibleRecipes) {
            // Allow inputs
            for (ItemType input : r.inputs.keySet()) {
                this.acceptedItems.add(input);

                // Set individual item caps (inputs)
                this.setItemCap(input, def.itemCapacity);
            }

            // Set individual item caps (outputs)
            for (ItemType output : r.outputs.keySet()) {
                this.setItemCap(output, def.itemCapacity);
            }
        }
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        // Check Power
        if (definition.powerConsumption > 0 && !hasPower) {
            return;
        }

        // Find Recipe
        if (activeRecipe == null) {
            findMatchingRecipe();
        }

        // Craft
        if (activeRecipe != null) {
            processRecipe(delta);
        }

        // Try to push outputs
        for (Recipe r : definition.possibleRecipes) {
            for (ItemType outputType : r.outputs.keySet()) {
                if (getItemCount(outputType) > 0) {
                    tryPushItem(grid, outputType);
                }
            }
        }
    }

    private void findMatchingRecipe() {
        // Iterate through all possible recipes for this machine
        for (Recipe r : definition.possibleRecipes) {
            if (canCraft(r)) {
                activeRecipe = r;
                return;
            }
        }
    }

    private void processRecipe(float delta) {
        // Check if crafting is possible
        if (canCraft(activeRecipe)) {
            progressTimer += delta * definition.craftingSpeed;

            if (progressTimer >= activeRecipe.craftTime) { // Finish crafting
                progressTimer -= activeRecipe.craftTime;
                produceItem(activeRecipe);

                activeRecipe = null;
            }
        } else {
            // Reset if crafting no longer possible
            progressTimer = 0f;
            activeRecipe = null;
        }
    }

    private boolean canCraft(Recipe r) {
        // Check Ingredients
        for (Map.Entry<ItemType, Integer> entry : r.inputs.entrySet()) {
            if (getItemCount(entry.getKey()) < entry.getValue()) return false;
        }

        // Check inventory capacity
        for (Map.Entry<ItemType, Integer> entry : r.outputs.entrySet()) {
            ItemType outType = entry.getKey();
            int amountToProduce = entry.getValue();

            int currentAmount = getItemCount(outType);

            // Get the specific limit for this item type
            int maxLimit = itemMaxes.get(outType);

            if (currentAmount + amountToProduce > maxLimit) {
                return false; // Output is full
            }
        }

        return true;
    }

    private void produceItem(Recipe r) {
        // Consume Inputs
        for (Map.Entry<ItemType, Integer> entry : r.inputs.entrySet()) {
            ItemType type = entry.getKey();
            int amount = entry.getValue();
            inventory.put(type, inventory.get(type) - amount);
            if (inventory.get(type) <= 0) inventory.remove(type);
            currentTotalItemCount -= amount;
        }

        // Add Outputs
        for (Map.Entry<ItemType, Integer> entry : r.outputs.entrySet()) {
            addInternalItem(entry.getKey(), entry.getValue());
        }
    }

    // Helper for visual feedback
    public Recipe getActiveRecipe() {
        return activeRecipe;
    }
}
