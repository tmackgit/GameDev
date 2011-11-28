package com.brackeen.javagamebook.game;

import com.brackeen.javagamebook.math3D.Vector3D;

/**
    The Physics class is a singleton that represents various
    attributes (like gravity) and the functions to manipulate
    objects based on those physical attributes. Currently,
    only gravity and scoot-up (acceleration when
    traveling up stairs) are supported.
*/
public class Physics {

    /**
        Default gravity in units per millisecond squared
    */
    public static final float DEFAULT_GRAVITY_ACCEL = -.002f;

    /**
        Default scoot-up (acceleration traveling up stairs)
        in units per millisecond squared.
    */
    public static final float DEFAULT_SCOOT_ACCEL = .006f;

    private static Physics instance;

    private float gravityAccel;
    private float scootAccel;
    private Vector3D velocity = new Vector3D();

    /**
        Gets the Physics instance. If a Physics instance does
        not yet exist, one is created with the default attributes.
    */
    public static synchronized Physics getInstance() {
        if (instance == null) {
            instance = new Physics();
        }
        return instance;
    }


    protected Physics() {
        gravityAccel = DEFAULT_GRAVITY_ACCEL;
        scootAccel = DEFAULT_SCOOT_ACCEL;
    }


    /**
        Gets the gravity acceleration in units per millisecond
        squared.
    */
    public float getGravityAccel() {
        return gravityAccel;
    }


    /**
        Sets the gravity acceleration in units per millisecond
        squared.
    */
    public void setGravityAccel(float gravityAccel) {
        this.gravityAccel = gravityAccel;
    }


    /**
        Gets the scoot-up acceleration in units per millisecond
        squared. The scoot up acceleration can be used for
        smoothly traveling up stairs.
    */
    public float getScootAccel() {
        return scootAccel;
    }


    /**
        Sets the scoot-up acceleration in units per millisecond
        squared. The scoot up acceleration can be used for
        smoothly traveling up stairs.
    */
    public void setScootAccel(float scootAccel) {
        this.scootAccel = scootAccel;
    }


    /**
        Applies gravity to the specified GameObject according
        to the amount of time that has passed.
    */
    public void applyGravity(GameObject object, long elapsedTime) {
        velocity.setTo(0, gravityAccel * elapsedTime, 0);
        object.getTransform().addVelocity(velocity);
    }


    /**
        Applies the scoot-up acceleration to the specified
        GameObject according to the amount of time that has passed.
    */
    public void scootUp(GameObject object, long elapsedTime) {
        velocity.setTo(0, scootAccel * elapsedTime, 0);
        object.getTransform().addVelocity(velocity);
    }


    /**
        Applies the negative scoot-up acceleration to the specified
        GameObject according to the amount of time that has passed.
    */
    public void scootDown(GameObject object, long elapsedTime) {
        velocity.setTo(0, -scootAccel * elapsedTime, 0);
        object.getTransform().addVelocity(velocity);
    }


    /**
        Sets the specified GameObject's vertical velocity to jump
        to the specified height. Calls getJumpVelocity() to
        calculate the velocity, which uses the Math.sqrt()
        function.
    */
    public void jumpToHeight(GameObject object, float jumpHeight) {
        jump(object, getJumpVelocity(jumpHeight));
    }


    /**
        Sets the specified GameObject's vertical velocity to the
        specified jump velocity.
    */
    public void jump(GameObject object, float jumpVelocity) {
        velocity.setTo(0, jumpVelocity, 0);
        object.getTransform().getVelocity().y = 0;
        object.getTransform().addVelocity(velocity);
    }


    /**
        Returns the vertical velocity needed to jump the specified
        height (based on current gravity). Uses the Math.sqrt()
        function.
    */
    public float getJumpVelocity(float jumpHeight) {
        // use velocity/acceleration formal: v*v = -2 * a(y-y0)
        // (v is jump velocity, a is accel, y-y0 is max height)
        return (float)Math.sqrt(-2*gravityAccel*jumpHeight);
    }
}
