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

    public void update(float delta) {
        // Reset Totals
        production = 0f;
        consumption = 0f;
        float totalCapacity = 0f;
        float totalStored = 0f;

        // Calculate Potentials
        for (Building b : members) {
            production += b.powerProduction;
            consumption += b.powerConsumption;

            // Battery Logic uses b.maxEnergy now
            if (b.energyCapacity > 0) {
                totalCapacity += b.energyCapacity;
                totalStored += b.currentEnergy;
            }
        }

        // Calculate Net Power for this frame
        // (Production - Consumption) * Time = Net Power
        float netEnergy = (production - consumption) * delta;

        // Handle power usage
        if (netEnergy > 0) {
            // Power surplus
            satisfaction = 1.0f; // Factories run at 100%

            // Distribute netEnergy into batteries
            if (totalCapacity > 0) {
                float energyToAdd = netEnergy;

                // Add to individual buildings
                for (Building b : members) {
                    if (b.isBattery() && energyToAdd > 0) {
                        float space = b.getDefinition().energyCapacity - b.currentEnergy;
                        float taking = Math.min(space, energyToAdd); // Accept only up to the cap

                        b.currentEnergy += taking;
                        energyToAdd -= taking;
                    }
                }
            }
        } else {
            // Power Deficit
            float energyNeeded = -netEnergy; // Make positive

            if (totalStored >= energyNeeded) {
                // Enough stored energy to continue operating
                satisfaction = 1.0f;

                // Drain the batteries
                for (Building b : members) {
                    if (b.isBattery() && energyNeeded > 0) {
                        float available = b.currentEnergy;
                        float taking = Math.min(available, energyNeeded);

                        b.currentEnergy -= taking;
                        energyNeeded -= taking;
                    }
                }
            } else {
                // Insufficient power from both generation and batteries
                // Satisfaction = Available / Required
                float totalAvailableForFrame = (production * delta) + totalStored;
                float totalRequiredForFrame = (consumption * delta);

                if (totalRequiredForFrame > 0) {
                    satisfaction = totalAvailableForFrame / totalRequiredForFrame;
                } else {
                    satisfaction = 1.0f;
                }

                // Empty batteries because there is no power
                for (Building b : members) {
                    if (b.isBattery()) b.currentEnergy = 0f;
                }
            }
        }

        // Update Buildings status
        for (Building b : members) {
            // Buildings require at least 10% power to function
            b.hasPower = (satisfaction > 0.1f);
        }
    }
}
