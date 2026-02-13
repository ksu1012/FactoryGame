package com.ksu1012.factory;

import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class PowerSystem {
    private List<PowerNetwork> networks = new ArrayList<>();

    // O(N^2), but should be fine (only runs when placing or deleting)
    public void rebuildNetworks(List<Building> allBuildings) {
        networks.clear();
        Set<Building> visited = new HashSet<>();

        // Reset every Building
        for (Building b : allBuildings) {
            b.network = null;
            b.hasPower = false;
        }

        // BFS Loop
        for (Building startNode : allBuildings) {
            // Skip if visited or if it doesn't interact with power at all
            if (visited.contains(startNode)) continue;
            if (!usesPower(startNode)) continue;

            // Start a new Network
            PowerNetwork net = new PowerNetwork();
            networks.add(net);

            Queue<Building> queue = new LinkedList<>();
            queue.add(startNode);
            visited.add(startNode);
            net.addMember(startNode);

            while (!queue.isEmpty()) {
                Building current = queue.poll();

                // Find neighbors
                for (Building other : allBuildings) {
                    if (visited.contains(other)) continue;
                    if (!usesPower(other)) continue;

                    // Check Distance
                    // Use the greater connection radius of the two Buildings (e.g. a PowerPole can connect to a Factory without contact)
                    float dist = Vector2.dst(current.x, current.y, other.x, other.y);
                    float range = Math.max(current.getConnectionRadius(), other.getConnectionRadius());

                    if (dist <= range) {
                        visited.add(other);
                        queue.add(other);
                        net.addMember(other);
                    }
                }
            }
        }

        update(0);
    }

    public void update(float delta) {
        for (PowerNetwork net : networks) {
            net.update(delta);
        }
    }

    private boolean usesPower(Building b) {
        return b.connectsToPower();
    }

    public List<PowerNetwork> getNetworks() { return networks; }
}
