package com.ksu1012.factory;

public class Recipes {
    // --- ORE REFINING ---
    public static final Recipe SMELT_COPPER_COAL = new Recipe(1.0f)
        .addInput(ItemType.COPPER_ORE, 1)
        .addInput(ItemType.COAL, 1)
        .addOutput(ItemType.COPPER, 1);

    public static final Recipe SMELT_IRON_COAL = new Recipe(1.0f)
        .addInput(ItemType.IRON_ORE, 1)
        .addInput(ItemType.COAL, 1)
        .addOutput(ItemType.IRON, 1);

    public static final Recipe BURN_COAL = new Recipe(1.0f)
        .addInput(ItemType.COAL, 1);
}
