package com.brackeen.javagamebook.game;

import com.brackeen.javagamebook.math3D.*;

/**
    A GameObject class is a base class for any type of object in a
    game that is represented by a PolygonGroup. For example, a
    GameObject can be a static object (like a crate), a moving
    object (like a projectile or a bad guy), or any other type of
    object (like a power-ups). GameObjects have three basic
    states: STATE_IDLE, STATE_ACTIVE, or STATE_DESTROYED.
*/
public class GameObject {

    /**
        Represents a GameObject that is idle. If the object is
        idle, it's Transform3D is not updated. By default,
        GameObjects are initially idle and are changed to the
        active state when they are initially visible. This
        behavior can be changed by overriding the notifyVisible()
        method.
    */
    protected static final int STATE_IDLE = 0;

    /**
        Represents a GameObject that is active.
        should no longer be updated or drawn.
        Once in the STATE_DESTROYED state, the GameObjectManager
        should remove this object from the list of GameObject
        it manages.
    */
    protected static final int STATE_ACTIVE = 1;

    /**
        Represents a GameObject that has been destroyed, and
        should no longer be updated or drawn.
        Once in the STATE_DESTROYED state, the GameObjectManager
        should remove this object from the list of GameObject
        it manages.
    */
    protected static final int STATE_DESTROYED = 2;

    private PolygonGroup polygonGroup;
    private int state;

    /**
        Creates a new GameObject represented by the specified
        PolygonGroup. The PolygonGroup can be null.
    */
    public GameObject(PolygonGroup polygonGroup) {
        this.polygonGroup = polygonGroup;
        state = STATE_IDLE;
    }


    /**
        Shortcut to get the location of this GameObject from the
        Transform3D.
    */
    public Vector3D getLocation() {
        return polygonGroup.getTransform().getLocation();
    }


    /**
        Gets this object's transform.
    */
    public MovingTransform3D getTransform() {
        return polygonGroup.getTransform();
    }


    /**
        Gets this object's PolygonGroup.
    */
    public PolygonGroup getPolygonGroup() {
        return polygonGroup;
    }


    /**
        Shortcut to get the X location of this GameObject.
    */
    public float getX() {
        return getLocation().x;
    }


    /**
        Shortcut to get the Y location of this GameObject.
    */
    public float getY() {
        return getLocation().y;
    }


    /**
        Shortcut to get the Z location of this GameObject.
    */
    public float getZ() {
        return getLocation().z;
    }


    /**
        Sets the state of this object. Should be either
        STATE_IDLE, STATE_ACTIVE, or STATE_DESTROYED.
    */
    protected void setState(int state) {
        this.state = state;
    }


    /**
        Sets the state of the specified object. This allows
        any GameObject to set the state of any other GameObject.
        The state should be either STATE_IDLE, STATE_ACTIVE, or
        STATE_DESTROYED.
    */
    protected void setState(GameObject object, int state) {
        object.setState(state);
    }


    /**
        Returns true if this GameObject is idle.
    */
    public boolean isIdle() {
        return (state == STATE_IDLE);
    }


    /**
        Returns true if this GameObject is active.
    */
    public boolean isActive() {
        return (state == STATE_ACTIVE);
    }


    /**
        Returns true if this GameObject is destroyed.
    */
    public boolean isDestroyed() {
        return (state == STATE_DESTROYED);
    }


    /**
        If this GameObject is in the active state, this method
        updates it's PolygonGroup. Otherwise, this method does
        nothing.
    */
    public void update(GameObject player, long elapsedTime) {
        if (isActive()) {
            polygonGroup.update(elapsedTime);
        }
    }


    /**
        Notifies this GameObject whether it was visible or not
        on the last update. By default, if this GameObject is
        idle and notified as visible, it changes to the active
        state.
    */
    public void notifyVisible(boolean visible) {
        if (visible && isIdle()) {
            state = STATE_ACTIVE;
        }
    }

}
