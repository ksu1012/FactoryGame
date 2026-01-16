package com.ksu1012.factory;

public class Drill extends Building {

    private float timer = 0f;
    private final float MINING_SPEED = 1.0f;

    public Drill(int x, int y) {
        super(x, y);
    }

    @Override
    public void update(float delta) {
        timer += delta;

        if (timer >= MINING_SPEED) {
            timer -= MINING_SPEED;
            // Mine an item (currently assumed to be copper)
            if (getItemCount(ItemType.COPPER_ORE) < 10) {
                addItem(ItemType.COPPER_ORE, 1);
                System.out.println("Drill at (" + x + "," + y + ") mined Copper! Total: " + getItemCount(ItemType.COPPER_ORE));
            } else {
                System.out.println("Drill at (" + x + "," + y + ") tried to mine but is maxed! Total: " + getItemCount(ItemType.COPPER_ORE));
            }
        }
    }
}
