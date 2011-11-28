package com.brackeen.javagamebook.game;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.util.*;
import com.brackeen.javagamebook.game.GameObjectRenderer;

/**
    The SimpleGameObjectManager is a GameObjectManager that
    keeps all object in a list and performs no collision
    detection.
*/
public class SimpleGameObjectManager implements GameObjectManager {

    private List allObjects;
    private List visibleObjects;
    private List spawnedObjects;
    private GameObject player;

    /**
        Creates a new SimpleGameObjectManager.
    */
    public SimpleGameObjectManager() {
        allObjects = new ArrayList();
        visibleObjects = new ArrayList();
        spawnedObjects = new ArrayList();
        player = null;
    }


    /**
        Marks all objects as potentially visible (should be drawn).
    */
    public void markAllVisible() {
        for (int i=0; i<allObjects.size(); i++) {
            GameObject object = (GameObject)allObjects.get(i);
            if (!visibleObjects.contains(object)) {
                visibleObjects.add(object);
            }
        }
    }


    /**
        Marks all objects within the specified 2D bounds
        as potentially visible (should be drawn).
    */
    public void markVisible(Rectangle bounds) {
        for (int i=0; i<allObjects.size(); i++) {
            GameObject object = (GameObject)allObjects.get(i);
            if (bounds.contains(object.getX(), object.getZ()) &&
                !visibleObjects.contains(object))
            {
                visibleObjects.add(object);
            }
        }
    }


    /**
        Adds a GameObject to this manager.
    */
    public void add(GameObject object) {
        if (object != null) {
            allObjects.add(object);
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
            player.notifyVisible(true);
            allObjects.add(0, player);
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
        Removes a GameObject from this manager.
    */
    public void remove(GameObject object) {
        allObjects.remove(object);
        visibleObjects.remove(object);
    }


    /**
        Updates all objects based on the amount of time passed
        from the last update.
    */
    public void update(long elapsedTime) {
        for (int i=0; i<allObjects.size(); i++) {
            GameObject object = (GameObject)allObjects.get(i);

            // move the object
            object.update(player, elapsedTime);

            // keep track of any spawned objects (add later)
            List spawns = object.getSpawns();
            if (spawns != null) {
                spawnedObjects.addAll(spawns);
            }

            // remove destroyed objects
            if (object.isDestroyed()) {
                allObjects.remove(i);
                visibleObjects.remove(object);
                i--;
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
        Draws all visible objects and marks all objects as
        not visible.
    */
    public void draw(Graphics2D g, GameObjectRenderer r) {
        Iterator i = visibleObjects.iterator();
        while (i.hasNext()) {
            GameObject object = (GameObject)i.next();
            boolean visible = r.draw(g, object);
            // notify objects if they are visible this frame
            object.notifyVisible(visible);
        }
        visibleObjects.clear();
    }
}
