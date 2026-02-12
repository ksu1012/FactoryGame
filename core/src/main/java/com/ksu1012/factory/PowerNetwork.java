package com.ksu1012.factory;

import java.util.ArrayList;
import java.util.List;

public class PowerNetwork {
    public List<Building> members = new ArrayList<>();
    public float production = 0f;
    public float consumption = 0f;
    public float satisfaction = 0f; // 0.0 to 1.0

    public void addMember(Building b) {
        members.add(b);
        production += b.getDefinition().powerGeneration;
        consumption += b.getDefinition().powerConsumption;
        b.network = this;
    }

    public void update() {
        if (consumption <= 0) {
            satisfaction = 1.0f;
        } else {
            if (production >= consumption) {
                satisfaction = 1.0f;
            } else {
                satisfaction = production / consumption;
            }
        }

        // Update status for all members
        for (Building b : members) {
            // It has power if it produces power, doesn't need power, or the grid works
            if (b.getDefinition().powerGeneration > 0 || b.getDefinition().powerConsumption <= 0) {
                b.hasPower = true;
            } else {
                // Machines stop if power is too low (e.g. below 10%)
                b.hasPower = (satisfaction > 0.1f);
            }
        }
    }
}
