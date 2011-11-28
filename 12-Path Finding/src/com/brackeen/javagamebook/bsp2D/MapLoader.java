package com.brackeen.javagamebook.bsp2D;

import java.io.*;
import java.util.*;
import com.brackeen.javagamebook.math3D.*;

/**
    The MapLoader class loads maps from a text file based on
    the Alias|Wavefront OBJ file specification.

    MAP file commands:
    <pre>
    v [x] [y] [z]        - Define a vertex with floating-point
                           coords (x,y,z).
    mtllib [filename]    - Load materials from an external .mtl
                           file.
    usemtl [name]        - Use the named material (loaded from a
                           .mtl file) for the next floor, ceiling,
                           or wall.
    ambientLightIntensity
        [value]          - Defines the ambient light intensity
                           for the next room, from 0 to 1.
    pointlight [v]       - Defines a point light located at the
        [intensity]        specfied vector. Optionally, light
        [falloff]          intesity and falloff distance can
                           be specified.
    player [v] [angle]   - Specifies the starting location of the
                           player and optionally a starting
                           angle, in radians, around the y-axis.
    obj [uniqueName]     - Defines an object from an external
        [filename] [v]     OBJ file. The unique name allows this
        [angle]            object to be uniquely identfied, but
                           can be "null" if no unique name is
                           needed. The filename is an external
                           OBJ file. Optionally, the starting
                           angle, in radians, around the y-axis
                           can be specified.
    room [name]          - Defines a new room, optionally giving
                           the room a name. A room consists of
                           vertical walls, a horizontal floor
                           and a horizontal ceiling. Concave rooms
                           are currently not supported, but can be
                           simulated by adjacent convex rooms.
    floor [height]       - Defines the height of the floor of
                           the current room, using the current
                           material. The current material can
                           be null, in which case no floor
                           polygon is created. The floor can be
                           above the ceiling, in which case a
                           "pillar" or "block" structure is
                           created, rather than a "room".
    ceil [height]        - Defines the height of the ceiling of
                           the current room, using the current
                           material. The current material can
                           be null, in which case no ceiling
                           polygon is created. The ceiling can be
                           below the floor, in which case a
                           "pillar" or "block" structure is
                           created, rather than a "room".
    wall [x] [z]         - Defines a wall vertex in a room using
         [bottom] [top]    the specified x and z coordinates.
                           Walls should be defined in clockwise
                           order. If "bottom" and "top" is not
                           defined, the floor and ceiling height
                           are used. If the current material is
                           null, or bottom is equal to top, no
                           wall polygon is created.
    </pre>
*/
public class MapLoader extends ObjectLoader {

    private BSPTreeBuilder builder;
    private Map loadedObjects;
    private Transform3D playerStart;
    private RoomDef currentRoom;
    private List rooms;
    private List mapObjects;

    // use a separate ObjectLoader for objects
    private ObjectLoader objectLoader;


    /**
        Creates a new MapLoader using the default BSPTreeBuilder.
    */
    public MapLoader() {
        this(null);
    }


    /**
        Creates a new MapLoader using the specified BSPTreeBuilder.
        If the builder is null, a default BSPTreeBuilder
        is created.
    */
    public MapLoader(BSPTreeBuilder builder) {
        if (builder == null) {
            this.builder = new BSPTreeBuilder();
        }
        else {
            this.builder = builder;
        }
        parsers.put("map", new MapLineParser());
        objectLoader = new ObjectLoader();
        loadedObjects = new HashMap();
        rooms = new ArrayList();
        mapObjects = new ArrayList();
    }


    /**
        Loads a map file and creates a BSP tree. Objects
        created can be retrieved from the getObjectsInMap()
        method.
    */
    public BSPTree loadMap(String filename) throws IOException {
        currentRoom = null;
        rooms.clear();
        vertices.clear();
        mapObjects.clear();
        playerStart = new Transform3D();

        path = new File(filename).getParentFile();

        parseFile(filename);

        return createBSPTree();
    }


    /**
        Creates a BSP tree from the rooms defined in the map file.
    */
    protected BSPTree createBSPTree() {
        // extract all polygons
        List allPolygons = new ArrayList();
        for (int i=0; i<rooms.size(); i++) {
            RoomDef room = (RoomDef)rooms.get(i);
            allPolygons.addAll(room.createPolygons());
        }

        // build the tree
        BSPTree tree = builder.build(allPolygons);

        // create polygon surfaces based on the lights.
        tree.createSurfaces(lights);
        return tree;
    }


    /**
        Gets a list of all objects degined in the map file.
    */
    public List getObjectsInMap() {
        return mapObjects;
    }


    /**
        Gets the player start location defined in the map file.
    */
    public Transform3D getPlayerStartLocation() {
        return playerStart;
    }


    /**
        Sets the lights used for OBJ objects.
    */
    public void setObjectLights(List lights,
        float ambientLightIntensity)
    {
        objectLoader.setLights(lights, ambientLightIntensity);
    }


    /**
        Parses a line in a MAP file.
    */
    protected class MapLineParser implements LineParser {

        public void parseLine(String line) throws IOException,
            NoSuchElementException
        {
            StringTokenizer tokenizer = new StringTokenizer(line);
            String command = tokenizer.nextToken();

            if (command.equals("v")) {
                // create a new vertex
                vertices.add(new Vector3D(
                    Float.parseFloat(tokenizer.nextToken()),
                    Float.parseFloat(tokenizer.nextToken()),
                    Float.parseFloat(tokenizer.nextToken())));
            }
            else if (command.equals("mtllib")) {
                // load materials from file
                String name = tokenizer.nextToken();
                parseFile(name);
            }
            else if (command.equals("usemtl")) {
                // define the current material
                String name = tokenizer.nextToken();
                if ("null".equals(name)) {
                    currentMaterial = new Material();
                }
                else {
                    currentMaterial =
                        (Material)materials.get(name);
                    if (currentMaterial == null) {
                        currentMaterial = new Material();
                        System.out.println("no material: " + name);
                    }
                }
            }
            else if (command.equals("pointlight")) {
                // create a point light
                Vector3D loc = getVector(tokenizer.nextToken());
                float intensity = 1;
                float falloff = PointLight3D.NO_DISTANCE_FALLOFF;
                if (tokenizer.hasMoreTokens()) {
                    intensity =
                        Float.parseFloat(tokenizer.nextToken());
                }
                if (tokenizer.hasMoreTokens()) {
                    falloff =
                        Float.parseFloat(tokenizer.nextToken());
                }
                lights.add(new PointLight3D(loc.x, loc.y, loc.z,
                    intensity, falloff));
            }
            else if (command.equals("ambientLightIntensity")) {
                // define the ambient light intensity
                ambientLightIntensity =
                    Float.parseFloat(tokenizer.nextToken());
            }
            else if (command.equals("player")) {
                // define the player start location
                playerStart.getLocation().setTo(
                    getVector(tokenizer.nextToken()));
                if (tokenizer.hasMoreTokens()) {
                    playerStart.setAngleY(
                        Float.parseFloat(tokenizer.nextToken()));
                }
            }
            else if (command.equals("obj")) {
                // create a new obj from an object file
                String uniqueName = tokenizer.nextToken();
                String filename = tokenizer.nextToken();
                // check if the object is already loaded
                PolygonGroup object =
                    (PolygonGroup)loadedObjects.get(filename);
                if (object == null) {
                    File file = new File(path, filename);
                    String filePath = file.getPath();
                    object = objectLoader.loadObject(filePath);
                    loadedObjects.put(filename, object);
                }
                Vector3D loc = getVector(tokenizer.nextToken());
                PolygonGroup mapObject =
                    (PolygonGroup)object.clone();
                mapObject.getTransform().getLocation().setTo(loc);
                if (!uniqueName.equals("null")) {
                    mapObject.setName(uniqueName);
                }
                if (tokenizer.hasMoreTokens()) {
                    mapObject.getTransform().setAngleY(
                        Float.parseFloat(tokenizer.nextToken()));
                }
                mapObjects.add(mapObject);
            }
            else if (command.equals("room")) {
                // start a new room
                currentRoom = new RoomDef(ambientLightIntensity);
                rooms.add(currentRoom);
            }
            else if (command.equals("floor")) {
                // define a room's floor
                float y = Float.parseFloat(tokenizer.nextToken());
                currentRoom.setFloor(y, currentMaterial.texture);
            }
            else if (command.equals("ceil")) {
                // define a room's ceiling
                float y = Float.parseFloat(tokenizer.nextToken());
                currentRoom.setCeil(y, currentMaterial.texture);
            }
            else if (command.equals("wall")) {
                // define a wall vertex in a room.
                float x = Float.parseFloat(tokenizer.nextToken());
                float z = Float.parseFloat(tokenizer.nextToken());
                if (tokenizer.hasMoreTokens()) {
                    float bottom =
                        Float.parseFloat(tokenizer.nextToken());
                    float top =
                        Float.parseFloat(tokenizer.nextToken());
                    currentRoom.addVertex(x, z, bottom, top,
                        currentMaterial.texture);
                }
                else {
                    currentRoom.addVertex(x, z,
                        currentMaterial.texture);
                }
            }
            else {
                System.out.println("Unknown command: " + command);
            }
        }
    }
}