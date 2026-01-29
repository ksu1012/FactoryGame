package com.ksu1012.factory;

interface BuildingBuilder {
    Building build(int x, int y, BuildingDef def);
}

public enum BuildingType {
    // --- CONVEYORS ---
    BASIC_CONVEYOR(new ConveyorDef(0.5f), (x, y, def) -> new Conveyor(x, y, (ConveyorDef) def)), // Moves one item every 0.5s
    FAST_CONVEYOR(new ConveyorDef(0.2f), (x, y, def) -> new Conveyor(x, y, (ConveyorDef) def)),  // Moves one item every 0.2s

    // --- DRILLS ---
    BASIC_DRILL(new DrillDef(1, 1, 1.0f, 1), (x, y, def) -> new Drill(x, y, (DrillDef) def)),
    LARGE_DRILL(new DrillDef(2, 2, 3.2f, 4), (x, y, def) -> new Drill(x, y, (DrillDef) def)),

    // --- FACTORIES ---
    SMELTER(new FactoryDef(2, 2, 1.0f, 10,
        Recipes.SMELT_COPPER_COAL,
        Recipes.SMELT_IRON_COAL),
        (x, y, def) -> new Factory(x, y, (FactoryDef) def)),

    INDUSTRIAL_SMELTER(new FactoryDef(3, 3, 5.0f, 50,
        Recipes.SMELT_COPPER_COAL,
        Recipes.SMELT_IRON_COAL),
        (x, y, def) -> new Factory(x, y, (FactoryDef) def)),

    // --- CORE ---
    CORE(new CoreDef(3, 3), (x, y, def) -> new Core(x, y));

    public final BuildingDef def;
    private final BuildingBuilder builder;

    BuildingType(BuildingDef def, BuildingBuilder builder) {
        this.def = def;
        this.builder = builder;
    }

    public Building create(int x, int y) {
        return builder.build(x, y, this.def);
    }
}
