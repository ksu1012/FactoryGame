package com.ksu1012.factory;

import com.badlogic.gdx.math.MathUtils;

public class WorldGenerator {

    private int width;
    private int height;

    public WorldGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Tile[][] generate() {
        Tile[][] map = new Tile[width][height];
        generateTerrain(map);
        generateResources(map);
        return map;
    }

    private void generateTerrain(Tile[][] map) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = new Tile(x, y);

                float random = MathUtils.random();

                // Randomly generate terrain, to be replaced with Perlin noise
                if (random < 0.05) map[x][y].terrain = TerrainType.WALL;
                else if (random < 0.15) map[x][y].terrain = TerrainType.WATER;
                else if (random < 0.18) map[x][y].terrain = TerrainType.LAVA;
                else {
                    map[x][y].terrain = TerrainType.DIRT;
                }
            }
        }
    }

    private void generateResources(Tile[][] map) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = map[x][y];

                // Temporary logic for generating ores on land
                if (tile.terrain.isBuildable) {

                    if (MathUtils.randomBoolean(0.08f)) { // 8% chance
                        tile.resource = ResourceType.COPPER_ORE;
                    } // add else-ifs to add more resources in the future
                }

                // placeholder for special case checks in the future
                else if (tile.terrain == TerrainType.WATER) {
                }
            }
        }
    }
}
