A grid-based logistics and management simulation engine written in Java using the LibGDX framework. This project focuses on efficient rendering of large-scale tile maps, simulation logic, and object-oriented entity design.

Uses Java Runtime Environment (JRE) 17 or higher.

To run the simulation, download the test release from the [Releases](link_to_releases) page and run the JAR file.


Controls:

WASD: Move camera

Scroll: Zoom In/Out

R: Rotate Selection

Left Click: Place Selected Structure

Right Click: Remove Structure

F11: Toggle full-screen



The core logic is located in `core/src/main/java/com/ksu1012/factory`:

`Main.java`: Entry point handling the render loop, input processing, and UI updates.

`WorldGenerator.java`: Contains noise algorithms and terrain generation logic.

`Building.java`: Abstract base class defining inventory management and item transport logic.

`BuildingType.java`: Enum definition acting as a factory pattern for entity creation.
