package com.ksu1012.factory;

interface BuildingBuilder {
    Building build(int x, int y, BuildingDef def);
}

/**
 * A Data-Driven definition of all game entities.
 * <p>
 * Utilizes the <b>Factory Pattern</b> via Functional Interfaces to dynamically
 * instantiate specific building classes without requiring hardcoded switch statements
 * in the game loop.
 */

public enum BuildingType {
    // --- CONVEYORS ---
    BASIC_CONVEYOR(new ConveyorDef(0.5f)
        .addCost(ItemType.COPPER, 1),
        (x, y, def) -> new Conveyor(x, y, (ConveyorDef) def)), // Moves one item every 0.5s

    FAST_CONVEYOR(new ConveyorDef(0.2f)
        .addCost(ItemType.IRON, 1),
        (x, y, def) -> new Conveyor(x, y, (ConveyorDef) def)),  // Moves one item every 0.2s


    // --- DRILLS ---
    BASIC_DRILL(new DrillDef(1, 1, 1.0f, 1)
        .addCost(ItemType.COPPER, 5),
        (x, y, def) -> new Drill(x, y, (DrillDef) def)),

    LARGE_DRILL(new DrillDef(2, 2, 3.2f, 4)
        .addCost(ItemType.COPPER, 10)
        .addCost(ItemType.IRON, 5),
        (x, y, def) -> new Drill(x, y, (DrillDef) def)),

    // --- FACTORIES ---
    SMELTER(new FactoryDef(2, 2, 1.0f, 10,
        Recipes.SMELT_COPPER_COAL,
        Recipes.SMELT_IRON_COAL)
        .addCost(ItemType.COPPER, 10),
        (x, y, def) -> new Factory(x, y, (FactoryDef) def)),

    INDUSTRIAL_SMELTER(new FactoryDef(3, 3, 5.0f, 50,
        Recipes.SMELT_COPPER_COAL,
        Recipes.SMELT_IRON_COAL)
        .addCost(ItemType.COPPER, 30)
        .addCost(ItemType.IRON, 15),
        (x, y, def) -> new Factory(x, y, (FactoryDef) def)),

    // --- GENERATORS ---
    COAL_GENERATOR(new GeneratorDef(2, 2, 1.0f, 10, 100f, 1000f, Recipes.BURN_COAL)
        .addCost(ItemType.COPPER, 10)
        .addCost(ItemType.IRON, 5),
        (x, y, def) -> new Generator(x, y, (GeneratorDef) def)),

    // --- POWER POLES ---
    POWER_POLE(new PowerPoleDef(1, 1)
        .addCost(ItemType.COPPER, 1),
        PowerPole::new),

    // --- BATTERIES ---
    BATTERY(new BatteryDef(1, 1, 10000f)
        .addCost(ItemType.COPPER, 5),
        Battery::new),

    // --- CORE ---
    CORE(new CoreDef(3, 3),
        (x, y, def) -> new Core(x, y, (CoreDef) def));


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
