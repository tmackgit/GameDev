package com.brackeen.javagamebook.scripting;

import java.util.EventListener;
import com.brackeen.javagamebook.game.GameObject;

/**
    Interface to receive GameObject notification events.
    See GameObject.addListener().
*/
public interface GameObjectEventListener extends EventListener {

    /**
        Notifies this GameObject whether it was visible or not
        on the last update.
    */
    public void notifyVisible(GameObject object, boolean visible);


    /**
        Notifies this GameObject that when it moved, it collided
        with the specified, other object.
    */
    public void notifyObjectCollision(GameObject object,
        GameObject otherObject);


    /**
        Notifies this GameObject that it is touching the specified
        object. This method is not called again if the two objects
        were touching in the last frame.
    */
    public void notifyObjectTouch(GameObject object,
        GameObject otherObject);


    /**
        Notifies this GameObject that it is no longer touching the
        specified object.
    */
    public void notifyObjectRelease(GameObject object,
        GameObject otherObject);


    /**
        Notifies this GameObject that when it moved, it collided
        with a floor.
    */
    public void notifyFloorCollision(GameObject object);


    /**
        Notifies this GameObject that when it moved, it collided
        with a ceiling.
    */
    public void notifyCeilingCollision(GameObject object);


    /**
        Notifies this GameObject that when it moved, it collided
        with a wall.
    */
    public void notifyWallCollision(GameObject object);



}