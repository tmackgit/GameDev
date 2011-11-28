package com.brackeen.javagamebook.scripting;

import java.util.*;
import com.brackeen.javagamebook.game.GameObject;

/**
    Adapter to multicast GameObject notifications to multiple
    listeners.
*/
public class GameObjectEventMulticaster
    implements GameObjectEventListener
{

    private List listeners;

    public GameObjectEventMulticaster(GameObjectEventListener l1,
        GameObjectEventListener l2)
    {
        listeners = new LinkedList();
        addListener(l1);
        addListener(l2);
    }

    public void addListener(GameObjectEventListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }


    public void removeListener(GameObjectEventListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }

    /**
        Notifies this GameObject whether it was visible or not
        on the last update.
    */
    public void notifyVisible(GameObject object, boolean visible) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            GameObjectEventListener l =
                (GameObjectEventListener)i.next();
            l.notifyVisible(object, visible);
        }
    }


    /**
        Notifies this GameObject that when it moved, it collided
        with the specified object.
    */
    public void notifyObjectCollision(GameObject object,
        GameObject otherObject)
    {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            GameObjectEventListener l =
                (GameObjectEventListener)i.next();
            l.notifyObjectCollision(object, otherObject);
        }
    }


    public void notifyObjectTouch(GameObject object,
        GameObject otherObject)
    {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            GameObjectEventListener l =
                (GameObjectEventListener)i.next();
            l.notifyObjectTouch(object, otherObject);
        }
    }


    public void notifyObjectRelease(GameObject object,
        GameObject otherObject)
    {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            GameObjectEventListener l =
                (GameObjectEventListener)i.next();
            l.notifyObjectRelease(object, otherObject);
        }
    }


    /**
        Notifies this GameObject that when it moved, it collided
        with a floor.
    */
    public void notifyFloorCollision(GameObject object) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            GameObjectEventListener l =
                (GameObjectEventListener)i.next();
            l.notifyFloorCollision(object);
        }
    }


    /**
        Notifies this GameObject that when it moved, it collided
        with a ceiling.
    */
    public void notifyCeilingCollision(GameObject object) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            GameObjectEventListener l =
                (GameObjectEventListener)i.next();
            l.notifyCeilingCollision(object);
        }
    }


    /**
        Notifies this GameObject that when it moved, it collided
        with a wall.
    */
    public void notifyWallCollision(GameObject object) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            GameObjectEventListener l =
                (GameObjectEventListener)i.next();
            l.notifyWallCollision(object);
        }
    }
}

