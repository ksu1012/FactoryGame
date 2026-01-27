package com.ksu1012.factory;

public class BuildingFactory {

    public static Building createBuilding(int x, int y, BuildingType type) {
        BuildingDef def = type.def;

        // Check the TYPE of the definition to decide which Java class to use
        if (def instanceof ConveyorDef) {
            return new Conveyor(x, y, (ConveyorDef) def);
        }
        else if (def instanceof DrillDef) {
            return new Drill(x, y, (DrillDef) def);
        }
        else if (def instanceof FactoryDef) {
            return new Factory(x, y, (FactoryDef) def);
        }

        return null;
    }
}
