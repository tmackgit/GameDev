package com.brackeen.javagamebook.path;

import java.util.Iterator;
import com.brackeen.javagamebook.path.PathFinder;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.GameObject;

/**
    A PathBot is a GameObject that follows a path from a
    PathFinder.
*/
public class PathBot extends GameObject {

    private static final float DEFAULT_TURN_SPEED = .005f;
    private static final float DEFAULT_SPEED = .25f;
    private static final long DEFAULT_PATH_RECALC_TIME = 4000;
    private static final float DEFAULT_FLY_HEIGHT = 64;

    protected PathFinder pathFinder;
    protected Iterator currentPath;
    private Vector3D nextPathLocation;
    private long timeUntilPathRecalc;
    private long pathRecalcTime;
    private Vector3D facing;

    private float turnSpeed;
    private float speed;
    private float flyHeight;


    public PathBot(PolygonGroup polygonGroup) {
        super(polygonGroup);
        nextPathLocation = new Vector3D();

        // set default values
        setPathRecalcTime(DEFAULT_PATH_RECALC_TIME);
        setSpeed(DEFAULT_SPEED);
        setTurnSpeed(DEFAULT_TURN_SPEED);
        setFlyHeight(DEFAULT_FLY_HEIGHT);
        setState(STATE_ACTIVE);
    }


    /**
        Sets the location this object should face as it follows
        the path. This value can change. If null, the this object
        faces the direction it is moving.
    */
    public void setFacing(Vector3D facing) {
        this.facing = facing;
    }


    /**
        Sets the PathFinder class to use to follow the path.
    */
    public void setPathFinder(PathFinder pathFinder) {
        if (this.pathFinder != pathFinder) {
            this.pathFinder = pathFinder;
            currentPath = null;

            // random amount of time until calulation, so
            // not all bot calc the path at the same time
            timeUntilPathRecalc = (long)(Math.random() * 1000);
        }
    }

    public void setPathRecalcTime(long pathRecalcTime) {
        this.pathRecalcTime = pathRecalcTime;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setTurnSpeed(float turnSpeed) {
        this.turnSpeed = turnSpeed;
    }

    public void setFlyHeight(float flyHeight) {
        getTransform().getLocation().y+=flyHeight - this.flyHeight;
        this.flyHeight = flyHeight;
    }

    public float getFlyHeight() {
        return flyHeight;
    }

    public void update(GameObject player, long elapsedTime) {

        if (pathFinder == null) {
            super.update(player, elapsedTime);
            return;
        }

        timeUntilPathRecalc-=elapsedTime;

        // updtate the path to the player
        if (timeUntilPathRecalc <= 0) {
            currentPath = pathFinder.find(this, player);
            if (currentPath != null) {
                getTransform().stop();
            }
            timeUntilPathRecalc = pathRecalcTime;
        }

        // follow the path
        if (currentPath != null && currentPath.hasNext() &&
            !getTransform().isMovingIgnoreY())
        {
            nextPathLocation.setTo((Vector3D)currentPath.next());
            nextPathLocation.y+=flyHeight;
            getTransform().moveTo(nextPathLocation, speed);

            Vector3D faceLocation = facing;
            if (faceLocation == null) {
                faceLocation = nextPathLocation;
            }
            getTransform().turnYTo(
                faceLocation.x - getX(),
                faceLocation.z - getZ(),
                (float)-Math.PI/2, turnSpeed);

        }

        super.update(player, elapsedTime);
    }


    /**
        When a collision occurs, back up for 200 ms and then
        wait a few seconds before recaculating the path.
    */
    protected void backupAndRecomputePath() {
        // back up for 200 ms
        nextPathLocation.setTo(getTransform().getVelocity());
        if (!isFlying()) {
            nextPathLocation.y = 0;
        }
        nextPathLocation.multiply(-1);
        getTransform().setVelocity(nextPathLocation, 200);

        // wait until computing the path again
        currentPath = null;
        timeUntilPathRecalc = (long)(Math.random() * 1000);
    }

    public boolean isFlying() {
        return (flyHeight > 0);
    }

    public void notifyWallCollision() {
        backupAndRecomputePath();
    }

    public void notifyObjectCollision(GameObject object) {
        backupAndRecomputePath();
    }

}
