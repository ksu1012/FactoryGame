package com.ksu1012.factory;

public enum BuildingType {
    // --- CONVEYORS ---
    BASIC_CONVEYOR(new ConveyorDef(0.5f)), // Moves every 0.5s
    FAST_CONVEYOR(new ConveyorDef(0.2f)),  // Moves every 0.2s

    // --- DRILLS ---
    BASIC_DRILL(new DrillDef(1, 1, 1.0f, 1)),
    LARGE_DRILL(new DrillDef(2, 2, 0.8f, 4)), // 2x2, twice as fast and produces 4x.
                                                                              // Need to implement scaling based on how much resource it is on

    // --- FACTORIES ---
    SMELTER(new FactoryDef(2, 2, 1.0f, 10,
        Recipes.SMELT_COPPER_COAL,
        Recipes.SMELT_IRON_COAL
    )),

    INDUSTRIAL_SMELTER(new FactoryDef(3, 3, 5.0f, 50,
                       Recipes.SMELT_COPPER_COAL,
                       Recipes.SMELT_IRON_COAL
    ));

    public final BuildingDef def;

    BuildingType(BuildingDef def) {
        this.def = def;
    }
}
