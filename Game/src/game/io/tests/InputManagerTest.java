package game.io.tests;

import java.awt.*;
import java.awt.event.KeyEvent;
import game.graphics.*;
import game.core.*;
import game.io.*;


/**
    InputManagerTest tests the InputManager with a simple
    run-and-jump mechanism. The player moves and jumps using
    the arrow keys and the space bar.
    <p>Also, InputManagerTest demonstrates pausing a game
    by not updating the game elements if the game is paused.
*/
public class InputManagerTest extends GameCore {

    public static void main(String[] args) {
        new InputManagerTest().run();
    }

    protected GameAction jump;
    protected GameAction exit;
    protected GameAction moveLeft;
    protected GameAction moveRight;
    protected GameAction pause;
    protected InputManager inputManager;
    private Player player;
    private Image bgImage;
    private boolean paused;

    public void init() {
        super.init();
        Window window = screen.getFullScreenWindow();
        inputManager = new InputManager(window);

        // use these lines for relative mouse mode
        //inputManager.setRelativeMouseMode(true);
        //inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        createGameActions();
        createSprite();
        paused = false;
    }


    /**
        Tests whether the game is paused or not.
    */
    public boolean isPaused() {
        return paused;
    }


    /**
        Sets the paused state.
    */
    public void setPaused(boolean p) {
        if (paused != p) {
            this.paused = p;
            inputManager.resetAllGameActions();
        }
    }


    public void update(long elapsedTime) {
        // check input that can happen whether paused or not
        checkSystemInput();

        if (!isPaused()) {
            // check game input
            checkGameInput();

            // update sprite
            player.update(elapsedTime);
        }
    }


    /**
        Checks input from GameActions that can be pressed
        regardless of whether the game is paused or not.
    */
    public void checkSystemInput() {
        if (pause.isPressed()) {
            setPaused(!isPaused());
        }
        if (exit.isPressed()) {
            stop();
        }
    }


    /**
        Checks input from GameActions that can be pressed
        only when the game is not paused.
    */
    public void checkGameInput() {
        float velocityX = 0;
        if (moveLeft.isPressed()) {
            velocityX-=Player.SPEED;
        }
        if (moveRight.isPressed()) {
            velocityX+=Player.SPEED;
        }
        player.setVelocityX(velocityX);

        if (jump.isPressed() &&
            player.getState() != Player.STATE_JUMPING)
        {
            player.jump();
        }
    }


    public void draw(Graphics2D g) {
        // draw background
        g.drawImage(bgImage, 0, 0, null);

        // draw sprite
        g.drawImage(player.getImage(),
            Math.round(player.getX()),
            Math.round(player.getY()),
            null);
    }


    /**
        Creates GameActions and maps them to keys.
    */
    public void createGameActions() {
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        pause = new GameAction("pause",
            GameAction.DETECT_INITAL_PRESS_ONLY);

        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(pause, KeyEvent.VK_P);

        // jump with spacebar or mouse button
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToMouse(jump,
            InputManager.MOUSE_BUTTON_1);

        // move with the arrow keys...
        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);

        // ... or with A and D.
        inputManager.mapToKey(moveLeft, KeyEvent.VK_A);
        inputManager.mapToKey(moveRight, KeyEvent.VK_D);

        // use these lines to map player movement to the mouse
        //inputManager.mapToMouse(moveLeft,
        //  InputManager.MOUSE_MOVE_LEFT);
        //inputManager.mapToMouse(moveRight,
        //  InputManager.MOUSE_MOVE_RIGHT);

    }


    /**
        Load images and creates the Player sprite.
    */
    private void createSprite() {
        // load images
        bgImage = loadImage("images/background.jpg");
        Image player1 = loadImage("images/player1.png");
        Image player2 = loadImage("images/player2.png");
        Image player3 = loadImage("images/player3.png");

        // create animation
        Animation anim = new Animation();
        anim.addFrame(player1, 250);
        anim.addFrame(player2, 150);
        anim.addFrame(player1, 150);
        anim.addFrame(player2, 150);
        anim.addFrame(player3, 200);
        anim.addFrame(player2, 150);

        player = new Player(anim);
        player.setFloorY(screen.getHeight() - player.getHeight());
    }

}
