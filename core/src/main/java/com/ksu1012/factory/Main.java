package com.ksu1012.factory; // <--- KEEP YOUR PACKAGE NAME

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    // --- GAME SETTINGS ---
    private final int TILE_SIZE = 32;
    private final int MAP_WIDTH = 50;  // Made map bigger to test movement
    private final int MAP_HEIGHT = 50;

    // --- VISUAL SETTINGS ---
    private final Color GROUND_COLOR_1 = new Color(0.15f, 0.15f, 0.15f, 1f);
    private final Color GROUND_COLOR_2 = new Color(0.18f, 0.18f, 0.18f, 1f);

    // --- PHYSICS SETTINGS ---
    private final float ACCELERATION = 5000f; // px / s^2
    private final float MAX_SPEED = 500f; // px / s
    private final float FRICTION = 0.93f; // Friction coefficient -- smaller = more friction

    // --- PHYSICS STATE ---
    private Vector2 velocity = new Vector2(0, 0);
    private Vector2 inputVector = new Vector2(0, 0);

    @Override
    public void create() {
        // Setup Camera
        camera = new OrthographicCamera();

        // Center the camera on the map initially
        camera.position.set(MAP_WIDTH * TILE_SIZE / 2f, MAP_HEIGHT * TILE_SIZE / 2f, 0);

        shapeRenderer = new ShapeRenderer();

        // Mouse inputs
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom += camera.zoom * amountY * 0.1f;

                // Zoom limits
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

        // --- PHYSICS LOGIC ---
        handleInput(deltaTime);

        // Apply velocity to camera position
        // We multiply by deltaTime to make movement smooth regardless of FPS
        camera.position.x += velocity.x * deltaTime;
        camera.position.y += velocity.y * deltaTime;

        // Update camera matrices
        camera.update();

        // --- RENDER LOGIC ---
        // Clear screen with a dark color (matching our ground)
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw Map
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                // Use the subtle colors defined at the top
                if ((x + y) % 2 == 0) {
                    shapeRenderer.setColor(GROUND_COLOR_1);
                } else {
                    shapeRenderer.setColor(GROUND_COLOR_2);
                }
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw a dot in the center of the screen where the camera is looking
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(camera.position.x, camera.position.y, 5);

        shapeRenderer.end();
    }

    private void handleInput(float delta) {
        // Reset input vector
        inputVector.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) inputVector.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) inputVector.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) inputVector.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputVector.x += 1;

        // --- NORMALIZATION ---
        if (inputVector.len() > 0) {
            inputVector.nor();
        }

        // --- ACCELERATION ---
        if (inputVector.len() > 0) {
            velocity.x += inputVector.x * ACCELERATION * delta;
            velocity.y += inputVector.y * ACCELERATION * delta;
        }

        // --- FRICTION (Deceleration) ---
        velocity.scl(FRICTION);

        // Stop completely if very slow (prevents micro-sliding)
        if (velocity.len() < 10f) {
            velocity.set(0, 0);
        }

        // --- MAX SPEED CLAMP ---
        velocity.clamp(0, MAX_SPEED);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
