# Factory Automation Simulation

A grid-based logistics and management simulation engine written in Java using the LibGDX framework. This project focuses on efficient rendering of large-scale tile maps, decoupled simulation logic, and object-oriented entity design.

## Technical Overview

This project implements a custom engine to handle the specific requirements of factory simulation games, specifically high entity counts and deterministic logic.

**Technologies:** Java 17, LibGDX, Gradle, OpenGL (LWJGL3).

### Key Implementations

*   **Render Pipeline Optimization:**
    *   Implements Frustum Culling to restrict rendering to the camera viewport, decoupling render time from total map size.
    *   Utilizes SpriteBatching to minimize OpenGL draw calls.
    *   Supports map sizes exceeding 5000x5000 tiles while maintaining high frame rates.

*   **Procedural Generation:**
    *   Terrain is generated via layered Perlin Noise to create distinct height and moisture maps.
    *   Applies Cellular Automata smoothing passes to eliminate noise artifacts.
    *   Resources generation uses a random walker algorithm to create organic vein structures rather than uniform clusters.

*   **Entity Architecture:**
    *   Buildings are defined using an Enum-based data structure containing functional interfaces for dynamic instantiation.
    *   Logic allows for multi-tile structures with specific input/output directionality.
    *   Simulation logic (tick updates) is separated from the rendering loop.

## Installation and Usage

### Prerequisites
*   Java Runtime Environment (JRE) 17 or higher.

### Running the Simulation
1.  Download the latest JAR file from the [Releases](link_to_releases) page.
2.  Run the application via command line or double-click:
    ```bash
    java -jar FactoryGame-1.0.0.jar
    ```

### Controls
*   **WASD:** Pan Camera
*   **Scroll:** Zoom In/Out
*   **1-5:** Select Building Type
*   **R:** Rotate Selection
*   **Left Click:** Place Structure
*   **Right Click:** Remove Structure

## Project Structure

The core logic is located in `core/src/main/java/com/ksu1012/factory`:

*   `Main.java`: Entry point handling the render loop, input processing, and UI updates.
*   `WorldGenerator.java`: Contains noise algorithms and terrain generation logic.
*   `Building.java`: Abstract base class defining inventory management and item transport logic.
*   `BuildingType.java`: Enum definition acting as a factory pattern for entity creation.
