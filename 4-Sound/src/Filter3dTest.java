import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.IOException;
import javax.sound.sampled.*;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.util.LoopingByteInputStream;

/**
    The Filter3dTest class demonstrates the Filter3d
    functionality. A fly buzzes around the listener, and the
    closer the fly is, the louder it's heard.
    @see Filter3d
    @see SimpleSoundPlayer
*/
public class Filter3dTest extends GameCore {

    public static void main(String[] args) {
        new Filter3dTest().run();
    }

    private Sprite fly;
    private Sprite listener;
    private InputManager inputManager;
    private GameAction exit;

    private SimpleSoundPlayer bzzSound;
    private InputStream bzzSoundStream;

    public void init() {
        super.init();

        // set up input manager
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        createSprites();

        // load the sound
        bzzSound = new SimpleSoundPlayer("../sounds/fly-bzz.wav");

        // create the 3d filter
        Filter3d filter =
            new Filter3d(fly, listener, screen.getHeight());

        // create the filtered sound stream
        bzzSoundStream = new FilteredSoundStream(
            new LoopingByteInputStream(bzzSound.getSamples()),
            filter);

        // play the sound in a separate thread
        new Thread() {
            public void run() {
                bzzSound.play(bzzSoundStream);
            }
        }.start();
    }


    /**
        Loads images and creates sprites.
    */
    private void createSprites() {
        // load images
        Image fly1 = loadImage("../images/fly1.png");
        Image fly2 = loadImage("../images/fly2.png");
        Image fly3 = loadImage("../images/fly3.png");
        Image ear = loadImage("../images/ear.png");

        // create "fly" sprite
        Animation anim = new Animation();
        anim.addFrame(fly1, 50);
        anim.addFrame(fly2, 50);
        anim.addFrame(fly3, 50);
        anim.addFrame(fly2, 50);

        fly = new Sprite(anim);

        // create the listener sprite
        anim = new Animation();
        anim.addFrame(ear, 0);
        listener = new Sprite(anim);
        listener.setX(
            (screen.getWidth() - listener.getWidth()) / 2);
        listener.setY(
            (screen.getHeight() - listener.getHeight()) / 2);
    }


    public void update(long elapsedTime) {
        if (exit.isPressed()) {
            stop();
        }
        else {
            listener.update(elapsedTime);
            fly.update(elapsedTime);
            fly.setX(inputManager.getMouseX());
            fly.setY(inputManager.getMouseY());
        }
    }


    public void stop() {
        super.stop();
        // stop the bzz sound
        try {
            bzzSoundStream.close();
        }
        catch (IOException ex) { }
    }


    public void draw(Graphics2D g) {

        // draw background
        g.setColor(new Color(0x33cc33));
        g.fillRect(0, 0, screen.getWidth(), screen.getHeight());

        // draw listener
        g.drawImage(listener.getImage(),
            Math.round(listener.getX()),
            Math.round(listener.getY()),
            null);

        // draw fly
        g.drawImage(fly.getImage(),
            Math.round(fly.getX()),
            Math.round(fly.getY()),
            null);
    }

}
