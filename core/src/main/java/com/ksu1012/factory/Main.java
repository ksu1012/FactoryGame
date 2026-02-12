package com.ksu1012.factory;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.Map;

/**
 * The core Engine class managing the Game Loop.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li><b>Render Pipeline:</b> Manages the SpriteBatch and Frustum Culling logic to render only visible tiles.</li>
 *   <li><b>Simulation Loop:</b> Decouples game logic updates from rendering frames.</li>
 *   <li><b>Input Handling:</b> Multiplexes UI events (Scene2D) and World events (Placement/Camera).</li>
 * </ul>
 */

public class Main extends ApplicationAdapter {
    private OrthographicCamera camera;

    // Replaced ShapeRenderer with SpriteBatch
    private com.badlogic.gdx.graphics.g2d.SpriteBatch batch;
    private com.badlogic.gdx.graphics.Texture whitePixel;

    // --- GAME SETTINGS ---
    private final int TILE_SIZE = 32;
    private final int MAP_WIDTH = 500;
    private final int MAP_HEIGHT = 500;

    // --- DATA LAYER ---
    private Tile[][] map; // The actual data storage
    private ArrayList<Building> buildings = new ArrayList<>(); // Optimization list
    private PowerSystem powerSystem = new PowerSystem();

    // Selection state
    private Direction currentFacing = Direction.NORTH;
    private BuildingType selectedBuilding = BuildingType.BASIC_CONVEYOR;

    // --- VISUAL SETTINGS ---
    private final Color CORE_COLOR = new Color(0.8f, 0.2f, 0.8f, 1f);
    private final Color HIGHLIGHT_COLOR = new Color(1f, 1f, 1f, 0.3f); // Semi-transparent white
    private final Color DRILL_COLOR = new Color(0.4f, 0.8f, 0.4f, 1f); // Green
    private final Color FACTORY_COLOR = new Color(0.9f, 0.6f, 0.2f, 1f); // Orange
    private final Color POWER_LINE_COLOR = new Color(1.0f, 0.9f, 0.4f, 0.6f);
    private final Color POWER_POLE_COLOR = new Color(0.7f, 0.7f, 0.5f, 1f);

    // --- UI ---
    private float uiTimer = 0f;
    private Stage uiStage;
    private Label resourceLabel;
    private Label selectionLabel;
    private StringBuilder hudString = new StringBuilder();

    // --- PHYSICS SETTINGS ---
    private final float ACCELERATION = 10000f;
    private final float MAX_SPEED = 1000f;
    private final float FRICTION = 0.96f;

    // --- PHYSICS STATE ---
    private Vector2 velocity = new Vector2(0, 0);
    private Vector2 inputVector = new Vector2(0, 0);

    // Mouse Interaction
    private Vector3 mousePos = new Vector3(); // Vector3 because camera uses 3D space (Z-axis)
    private Tile hoveredTile = null;

    @Override
    public void create() {
        // TEMP: START WITH RESOURCES
        GameState.instance.addResource(ItemType.COPPER, 100);
        GameState.instance.addResource(ItemType.IRON, 100);

        camera = new OrthographicCamera();
        camera.position.set(MAP_WIDTH * TILE_SIZE / 2f, MAP_HEIGHT * TILE_SIZE / 2f, 0);

        // New Rendering system
        batch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();

        // 1x1 white pixel
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();

        // --- MAP GENERATION ---
        WorldGenerator worldGenerator = new WorldGenerator(MAP_WIDTH, MAP_HEIGHT);
        this.map = worldGenerator.generate();

        int centerX = MAP_WIDTH / 2;
        int centerY = MAP_HEIGHT / 2;

        // Clear an area in the center of the map (where the core will be)
        int clearRadius = 5;
        for (int x = centerX - clearRadius; x <= centerX + clearRadius; x++) {
            for (int y = centerY - clearRadius; y <= centerY + clearRadius; y++) {
                if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                    map[x][y].terrain = TerrainType.DIRT;
                    map[x][y].resource = null;
                    map[x][y].building = null;
                }
            }
        }

        // Spawn core
        Core core = new Core(centerX - 1, centerY - 1, new CoreDef(3, 3));
        buildings.add(core);

        for (int i = -1; i < core.width - 1; i++) {
            for (int j = -1; j < core.height - 1; j++) {
                if (centerX + i < MAP_WIDTH && centerY + j < MAP_HEIGHT) {
                    map[centerX + i][centerY + j].building = core;

                    map[centerX + i][centerY + j].terrain = TerrainType.DIRT;
                    map[centerX + i][centerY + j].resource = null;
                }
            }
        }

        // --- UI SETUP ---
        uiStage = new Stage(new ScreenViewport());

        BitmapFont font = new BitmapFont();
        font.getData().setScale(2.0f);

        LabelStyle style = new LabelStyle();
        style.font = font;
        style.fontColor = Color.WHITE;

        // Gray background for buttons
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f)); // Dark Grey, slightly transparent
        bgPixmap.fill();
        TextureRegionDrawable buttonBg = new TextureRegionDrawable(new Texture(bgPixmap));

        // Lighter gray for selected option
        bgPixmap.setColor(new Color(0.4f, 0.4f, 0.4f, 0.8f));
        bgPixmap.fill();
        TextureRegionDrawable buttonDown = new TextureRegionDrawable(new Texture(bgPixmap));

        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.up = buttonBg;
        buttonStyle.down = buttonDown;
        buttonStyle.checked = buttonDown; // Selected
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;

        // Create the Toolbar Table
        Table toolbarTable = new Table();
        toolbarTable.bottom(); // Align to bottom of screen
        toolbarTable.setFillParent(true);

        // Add Buttons from the BuildingType enum, skipping core (maybe allow this in future)
        for (BuildingType type : BuildingType.values()) {
            if (type == BuildingType.CORE) continue;

            // Create Button
            String name = type.name().substring(0, 1) + type.name().substring(1).toLowerCase();
            TextButton button = new TextButton(name, buttonStyle);

            // Add listener
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedBuilding = type;
                    // Update visuals
                }
            });

            toolbarTable.add(button).pad(5).width(120).height(40);
        }

        // Add Toolbar to Stage
        uiStage.addActor(toolbarTable);

        // Table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        rootTable.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);


        // Create Labels
        resourceLabel = new Label("Resources: 0", style);
        selectionLabel = new Label("Selected: Conveyor", style);

        // Add to Table
        rootTable.add(resourceLabel).pad(10).left();
        rootTable.row(); // New line
        rootTable.add(selectionLabel).pad(10).left();
        rootTable.row().expandY(); // Push everything else down

        uiStage.addActor(rootTable);

        InputMultiplexer multiplexer = new InputMultiplexer();

        multiplexer.addProcessor(uiStage);

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                float zoomSpeed = 0.1f;
                camera.zoom += amountY * camera.zoom * zoomSpeed;
                camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 4.0f);
                return true;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // Update UI Viewport
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 0.1f); // Only do up to 0.1s of simulation per frame

        // Update logic
        update(deltaTime);

        // Draw visuals
        draw();

        // Draw UI
        uiTimer += deltaTime;
        if (uiTimer >= 0.1f) {
            updateUI();
            uiTimer = 0f;
        }
        uiStage.draw();
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedBuilding = BuildingType.BASIC_CONVEYOR;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectedBuilding = BuildingType.FAST_CONVEYOR;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) selectedBuilding = BuildingType.BASIC_DRILL;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) selectedBuilding = BuildingType.LARGE_DRILL;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) selectedBuilding = BuildingType.SMELTER;

        // --- ROTATION ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            currentFacing = currentFacing.next(); // Rotate Clockwise
        }

        // --- PLACEMENT ---
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 stagePos = uiStage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            // Ignore Button clicks
            if (uiStage.hit(stagePos.x, stagePos.y, true) != null) {
                return;
            }

            if (hoveredTile != null) {
                Building newBuilding = selectedBuilding.create(gridX, gridY);

                if (newBuilding != null) {
                    // Set orientation
                    newBuilding.setFacing(currentFacing);

                    // Check location validity
                    boolean isSpaceValid = canPlaceBuilding(gridX, gridY, newBuilding);

                    // Check if the building can be afforded
                    boolean canAfford = GameState.instance.canAfford(selectedBuilding.def.cost);

                    if (isSpaceValid && canAfford) {
                        GameState.instance.payCost(selectedBuilding.def.cost);

                        newBuilding.facing = currentFacing;
                        buildings.add(newBuilding);
                        powerSystem.rebuildNetworks(buildings);

                        for (int i = 0; i < newBuilding.width; i++) {
                            for (int j = 0; j < newBuilding.height; j++) {
                                map[gridX + i][gridY + j].building = newBuilding;
                            }
                        }

                        newBuilding.onPlaced(map);
                        System.out.println("Placed " + selectedBuilding.name());
                    }
                }
            }
        }

        // Remove a building upon right-clicking
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            if (hoveredTile != null && hoveredTile.building != null) {
                Building b = hoveredTile.building;
                buildings.remove(b);
                powerSystem.rebuildNetworks(buildings);

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

        powerSystem.update();
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

    private void updateUI() {
        // Update Resources
        hudString.setLength(0); // Clear the buffer
        hudString.append("[RESOURCES]\n");

        if (GameState.instance != null) {
            for (Map.Entry<ItemType, Integer> entry : GameState.instance.resources.entrySet()) {
                // Currently showing all resources (even if 0)
                String name = formatEnumName(entry.getKey().name());
                hudString.append(name)
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
            }
        }
        resourceLabel.setText(hudString);

        // Update Selection
        String buildingName = formatEnumName(selectedBuilding.name());
        selectionLabel.setText("SELECTED: " + buildingName + "\nROTATION: " + currentFacing);
        hudString.setLength(0);
        hudString.append("SELECTED: ").append(formatEnumName(selectedBuilding.name())).append("\n");
        hudString.append("ROTATION: ").append(currentFacing).append("\n");
        hudString.append("COST:\n");

        if (selectedBuilding.def.cost.isEmpty()) {
            hudString.append(" Free");
        } else {
            for (Map.Entry<ItemType, Integer> entry : selectedBuilding.def.cost.entrySet()) {
                hudString.append(" - ")
                    .append(formatEnumName(entry.getKey().name()))
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
            }
        }
        selectionLabel.setText(hudString);

        uiStage.act();
    }

    // Helper to turn "COPPER_ORE" into "Copper Ore"
    private String formatEnumName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)))
                .append(word.substring(1))
                .append(" ");
        }
        return sb.toString().trim();
    }

    // Handles rendering
    private void draw() {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        batch.setProjectionMatrix(camera.combined);
        batch.begin(); // Start GPU Batch

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

        // Render visible tiles
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                drawGround(x, y);
            }
        }

        // Render Buildings
        for (Building b : buildings) {
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

            // Create temporary ghost building
            Building temp = selectedBuilding.create(hoveredTile.x, hoveredTile.y);

            if (temp != null) {
                // Set orientation
                temp.setFacing(currentFacing);

                // Check both conditions
                boolean isSpaceValid = canPlaceBuilding(hoveredTile.x, hoveredTile.y, temp);
                boolean canAfford = GameState.instance.canAfford(selectedBuilding.def.cost);

                Gdx.gl.glEnable(Gdx.gl.GL_BLEND);

                // Valid only if both conditions are met
                if (isSpaceValid && canAfford) {
                    batch.setColor(1f, 1f, 1f, 0.5f); // White
                } else {
                    batch.setColor(1f, 0f, 0f, 0.5f); // Red
                }

                int gx = hoveredTile.x * TILE_SIZE;
                int gy = hoveredTile.y * TILE_SIZE;

                // Draw over placement area
                batch.draw(whitePixel, gx, gy, temp.width * TILE_SIZE, temp.height * TILE_SIZE);

                // Draw direction indicator
                batch.setColor(1f, 1f, 0f, 0.5f);

                // Calculate center based on building size
                float centerX = gx + (temp.width * TILE_SIZE) / 2f;
                float centerY = gy + (temp.height * TILE_SIZE) / 2f;

                // Offset dot based on current rotation
                float dotX = centerX + (currentFacing.dx * temp.width * TILE_SIZE / 2f) - 3;
                float dotY = centerY + (currentFacing.dy * temp.height * TILE_SIZE / 2f) - 3;

                batch.draw(whitePixel, dotX, dotY, 6, 6);
            }
        }

        // Highlight tile
        if (hoveredTile != null) {
            batch.setColor(HIGHLIGHT_COLOR);
            batch.draw(whitePixel, hoveredTile.x * TILE_SIZE, hoveredTile.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // --- DRAW POWER LINES ---
        // Iterate through all active power networks
        if (powerSystem != null) {
            for (PowerNetwork net : powerSystem.getNetworks()) {

                // Compare every building in the network to every other building
                // (i + 1 optimization prevents drawing the same line twice)
                for (int i = 0; i < net.members.size(); i++) {
                    Building a = net.members.get(i);

                    for (int j = i + 1; j < net.members.size(); j++) {
                        Building b = net.members.get(j);

                        // Check if they are close enough to physically connect
                        float range = Math.max(a.getConnectionRadius(), b.getConnectionRadius());
                        float dst = Vector2.dst(a.x, a.y, b.x, b.y);

                        // If they are within range (and not the same building)
                        if (dst <= range) {
                            // Calculate centers (in pixels)
                            float x1 = (a.x * TILE_SIZE) + (a.width * TILE_SIZE) / 2f;
                            float y1 = (a.y * TILE_SIZE) + (a.height * TILE_SIZE) / 2f;
                            float x2 = (b.x * TILE_SIZE) + (b.width * TILE_SIZE) / 2f;
                            float y2 = (b.y * TILE_SIZE) + (b.height * TILE_SIZE) / 2f;

                            // Draw the beam
                            drawLine(x1, y1, x2, y2, 2f, POWER_LINE_COLOR); // 2f thickness
                        }
                    }
                }
            }
        }

        batch.end();
    }

    private void drawLine(float x1, float y1, float x2, float y2, float thickness, Color color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;

        batch.setColor(color);
        // We draw the whitePixel:
        // - At x1, y1
        // - With Origin at 0, thickness/2 (so the line is centered on the start point)
        // - Width = dist (length of line)
        // - Height = thickness
        // - Rotated by 'angle'
        batch.draw(whitePixel, x1, y1, 0, thickness/2, dist, thickness, 1, 1, angle, 0, 0, 1, 1, false, false);
    }

    private void drawGround(int x, int y) {
        Tile tile = map[x][y];

        // Base Terrain layer
        if ((x + y) % 2 == 0) { // Checkerboard
            batch.setColor(tile.terrain.color1);
        } else {
            batch.setColor(tile.terrain.color2);
        }

        batch.draw(whitePixel, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Resource Layer
        if (tile.resource != null) {
            batch.setColor(tile.resource.color);
            float margin = 4;
            batch.draw(whitePixel, (x * TILE_SIZE) + margin, (y * TILE_SIZE) + margin, TILE_SIZE - (margin * 2), TILE_SIZE - (margin * 2));
        }
    }

    private void drawBuilding(Building b) {
        // --- DRAW BUILDING ---
        if (b instanceof Drill) {
            batch.setColor(DRILL_COLOR);
        } else if (b instanceof Conveyor) {
            batch.setColor(Color.DARK_GRAY);
        } else if (b instanceof Factory) {
            batch.setColor(FACTORY_COLOR);
        } else if (b instanceof Core) {
            batch.setColor(CORE_COLOR);
        } else if (b instanceof PowerPole) {
            batch.setColor(POWER_POLE_COLOR);
        } else {
            batch.setColor(Color.WHITE);
        }

        // Draw the main box using width/height
        batch.draw(whitePixel,
            (b.x * TILE_SIZE) + 2,
            (b.y * TILE_SIZE) + 2,
            (b.width * TILE_SIZE) - 4,
            (b.height * TILE_SIZE) - 4);


        // --- DRAW DIRECTION INDICATOR (Yellow Dot) ---
        batch.setColor(Color.YELLOW);
        float centerX = (b.x * TILE_SIZE) + (b.width * TILE_SIZE) / 2f;
        float centerY = (b.y * TILE_SIZE) + (b.height * TILE_SIZE) / 2f;

        // Offset the dot based on facing direction (dx/dy)
        float dotX = centerX + (b.facing.dx * 10) - 3;
        float dotY = centerY + (b.facing.dy * 10) - 3;
        batch.draw(whitePixel, dotX, dotY, 6, 6);

        // --- DRAW ITEMS ---
        ItemType item = b.getFirstItem();
        if (item != null) {
            batch.setColor(item.color);
            // Draw item slightly smaller in center
            batch.draw(whitePixel, centerX - 4, centerY - 4, 8, 8);
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
        batch.dispose();
        whitePixel.dispose();
        if(uiStage != null) uiStage.dispose();    }
}
