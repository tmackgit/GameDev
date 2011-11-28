package com.brackeen.javagamebook.game;

import com.brackeen.javagamebook.bsp2D.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.GameObject;

/**
    The CollisionDetectionWithSliding class handles collision
    detection between the GameObjects, and between GameObjects and
    a BSP tree. When a collision occurs, the GameObject slides
    to the side rather than stops.
*/
public class CollisionDetectionWithSliding
    extends CollisionDetection
{

    private Vector3D scratch = new Vector3D();
    private Vector3D originalLocation = new Vector3D();

    /**
        Creates a new CollisionDetectionWithSliding object for the
        specified BSP tree.
    */
    public CollisionDetectionWithSliding(BSPTree bspTree) {
        super(bspTree);
    }


    /**
        Checks for a game object collision with the walls of the
        BSP tree. Returns the first wall collided with, or null if
        there was no collision. If there is a collision, the
        object slides along the wall and again checks for a
        collision. If a collision occurs on the slide, the object
        reverts back to its old location.
    */
    public BSPPolygon checkWalls(GameObject object,
        Vector3D oldLocation, long elapsedTime)
    {

        float goalX = object.getX();
        float goalZ = object.getZ();

        BSPPolygon wall = super.checkWalls(object,
            oldLocation, elapsedTime);
        // if collision found and object didn't stop itself
        if (wall != null && object.getTransform().isMoving()) {
            float actualX = object.getX();
            float actualZ = object.getZ();

            // dot product between wall's normal and line to goal
            scratch.setTo(actualX, 0, actualZ);
            scratch.subtract(goalX, 0, goalZ);
            float length = scratch.getDotProduct(wall.getNormal());

            float slideX = goalX + length * wall.getNormal().x;
            float slideZ = goalZ + length * wall.getNormal().z;

            object.getLocation().setTo(
                slideX, object.getY(), slideZ);
            originalLocation.setTo(oldLocation);
            oldLocation.setTo(actualX, oldLocation.y, actualZ);

            // use a smaller radius for sliding
            PolygonGroupBounds bounds = object.getBounds();
            float originalRadius = bounds.getRadius();
            bounds.setRadius(originalRadius-1);

            // check for collision with slide position
            BSPPolygon wall2 = super.checkWalls(object,
                oldLocation, elapsedTime);

            // restore changed parameters
            oldLocation.setTo(originalLocation);
            bounds.setRadius(originalRadius);

            if (wall2 != null) {
                object.getLocation().setTo(
                    actualX, object.getY(), actualZ);
                return wall2;
            }
        }

        return wall;
    }


    /**
        Checks for object collisions with the floor and ceiling.
        Uses object.getFloorHeight() and object.getCeilHeight()
        for the floor and ceiling values.
        Applies gravity if the object is above the floor,
        and scoots the object up if the player is below the floor
        (for smooth movement up stairs).
    */
    protected void checkFloorAndCeiling(GameObject object,
        long elapsedTime)
    {
        float floorHeight = object.getFloorHeight();
        float ceilHeight = object.getCeilHeight();
        float bottomHeight = object.getBounds().getBottomHeight();
        float topHeight = object.getBounds().getTopHeight();
        Vector3D v = object.getTransform().getVelocity();
        Physics physics = Physics.getInstance();

        // check if on floor
        if (object.getY() + bottomHeight == floorHeight) {
            if (v.y < 0) {
                v.y = 0;
            }
        }
        // check if below floor
        else if (object.getY() + bottomHeight < floorHeight) {

            if (!object.isFlying()) {
                // if falling
                if (v.y < 0) {
                    object.getListener().
                        notifyFloorCollision(object);
                    v.y = 0;
                    object.getLocation().y =
                        floorHeight - bottomHeight;
                }
                else if (!object.isJumping()) {
                    physics.scootUp(object, elapsedTime);
                }
            }
            else {
                object.getListener().notifyFloorCollision(object);
                v.y = 0;
                object.getLocation().y =
                    floorHeight - bottomHeight;
            }
        }
        // check if hitting ceiling
        else if (object.getY() + topHeight > ceilHeight) {
            object.getListener().notifyCeilingCollision(object);
            if (v.y > 0) {
                v.y = 0;
            }
            object.getLocation().y = ceilHeight - topHeight;
            if (!object.isFlying()) {
                physics.applyGravity(object, elapsedTime);
            }
        }
        // above floor
        else {
            if (!object.isFlying()) {
                // if scooting-up, stop the scoot
                if (v.y > 0 && !object.isJumping()) {
                    v.y = 0;
                    object.getLocation().y =
                        floorHeight - bottomHeight;
                }
                else {
                    physics.applyGravity(object, elapsedTime);
                }
            }
        }


    }


    /**
        Handles an object collision. Object A is the moving
        object, and Object B is the object that Object A collided
        with. Object A slides around or steps on top of
        Object B if possible.
    */
    protected boolean handleObjectCollision(GameObject objectA,
        GameObject objectB, float distSq, float minDistSq,
        Vector3D oldLocation)
    {
        objectA.getListener().notifyObjectCollision(
            objectA, objectB);

        // if objectB has no polygons, it's a trigger area
        if (objectB.getPolygonGroup().isEmpty()) {
            return false;
        }

        if (objectA.isFlying()) {
            return true;
        }

        float stepSize = objectA.getBounds().getTopHeight() / 6;
        Vector3D velocity =
            objectA.getTransform().getVelocity();

        // step up on top of object if possible
        float objectABottom = objectA.getY() +
            objectA.getBounds().getBottomHeight();
        float objectBTop = objectB.getY() +
            objectB.getBounds().getTopHeight();
        if (objectABottom + stepSize > objectBTop &&
            objectBTop +
            objectA.getBounds().getTopHeight() <
            objectA.getCeilHeight())
        {
            objectA.getLocation().y = (objectBTop -
                objectA.getBounds().getBottomHeight());
            if (velocity.y < 0) {
                objectA.setJumping(false);
                // don't let gravity get out of control
                velocity.y = -.01f;
            }
            return false;
        }

        if (objectA.getX() != oldLocation.x ||
            objectA.getZ() != oldLocation.z)
        {
            // slide to the side
            float slideDistFactor =
                (float)Math.sqrt(minDistSq / distSq) - 1;
            scratch.setTo(objectA.getX(), 0, objectA.getZ());
            scratch.subtract(objectB.getX(), 0, objectB.getZ());
            scratch.multiply(slideDistFactor);
            objectA.getLocation().add(scratch);

            // revert location if passing through a wall
            if (super.checkWalls(objectA, oldLocation, 0)
                != null)
            {
                return true;
            }

            return false;
        }

        return true;
    }



}
