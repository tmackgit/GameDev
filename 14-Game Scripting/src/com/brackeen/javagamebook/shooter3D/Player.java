package com.brackeen.javagamebook.shooter3D;

import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.*;
import com.brackeen.javagamebook.ai.Projectile;

/**
    A Player object.
*/
public class Player extends JumpingGameObject {

    private static final float BULLET_HEIGHT = 75;
    private static final float DEFAULT_PLAYER_RADIUS = 32;
    private static final float DEFAULT_PLAYER_HEIGHT = 128;
    private static final float DEFAULT_MAX_HEALTH = 100;

    private PolygonGroup blastModel;
    private float maxHealth;
    private float health;

    public Player() {
        super(new PolygonGroup("player"));

        // set up player bounds
        PolygonGroupBounds playerBounds = getBounds();
        playerBounds.setTopHeight(DEFAULT_PLAYER_HEIGHT);
        playerBounds.setRadius(DEFAULT_PLAYER_RADIUS);

        // set up health
        maxHealth = DEFAULT_MAX_HEALTH;
        setHealth(maxHealth);
    }

    public void setBlastModel(PolygonGroup blastModel) {
        this.blastModel = blastModel;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void addHealth(float addition) {
        setHealth(health + addition);
    }

    public boolean isAlive() {
        return (health > 0);
    }

    public void fireProjectile() {

        //
        float x = -getTransform().getSinAngleY();
        float z = -getTransform().getCosAngleY();
        float cosX = getTransform().getCosAngleX();
        float sinX = getTransform().getSinAngleX();
        Projectile blast = new Projectile(
            (PolygonGroup)blastModel.clone(),
            new Vector3D(cosX*x, sinX, cosX*z),
            null,
            40, 60);
        float dist = getBounds().getRadius() +
            blast.getBounds().getRadius();
        // blast starting location needs work. looks like
        // the blast is coming out of your forehead when
        // you're shooting down.
        blast.getLocation().setTo(
            getX() + x*dist,
            getY() + BULLET_HEIGHT,
            getZ() + z*dist);

        // "spawns" the new game object
        addSpawn(blast);

        // make a "virtual" noise that bots can "hear"
        // (500 milliseconds)
        makeNoise(500);
    }

}
