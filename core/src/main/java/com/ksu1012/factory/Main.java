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

    // --- VISUAL SETTINGS ---
    private final Color GROUND_COLOR_1 = new Color(0.15f, 0.15f, 0.15f, 1f);
    private final Color GROUND_COLOR_2 = new Color(0.18f, 0.18f, 0.18f, 1f);
    private final Color HIGHLIGHT_COLOR = new Color(1f, 1f, 1f, 0.3f); // Semi-transparent white
    private final Color RESOURCE_COLOR = new Color(0.8f, 0.5f, 0.2f, 1f); // Copper-ish color
    private final Color DRILL_COLOR = new Color(0.4f, 0.8f, 0.4f, 1f); // Green

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

        // --- INITIALIZE MAP DATA ---
        map = new Tile[MAP_WIDTH][MAP_HEIGHT];
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                map[x][y] = new Tile(x, y);

                // Temp: Randomly scatter some resources
                if (Math.random() < 0.1) { // 10% chance
                    map[x][y].type = TerrainType.COPPER_ORE;
                }
            }
        }

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

        // --- BUILDING PLACEMENT ---
        // Place a building upon left-clicking if possible
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (hoveredTile != null && hoveredTile.building == null && hoveredTile.type == TerrainType.COPPER_ORE) {
                Building newDrill = new Drill(gridX, gridY);
                hoveredTile.building = newDrill;
                buildings.add(newDrill); // Add to optimization list
            }
        }

        // Remove a building upon right-clicking
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            if (hoveredTile != null && hoveredTile.building != null) {
                buildings.remove(hoveredTile.building);
                hoveredTile.building = null;
            }
        }

        // Update all buildings
        for (Building b : buildings) {
            Tile tile = map[b.x][b.y];
            b.update(delta, tile);
        }
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

        // Render only visible tiles
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                drawTile(x, y);
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

    public void drawTile(int x, int y) {
        Tile tile = map[x][y];

        // Draw Base
        if ((x + y) % 2 == 0) {
            shapeRenderer.setColor(GROUND_COLOR_1);
        } else {
            shapeRenderer.setColor(GROUND_COLOR_2);
        }
        shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // --- Draw Resources ---
        switch (tile.type) {
            case DIRT:
                // Draw nothing
                break;

            case COPPER_ORE: // Draw a slightly smaller square within to show the resource
                shapeRenderer.setColor(RESOURCE_COLOR);
                float margin = 4;
                shapeRenderer.rect(
                    (x * TILE_SIZE) + margin,
                    (y * TILE_SIZE) + margin,
                    TILE_SIZE - (margin * 2),
                    TILE_SIZE - (margin * 2)
                );
                break;
        }

        // Draw Buildings
        if (tile.building != null) {
            if (tile.building instanceof Drill) {
                shapeRenderer.setColor(DRILL_COLOR);
                float margin = 2;
                shapeRenderer.rect(
                    (x * TILE_SIZE) + margin,
                    (y * TILE_SIZE) + margin,
                    TILE_SIZE - (margin * 2),
                    TILE_SIZE - (margin * 2)
                );

                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rect((x * TILE_SIZE) + 12, (y * TILE_SIZE) + 12, 8, 8);

                // Visual indicator for contents (temporary)
                ItemType item = tile.building.getFirstItem();

                if (item != null && tile.building.getItemCount(item) > 0) {
                    shapeRenderer.setColor(item.color);
                    shapeRenderer.rect((x * TILE_SIZE) + 14, (y * TILE_SIZE) + 14, 4, 4);
                }
            }
        }
    }

    // Movement vector calculations
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
