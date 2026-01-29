package com.ksu1012.factory;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class WorldGenerator {

    private int width;
    private int height;
    private long seed;

    public WorldGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.seed = MathUtils.random(1000000);
        System.out.println("Seed: " + seed);
    }

    public Tile[][] generate() {
        Tile[][] map = new Tile[width][height];

        // Initialize map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = new Tile(x, y);
            }
        }

        // Generate Base Terrain
        PerlinNoise terrainNoise = new PerlinNoise(seed);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double elevation = terrainNoise.getFractalNoise(x, y, 0.035, 5, 0.4);

                if (elevation < -0.285) map[x][y].terrain = TerrainType.WATER;
                else if (elevation > 0.2) map[x][y].terrain = TerrainType.WALL;
                else map[x][y].terrain = TerrainType.DIRT;
            }
        }

        // Smooth Terrain
        for (int i = 0; i < 4; i++) {
            smoothTerrain(map);
        }

        generateResources(map);

        return map;
    }

    private void generateResources(Tile[][] map) {
        int totalMapArea = width * height;

        // Loop all resources
        for (ResourceType type : ResourceType.values()) {
            // Calculate how many patches to spawn based on map size
            int totalCount = (int) (totalMapArea * type.density);
            if (totalCount < 1) totalCount = 1;

            int veins = (int)(totalCount * 0.6f);
            int clusters = totalCount - veins;

            // Generate
            if (veins > 0) {
                generateVeins(map, type, veins, type.averageSize);
            }
            if (clusters > 0) {
                generateClusters(map, type, clusters, (int)(type.averageSize * 3f)); // Clusters slightly bigger?
            }
        }
    }

    // Removes isolated tiles
    private void smoothTerrain(Tile[][] map) {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                TerrainType type = map[x][y].terrain;
                if (type == TerrainType.DIRT) continue; // Don't smooth dirt

                int neighbors = 0;
                // Check 3x3
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (map[x + i][y + j].terrain == type) neighbors++;
                    }
                }

                // Remove water/walls with few neighbors
                if (neighbors < 4) {
                    map[x][y].terrain = TerrainType.DIRT;
                }
            }
        }
    }

    // Create streaks of ores
    private void generateVeins(Tile[][] map, ResourceType type, int count, int avgLength) {
        for (int i = 0; i < count; i++) {
            float x = MathUtils.random(5, width - 5);
            float y = MathUtils.random(5, height - 5);

            Vector2 dir = new Vector2(1, 0).setToRandomDirection();

            // Randomize length slightly
            int length = MathUtils.random((int)(avgLength * 0.8f), (int)(avgLength * 1.2f));

            // Thickness pulser
            float thickness = MathUtils.random(1.25f, 1.75f);

            for (int step = 0; step < length; step++) {
                // Vary thickness
                thickness += MathUtils.random(-0.3f, 0.3f);
                thickness = MathUtils.clamp(thickness, 1.0f, 2.0f);

                // Create rough edges
                drawNoisyBrush(map, (int)x, (int)y, thickness, type);

                x += dir.x;
                y += dir.y;

                // Wiggle direction
                dir.rotateDeg(MathUtils.random(-15f, 15f));

                if (x < 3 || x >= width - 3 || y < 3 || y >= height - 3) break;
            }
        }
    }

    // Create blob-like ore veins
    private void generateClusters(Tile[][] map, ResourceType type, int count, int size) {
        for (int i = 0; i < count; i++) {
            int startX = MathUtils.random(5, width - 5);
            int startY = MathUtils.random(5, height - 5);

            // List of tiles in this cluster we can grow from
            List<Vector2> growthFrontier = new ArrayList<>();
            growthFrontier.add(new Vector2(startX, startY));

            int placed = 0;
            while (placed < size && !growthFrontier.isEmpty()) {
                // Pick a random tile from the frontier to grow from
                int randIndex = MathUtils.random(0, growthFrontier.size() - 1);
                Vector2 current = growthFrontier.get(randIndex);

                int cx = (int)current.x;
                int cy = (int)current.y;

                // Attempt to place ore here
                if (cx >= 0 && cx < width && cy >= 0 && cy < height) {
                    if (map[cx][cy].terrain.isBuildable && map[cx][cy].resource != type) {
                        map[cx][cy].resource = type;
                        placed++;

                        // Add neighbors to frontier (Candidate spots to grow next)
                        growthFrontier.add(new Vector2(cx + 1, cy));
                        growthFrontier.add(new Vector2(cx - 1, cy));
                        growthFrontier.add(new Vector2(cx, cy + 1));
                        growthFrontier.add(new Vector2(cx, cy - 1));
                    }
                }

                // Remove this spot so we don't pick it again infinitely
                growthFrontier.remove(randIndex);
            }
        }
    }

    // Draws a circle with rough edges
    private void drawNoisyBrush(Tile[][] map, int centerX, int centerY, float radius, ResourceType type) {
        int r = (int) Math.ceil(radius);

        for (int x = centerX - r; x <= centerX + r; x++) {
            for (int y = centerY - r; y <= centerY + r; y++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {

                    float dist = Vector2.dst(centerX, centerY, x, y);

                    boolean shouldPaint = false;

                    if (dist < radius - 0.5f) {
                        shouldPaint = true; // Solid center
                    } else if (dist <= radius + 0.5f) {
                        shouldPaint = MathUtils.randomBoolean(0.6f); // Fuzzy edge
                    }

                    if (shouldPaint && map[x][y].terrain.isBuildable) {
                        map[x][y].resource = type;
                    }
                }
            }
        }
    }
}
