package game.io.tests;
import game.graphics.*;


/**
    The Player extends the Sprite class to add states
    (STATE_NORMAL or STATE_JUMPING) and gravity.
*/
public class Player extends Sprite {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_JUMPING = 1;

    public static final float SPEED = .3f;
    public static final float GRAVITY = .002f;

    private int floorY;
    private int state;

    public Player(Animation anim) {
        super(anim);
        state = STATE_NORMAL;
    }


	/**
        Gets the state of the Player (either STATE_NORMAL or
        STATE_JUMPING);
    */
    public int getState() {
        return state;
    }


    /**
        Sets the state of the Player (either STATE_NORMAL or
        STATE_JUMPING);
    */
    public void setState(int state) {
        this.state = state;
    }


    /**
        Sets the location of "floor", where the Player starts
        and lands after jumping.
    */
    public void setFloorY(int floorY) {
        this.floorY = floorY;
        setY(floorY);
    }


    /**
        Causes the Player to jump
    */
    public void jump() {
        setVelocityY(-1);
        state = STATE_JUMPING;
    }


    /**
        Updates the player's positon and animation. Also, sets the
        Player's state to NORMAL if a jumping Player landed on
        the floor.
    */
    public void update(long elapsedTime) {
        // set vertical velocity (gravity effect)
        if (getState() == STATE_JUMPING) {
            setVelocityY(getVelocityY() + GRAVITY * elapsedTime);
        }

        // move player
        super.update(elapsedTime);

        // check if player landed on floor
        if (getState() == STATE_JUMPING && getY() >= floorY) {
            setVelocityY(0);
            setY(floorY);
            setState(STATE_NORMAL);
        }

    }
}