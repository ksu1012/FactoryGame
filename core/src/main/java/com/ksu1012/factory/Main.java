package com.ksu1012.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    // --- GAME SETTINGS ---
    private final int TILE_SIZE = 32;
    private final int MAP_WIDTH = 50;
    private final int MAP_HEIGHT = 50;

    // --- DATA LAYER ---
    private Tile[][] map; // The actual data storage
    private ArrayList<Building> buildings = new ArrayList<>(); // Optimization list

    // Selection state
    private Direction currentFacing = Direction.NORTH;
    private BuildingType selectedBuilding = BuildingType.CONVEYOR;

    // --- VISUAL SETTINGS ---
    private final Color GROUND_COLOR_1 = new Color(0.15f, 0.15f, 0.15f, 1f);
    private final Color GROUND_COLOR_2 = new Color(0.18f, 0.18f, 0.18f, 1f);
    private final Color WALL_COLOR = new Color(0.05f, 0.05f, 0.05f, 1f); // Dark Grey/Black
    private final Color WATER_COLOR = new Color(0.2f, 0.4f, 0.8f, 1f);   // Blue
    private final Color LAVA_COLOR = new Color(0.8f, 0.2f, 0.1f, 1f);    // Red

    private final Color HIGHLIGHT_COLOR = new Color(1f, 1f, 1f, 0.3f); // Semi-transparent white
    private final Color RESOURCE_COLOR = new Color(0.8f, 0.5f, 0.2f, 1f); // Copper-ish color
    private final Color DRILL_COLOR = new Color(0.4f, 0.8f, 0.4f, 1f); // Green
    private final Color FACTORY_COLOR = new Color(0.9f, 0.6f, 0.2f, 1f); // Orange

    // --- PHYSICS SETTINGS ---
    private final float ACCELERATION = 4000f;
    private final float MAX_SPEED = 500f;
    private final float FRICTION = 0.93f;

    // --- PHYSICS STATE ---
    private Vector2 velocity = new Vector2(0, 0);
    private Vector2 inputVector = new Vector2(0, 0);

    // Mouse Interaction
    private Vector3 mousePos = new Vector3(); // Vector3 because camera uses 3D space (Z-axis)
    private Tile hoveredTile = null;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.position.set(MAP_WIDTH * TILE_SIZE / 2f, MAP_HEIGHT * TILE_SIZE / 2f, 0);
        shapeRenderer = new ShapeRenderer();

        // --- MAP GENERATION ---
        WorldGenerator generator = new WorldGenerator(MAP_WIDTH, MAP_HEIGHT);
        this.map = generator.generate();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                float zoomSpeed = 0.1f;
                camera.zoom += amountY * camera.zoom * zoomSpeed;
                camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 4.0f);
                return true;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        // Update logic
        update(deltaTime);

        // Draw visuals
        draw();
    }

    // Handles all game logic, physics, and input.
    private void update(float delta) {
        updatePosition(delta);

        // --- MOUSE INPUT ---
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);

        int gridX = (int) (mousePos.x / TILE_SIZE);
        int gridY = (int) (mousePos.y / TILE_SIZE);

        if (gridX >= 0 && gridX < MAP_WIDTH && gridY >= 0 && gridY < MAP_HEIGHT) {
            hoveredTile = map[gridX][gridY];
        } else {
            hoveredTile = null;
        }

        // Selection Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            selectedBuilding = BuildingType.CONVEYOR;
            System.out.println("Selected: Conveyor");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            selectedBuilding = BuildingType.DRILL;
            System.out.println("Selected: Drill");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            selectedBuilding = BuildingType.FACTORY;
            System.out.println("Selected: Factory");
        }

        // --- ROTATION ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            currentFacing = currentFacing.next(); // Rotate Clockwise
        }

        // --- PLACEMENT ---
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // Only allow placement if tile exists and is empty
            if (hoveredTile != null) {

                Building newBuilding = null;

                // Create object to check dimensions
                switch (selectedBuilding) {
                    case CONVEYOR:
                        newBuilding = new Conveyor(gridX, gridY);
                        break;
                    case DRILL:
                        newBuilding = new Drill(gridX, gridY);
                        break;
                    case FACTORY:
                        newBuilding = new Factory(gridX, gridY);
                        break;
                }

                if (newBuilding != null) {
                    // Check if footprint is valid (terrain/overlap)
                    if (canPlaceBuilding(gridX, gridY, newBuilding)) {
                        newBuilding.facing = currentFacing; // Apply rotation
                        buildings.add(newBuilding);

                        // Occupy all tiles in footprint
                        for (int i = 0; i < newBuilding.width; i++) {
                            for (int j = 0; j < newBuilding.height; j++) {
                                map[gridX + i][gridY + j].building = newBuilding;
                            }
                        }
                    } else {
                        System.out.println("Cannot build here (Blocked/Invalid)");
                    }
                }
            }
        }

        // Remove a building upon right-clicking
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            if (hoveredTile != null && hoveredTile.building != null) {
                Building b = hoveredTile.building;
                buildings.remove(b);

                // Clear the whole footprint
                for (int i = 0; i < b.width; i++) {
                    for (int j = 0; j < b.height; j++) {
                        if (b.x + i < MAP_WIDTH && b.y + j < MAP_HEIGHT) {
                            map[b.x + i][b.y + j].building = null;
                        }
                    }
                }
            }
        }

        // Update all buildings
        for (Building b : buildings) {
            b.update(delta, map);
        }
    }

    // Check against bounds, occupancy, and Building's own terrain rules
    private boolean canPlaceBuilding(int startX, int startY, Building building) {
        if (startX < 0 || startY < 0 || startX + building.width > MAP_WIDTH || startY + building.height > MAP_HEIGHT) {
            return false;
        }

        for (int i = 0; i < building.width; i++) {
            for (int j = 0; j < building.height; j++) {
                Tile t = map[startX + i][startY + j];
                // Occupied check
                if (t.building != null) return false;

                // Terrain check
                if (!building.canBuildOn(t.terrain)) return false;
            }
        }
        return true;
    }

    private void updatePosition(float delta) {
        calculateVectors(delta);

        camera.position.x += velocity.x * delta;
        camera.position.y += velocity.y * delta;
        camera.update();
    }

    // Handles rendering
    private void draw() {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Calculate Visible Range
        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;

        int startX = (int) ((camera.position.x - viewWidth / 2) / TILE_SIZE) - 2;
        int endX = (int) ((camera.position.x + viewWidth / 2) / TILE_SIZE) + 2;
        int startY = (int) ((camera.position.y - viewHeight / 2) / TILE_SIZE) - 2;
        int endY = (int) ((camera.position.y + viewHeight / 2) / TILE_SIZE) + 2;

        // Clamp to map bounds
        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(MAP_WIDTH, endX);
        endY = Math.min(MAP_HEIGHT, endY);

        // Render visible tiles (Ground Layer)
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                drawGround(x, y);
            }
        }

        // Render Buildings (Building Layer)
        // We iterate the list to ensure large buildings don't clip when their anchor is off-screen
        for (Building b : buildings) {
            // Optimization: Simple bound check
            float bx = b.x * TILE_SIZE;
            float by = b.y * TILE_SIZE;
            if (bx + (b.width * TILE_SIZE) > camera.position.x - viewWidth / 2 &&
                bx < camera.position.x + viewWidth / 2 &&
                by + (b.height * TILE_SIZE) > camera.position.y - viewHeight / 2 &&
                by < camera.position.y + viewHeight / 2) {

                drawBuilding(b);
            }
        }

        // Preview drawing (Ghost Layer)
        if (hoveredTile != null && hoveredTile.building == null) {
            Building temp = null;
            switch (selectedBuilding) {
                case CONVEYOR: temp = new Conveyor(hoveredTile.x, hoveredTile.y); break;
                case DRILL:    temp = new Drill(hoveredTile.x, hoveredTile.y); break;
                case FACTORY:  temp = new Factory(hoveredTile.x, hoveredTile.y); break;
            }

            if (temp != null) {
                // Pass temp building to check against its terrain rules
                boolean isValid = canPlaceBuilding(hoveredTile.x, hoveredTile.y, temp);

                Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
                if (isValid) {
                    shapeRenderer.setColor(1f, 1f, 1f, 0.5f); // Valid
                } else {
                    shapeRenderer.setColor(1f, 0f, 0f, 0.5f); // Invalid
                }

                int gx = hoveredTile.x * TILE_SIZE;
                int gy = hoveredTile.y * TILE_SIZE;
                shapeRenderer.rect(gx, gy, temp.width * TILE_SIZE, temp.height * TILE_SIZE);

                // Draw Direction Indicator
                shapeRenderer.setColor(Color.YELLOW);
                float centerX = gx + (temp.width * TILE_SIZE) / 2f;
                float centerY = gy + (temp.height * TILE_SIZE) / 2f;
                float dotX = centerX + (currentFacing.dx * temp.width * TILE_SIZE / 2f) - 3;
                float dotY = centerY + (currentFacing.dy * temp.width * TILE_SIZE / 2f) - 3;
                shapeRenderer.rect(dotX, dotY, 6, 6);
            }
        }

        // Highlight tile
        if (hoveredTile != null) {
            shapeRenderer.setColor(HIGHLIGHT_COLOR);
            shapeRenderer.rect(hoveredTile.x * TILE_SIZE, hoveredTile.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    private void drawGround(int x, int y) {
        Tile tile = map[x][y];

        // Draw base terrain
        switch(tile.terrain) {
            case DIRT:
                if ((x + y) % 2 == 0) shapeRenderer.setColor(GROUND_COLOR_1);
                else shapeRenderer.setColor(GROUND_COLOR_2);
                break;
            case WALL:
                shapeRenderer.setColor(WALL_COLOR);
                break;
            case WATER:
                shapeRenderer.setColor(WATER_COLOR);
                break;
            case LAVA:
                shapeRenderer.setColor(LAVA_COLOR);
                break;
        }
        shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw resource
        if (tile.resource != null) {
            if (tile.resource == ResourceType.COPPER_ORE) {
                shapeRenderer.setColor(RESOURCE_COLOR);
            }

            float margin = 4;
            shapeRenderer.rect(
                (x * TILE_SIZE) + margin,
                (y * TILE_SIZE) + margin,
                TILE_SIZE - (margin * 2),
                TILE_SIZE - (margin * 2)
            );
        }
    }

    private void drawBuilding(Building b) {
        // --- DRAW BUILDING ---
        if (b instanceof Drill) {
            shapeRenderer.setColor(DRILL_COLOR);
        } else if (b instanceof Conveyor) {
            shapeRenderer.setColor(Color.DARK_GRAY);
        } else if (b instanceof Factory) {
            shapeRenderer.setColor(FACTORY_COLOR);
        }

        // Draw the main box using width/height
        shapeRenderer.rect(
            (b.x * TILE_SIZE) + 2,
            (b.y * TILE_SIZE) + 2,
            (b.width * TILE_SIZE) - 4,
            (b.height * TILE_SIZE) - 4
        );

        // --- DRAW DIRECTION INDICATOR (Yellow Dot) ---
        shapeRenderer.setColor(Color.YELLOW);
        float centerX = (b.x * TILE_SIZE) + (b.width * TILE_SIZE) / 2f;
        float centerY = (b.y * TILE_SIZE) + (b.height * TILE_SIZE) / 2f;

        // Offset the dot based on facing direction (dx/dy)
        float dotX = centerX + (b.facing.dx * 10) - 3;
        float dotY = centerY + (b.facing.dy * 10) - 3;
        shapeRenderer.rect(dotX, dotY, 6, 6);

        // --- DRAW ITEMS ---
        ItemType item = b.getFirstItem();
        if (item != null) {
            shapeRenderer.setColor(item.color);
            // Draw item slightly smaller in center
            shapeRenderer.rect(centerX - 4, centerY - 4, 8, 8);
        }
    }

    private void calculateVectors(float delta) {
        inputVector.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) inputVector.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) inputVector.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) inputVector.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputVector.x += 1;

        if (inputVector.len() > 0) {
            inputVector.nor();
        }

        if (inputVector.len() > 0) {
            velocity.x += inputVector.x * ACCELERATION * delta;
            velocity.y += inputVector.y * ACCELERATION * delta;
        }

        velocity.scl(FRICTION);

        if (velocity.len() < 10f) {
            velocity.set(0, 0);
        }

        velocity.clamp(0, MAX_SPEED);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
