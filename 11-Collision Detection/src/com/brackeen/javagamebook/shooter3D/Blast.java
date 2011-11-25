package com.brackeen.javagamebook.shooter3D;

import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.GameObject;

/**
    The Blast GameObject is a projectile, designed to travel
    in a straight line for five seconds, then die. Blasts
    destroy Bots instantly.
*/
public class Blast extends GameObject {

    private static final long DIE_TIME = 5000;
    private static final float SPEED = 1.5f;
    private static final float ROT_SPEED = .008f;

    private MovingTransform3D transform;
    private long aliveTime;

    /**
        Create a new Blast with the specified PolygonGroup
        and normalized vector direction.
    */
    public Blast(PolygonGroup polygonGroup, Vector3D direction) {
        super(polygonGroup);
        transform = polygonGroup.getTransform();
        Vector3D velocity = transform.getVelocity();
        velocity.setTo(direction);
        velocity.multiply(SPEED);
        transform.setVelocity(velocity);
        //transform.setAngleVelocityX(ROT_SPEED);
        transform.setAngleVelocityY(ROT_SPEED);
        transform.setAngleVelocityZ(ROT_SPEED);
        setState(STATE_ACTIVE);
    }


    public void update(GameObject player, long elapsedTime) {
        aliveTime+=elapsedTime;
        if (aliveTime >= DIE_TIME) {
            setState(STATE_DESTROYED);
        }
        else {
            super.update(player, elapsedTime);
        }
    }


    public boolean isFlying() {
        return true;
    }


    public void notifyObjectCollision(GameObject object) {
        // destroy bots and itself
        if (object instanceof Bot) {
            setState(object, STATE_DESTROYED);
            setState(STATE_DESTROYED);
        }
    }


    public void notifyWallCollision() {
        // stop the projectile from moving
        transform.getVelocity().setTo(0,0,0);
    }


    public void notifyFloorCollision() {
        notifyWallCollision();
    }


    public void notifyCeilingCollision() {
        notifyWallCollision();
    }
}
