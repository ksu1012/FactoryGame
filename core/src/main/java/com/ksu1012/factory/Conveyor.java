package com.ksu1012.factory;

public class Conveyor extends Building {
    private float moveTimer = 0f;
    private float moveSpeed;

    // Constructor takes the Definition now!
    public Conveyor(int x, int y, ConveyorDef def) {
        super(x, y);
        this.width = def.width;
        this.height = def.height;
        this.moveSpeed = def.speed;

        setGlobalCap(1); // Holds 1 item
        setAcceptsAnyItem(true); // Accepts anything
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        if (currentTotalItemCount > 0) {
            moveTimer += delta;
            if (moveTimer >= moveSpeed) {
                if (tryPushItem(grid)) {
                    moveTimer -= moveSpeed;
                }
            }
        } else {
            moveTimer = 0f;
        }
    }
}
