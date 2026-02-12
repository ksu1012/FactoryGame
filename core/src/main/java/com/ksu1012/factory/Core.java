package com.ksu1012.factory;

public class Core extends Building {

    public Core(int x, int y, CoreDef def) {
        super(x, y, def);

        // Accepts everything
        setAcceptsAnyItem(true);
        // "Infinite" storage (deletes items and adds to inventory counters)
        setGlobalCap(Integer.MAX_VALUE);
    }

    @Override
    public boolean addItem(ItemType type, int amount, Direction incomingDirection) {
        // Ignore directional checks
        return addInternalItem(type, amount);
    }

    @Override
    public void update(float delta, Tile[][] grid) {
        // If there are items, remove them all from Core and add to GameState inventory
        if (!inventory.isEmpty()) {
            for (ItemType type : new java.util.HashSet<>(inventory.keySet())) {
                int count = inventory.get(type);

                GameState.instance.addResource(type, count);

                inventory.remove(type);
                currentTotalItemCount -= count;
            }
        }
    }
}
