package game.io.tests;
import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;
import game.graphics.*;
import game.core.*;

/**
    A simple mouselook test. Using mouselook, the user can
    virtually move the mouse in any direction indefinitly.
    Without mouselook, the mouse stops when it hits the edge of
    the screen.
    <p>Mouselook works by recentering the mouse whenever it is
    moved, so it can always measure the relative mouse movement,
    and the mouse never hits the edge of the screen.
*/
public class MouseLookTest extends GameCore
    implements MouseMotionListener, KeyListener
{

    public static void main(String[] args) {
        new MouseLookTest().run();
    }

    private Image bgImage;
    private Robot robot;
    private Point mouseLocation;
    private Point centerLocation;
    private Point imageLocation;
    private boolean relativeMouseMode;
    private boolean isRecentering;

    public void init() {
        super.init();
        mouseLocation = new Point();
        centerLocation = new Point();
        imageLocation = new Point();

        relativeMouseMode = true;
        isRecentering = false;

        try {
            robot = new Robot();
            recenterMouse();
            mouseLocation.x = centerLocation.x;
            mouseLocation.y = centerLocation.y;
        }
        catch (AWTException ex) {
            System.out.println("Couldn't create Robot!");
        }
        Window window = screen.getFullScreenWindow();
        window.addMouseMotionListener(this);
        window.addKeyListener(this);
        bgImage = loadImage("images/background.jpg");
    }


    public synchronized void draw(Graphics2D g) {

        int w = screen.getWidth();
        int h = screen.getHeight();

        // make sure position is correct
        imageLocation.x %= w;
        imageLocation.y %= screen.getHeight();
        if (imageLocation.x < 0) {
            imageLocation.x += w;
        }
        if (imageLocation.y < 0) {
            imageLocation.y += screen.getHeight();
        }

        // draw the image in four places to cover the screen
        int x = imageLocation.x;
        int y = imageLocation.y;
        g.drawImage(bgImage, x, y, null);
        g.drawImage(bgImage, x-w, y, null);
        g.drawImage(bgImage, x, y-h, null);
        g.drawImage(bgImage, x-w, y-h, null);

        // draw instructions
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString("Press Space to change mouse modes.", 5,
            FONT_SIZE);
        g.drawString("Press Escape to exit.", 5, FONT_SIZE*2);
    }


    /**
        Uses the Robot class to try to postion the mouse in the
        center of the screen.
        <p>Note that use of the Robot class may not be available
        on all platforms.
    */
    private synchronized void recenterMouse() {
        Window window = screen.getFullScreenWindow();
        if (robot != null && window.isShowing()) {
            centerLocation.x = window.getWidth() / 2;
            centerLocation.y = window.getHeight() / 2;
            SwingUtilities.convertPointToScreen(centerLocation,
                window);
            isRecentering = true;
            robot.mouseMove(centerLocation.x, centerLocation.y);
        }
    }


    // from the MouseMotionListener interface
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }


    // from the MouseMotionListener interface
    public synchronized void mouseMoved(MouseEvent e) {
        // this event is from re-centering the mouse - ignore it
        if (isRecentering &&
            centerLocation.x == e.getX() &&
            centerLocation.y == e.getY())
        {
            isRecentering = false;
        }
        else {
            int dx = e.getX() - mouseLocation.x;
            int dy = e.getY() - mouseLocation.y;
            imageLocation.x -= dx;
            imageLocation.y -= dy;
            // recenter the mouse
            if (relativeMouseMode) {
                recenterMouse();
            }

        }

        mouseLocation.x = e.getX();
        mouseLocation.y = e.getY();

    }


    // from the KeyListener interface
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            // exit the program
            stop();
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // change relative mouse mode
            relativeMouseMode = !relativeMouseMode;
        }
    }


    // from the KeyListener interface
    public void keyReleased(KeyEvent e) {
        // do nothing
    }


    // from the KeyListener interface
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

}

