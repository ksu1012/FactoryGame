package com.ksu1012.factory;

public class Conveyor extends Building {
    private float moveTimer = 0f;
    private final float MOVE_SPEED = 0.2f; // Time in seconds to move one item

    public Conveyor(int x, int y) {
        super(x, y);
        setGlobalCap(1);        // Holds 1 item
        setAcceptsAnyItem(true); // Accepts anything
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        // If we have an item, try to move it
        if (currentTotalItemCount > 0) {
            moveTimer += delta;

            if (moveTimer >= MOVE_SPEED) {
                if (tryPushItem(grid)) {
                    moveTimer -= MOVE_SPEED;
                }
            }
        } else {
            moveTimer = 0f;
        }
    }
}
