package com.brackeen.javagamebook.game;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.util.Iterator;
import com.brackeen.javagamebook.game.GameObjectRenderer;

/**
    The GameObjectManager interface provides methods to keep
    track of and draw GameObjects.
*/
public interface GameObjectManager {


    /**
        Marks all objects within the specified 2D bounds
        as potentially visible (should be drawn).
    */
    public void markVisible(Rectangle bounds);


    /**
        Marks all objects as potentially visible (should be drawn).
    */
    public void markAllVisible();


    /**
        Adds a GameObject to this manager.
    */
    public void add(GameObject object);


    /**
        Gets an iterator of all GameObjects (including the player).
    */
    public Iterator iterator();


    /**
        Adds a GameObject to this manager, specifying it as the
        player object. An existing player object, if any,
        is not removed.
    */
    public void addPlayer(GameObject player);


    /**
        Gets the object specified as the Player object, or null
        if no player object was specified.
    */
    public GameObject getPlayer();


    /**
        Removes a GameObject from this manager.
    */
    public void remove(GameObject object);


    /**
        Updates all objects based on the amount of time passed
        from the last update.
    */
    public void update(long elapsedTime);


    /**
        Draws all visible objects.
    */
    public void draw(Graphics2D g, GameObjectRenderer r);

}
