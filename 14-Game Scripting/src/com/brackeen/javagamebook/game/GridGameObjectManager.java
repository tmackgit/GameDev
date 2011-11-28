package com.brackeen.javagamebook.game;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.util.*;
import com.brackeen.javagamebook.math3D.*;

/**
    The GridGameObjectManager is a GameObjectManager that
    integrally arranges GameObjects on a 2D grid for visibility
    determination and to limit the number of tests for
    collision detection.
*/
public class GridGameObjectManager implements GameObjectManager {

    /**
        Default grid size of 512. The grid size should be larger
        than the largest object's diameter.
    */
    private static final int GRID_SIZE_BITS = 9;
    private static final int GRID_SIZE = 1 << GRID_SIZE_BITS;

    /**
        The Cell class represents a cell in the grid. It contains
        a list of game objects and a visible flag.
    */
    private static class Cell {
        List objects;
        boolean visible;

        Cell() {
            objects = new ArrayList();
            visible = false;
        }
    }

    private Cell[] grid;
    private Rectangle mapBounds;
    private int gridWidth;
    private int gridHeight;
    private List allObjects;
    private List spawnedObjects;
    private GameObject player;
    private Vector3D oldLocation;
    private CollisionDetection collisionDetection;

    /**
        Creates a new GridGameObjectManager with the specified
        map bounds and collision detection handler. GameObjects
        outside the map bounds will never be shown.
    */
    public GridGameObjectManager(Rectangle mapBounds,
        CollisionDetection collisionDetection)
    {
        this.mapBounds = mapBounds;
        this.collisionDetection = collisionDetection;
        gridWidth = (mapBounds.width >> GRID_SIZE_BITS) + 1;
        gridHeight = (mapBounds.height >> GRID_SIZE_BITS) + 1;
        grid = new Cell[gridWidth*gridHeight];
        for (int i=0; i<grid.length; i++) {
            grid[i] = new Cell();
        }
        allObjects = new ArrayList();
        spawnedObjects = new ArrayList();
        oldLocation = new Vector3D();
    }


    /**
        Converts a map x-coordinate to a grid x-coordinate.
    */
    private int convertMapXtoGridX(int x) {
        return (x - mapBounds.x) >> GRID_SIZE_BITS;
    }


    /**
        Converts a map y-coordinate to a grid y-coordinate.
    */
    private int convertMapYtoGridY(int y) {
        return (y - mapBounds.y) >> GRID_SIZE_BITS;
    }


    /**
        Marks all objects as potentially visible (should be drawn).
    */
    public void markAllVisible() {
        for (int i=0; i<grid.length; i++) {
            grid[i].visible = true;
        }
    }


    /**
        Marks all objects within the specified 2D bounds
        as potentially visible (should be drawn).
    */
    public void markVisible(Rectangle bounds) {
        int x1 = Math.max(0, convertMapXtoGridX(bounds.x));
        int y1 = Math.max(0, convertMapYtoGridY(bounds.y));
        int x2 = Math.min(gridWidth-1,
            convertMapXtoGridX(bounds.x + bounds.width));
        int y2 = Math.min(gridHeight-1,
            convertMapYtoGridY(bounds.y + bounds.height));

        for (int y=y1; y<=y2; y++) {
            int offset = y * gridWidth;
            for (int x=x1; x<=x2; x++) {
                grid[offset+x].visible = true;
            }
        }
    }


    public Iterator iterator() {
        return allObjects.iterator();
    }


    /**
        Adds a GameObject to this manager.
    */
    public void add(GameObject object) {
        if (object != null) {
            if (object == player) {
                // ensure player always moves first
                allObjects.add(0, object);
            }
            else {
                allObjects.add(object);
            }
            Cell cell = getCell(object);
            if (cell != null) {
                cell.objects.add(object);
            }

        }
    }


    /**
        Removes a GameObject from this manager.
    */
    public void remove(GameObject object) {
        if (object != null) {
            allObjects.remove(object);
            Cell cell = getCell(object);
            if (cell != null) {
                cell.objects.remove(object);
            }
        }
    }


    /**
        Adds a GameObject to this manager, specifying it as the
        player object. An existing player object, if any,
        is not removed.
    */
    public void addPlayer(GameObject player) {
        this.player = player;
        if (player != null) {
            player.getListener().notifyVisible(player, true);
            add(player);
        }
    }


    /**
        Gets the object specified as the Player object, or null
        if no player object was specified.
    */
    public GameObject getPlayer() {
        return player;
    }


    /**
        Gets the cell the specified GameObject is in, or null if
        the GameObject is not within the map bounds.
    */
    private Cell getCell(GameObject object) {
        int x = convertMapXtoGridX((int)object.getX());
        int y = convertMapYtoGridY((int)object.getZ());
        return getCell(x, y);
    }


    /**
        Gets the cell of the specified grid location, or null if
        the grid location is invalid.
    */
    private Cell getCell(int x, int y) {

        // check bounds
        if (x < 0 || y < 0 || x >= gridWidth || y >= gridHeight) {
            return null;
        }

        // get the cell at the x,y location
        return grid[x + y * gridWidth];
    }


    /**
        Updates all objects based on the amount of time passed
        from the last update and applied collision detection.
    */
    public void update(long elapsedTime) {
        for (int i=0; i<allObjects.size(); i++) {
            GameObject object = (GameObject)allObjects.get(i);

            // save the object's old position
            Cell oldCell = getCell(object);
            oldLocation.setTo(object.getLocation());
            boolean isRegenerating = false;

            // move the object
            object.update(player, elapsedTime);

            // keep track of any spawned objects (add later)
            List spawns = object.getSpawns();
            if (spawns != null) {
                if (spawns.contains(object)) {
                    isRegenerating = true;
                }
                spawnedObjects.addAll(spawns);
            }

            // remove the object if destroyed
            if (object.isDestroyed() || isRegenerating) {
                allObjects.remove(i);
                i--;
                if (oldCell != null) {
                    oldCell.objects.remove(object);
                }
                continue;
            }

            // if the object moved, do collision detection
            if (!object.getLocation().equals(oldLocation) ||
                object.isJumping())
            {

                // check walls, floors, and ceilings
                collisionDetection.checkBSP(object,
                    oldLocation, elapsedTime);

                // check other objects
                if (checkObjectCollision(object, oldLocation)) {
                    // revert to old position
                    object.getLocation().setTo(oldLocation);
                }

                // update grid location
                Cell cell = getCell(object);
                if (cell != oldCell) {
                    if (oldCell != null) {
                        oldCell.objects.remove(object);
                    }
                    if (cell != null) {
                        cell.objects.add(object);
                    }
                }

                // send touch (and release) notifications
                object.sendTouchNotifications();
            }


        }

        // add any spawned objects
        if (spawnedObjects.size() > 0) {
            for (int i=0; i<spawnedObjects.size(); i++) {
                add((GameObject)spawnedObjects.get(i));
            }
            spawnedObjects.clear();
        }
    }


    /**
        Checks to see if the specified object collides with any
        other object.
    */
    public boolean checkObjectCollision(GameObject object,
        Vector3D oldLocation)
    {

        boolean collision = false;

        // use the object's (x,z) position (ground plane)
        int x = convertMapXtoGridX((int)object.getX());
        int y = convertMapYtoGridY((int)object.getZ());

        // check the object's surrounding 9 cells
        for (int i=x-1; i<=x+1; i++) {
            for (int j=y-1; j<=y+1; j++) {
                Cell cell = getCell(i, j);
                if (cell != null) {
                    collision |= collisionDetection.checkObject(
                        object, cell.objects, oldLocation);
                }
            }
        }

        return collision;
    }


    /**
        Draws all visible objects and marks all objects as
        not visible.
    */
    public void draw(Graphics2D g, GameObjectRenderer r) {
        for (int i=0; i<grid.length; i++) {
            List objects = grid[i].objects;
            for (int j=0; j<objects.size(); j++) {
                GameObject object = (GameObject)objects.get(j);
                boolean visible = false;
                if (grid[i].visible) {
                    visible = r.draw(g, object);
                }
                if (object != player) {
                    // notify objects if they are visible
                    object.getListener().notifyVisible(object,
                        visible);
                }
            }
            grid[i].visible = false;
        }
    }
}
