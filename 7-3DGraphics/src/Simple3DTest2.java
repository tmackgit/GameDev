import com.brackeen.javagamebook.test.GameCore;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;

import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.*;

public class Simple3DTest2 extends GameCore {

    public static void main(String[] args) {
        new Simple3DTest2().run();
    }

    protected PolygonRenderer polygonRenderer;
    protected ViewWindow viewWindow;
    protected List polygons;

    private boolean drawFrameRate = false;
    private boolean drawInstructions = true;

    // for calculating frame rate
    private int numFrames;
    private long startTime;
    private float frameRate;

    protected InputManager inputManager;
    private GameAction exit = new GameAction("exit");
    private GameAction smallerView = new GameAction("smallerView",
        GameAction.DETECT_INITAL_PRESS_ONLY);
    private GameAction largerView = new GameAction("largerView",
        GameAction.DETECT_INITAL_PRESS_ONLY);
    private GameAction frameRateToggle = new GameAction(
        "frameRateToggle", GameAction.DETECT_INITAL_PRESS_ONLY);
    private GameAction goForward = new GameAction("goForward");
    private GameAction goBackward = new GameAction("goBackward");
    private GameAction goUp = new GameAction("goUp");
    private GameAction goDown = new GameAction("goDown");
    private GameAction goLeft = new GameAction("goLeft");
    private GameAction goRight = new GameAction("goRight");
    private GameAction turnLeft = new GameAction("turnLeft");
    private GameAction turnRight = new GameAction("turnRight");
    private GameAction tiltUp = new GameAction("tiltUp");
    private GameAction tiltDown = new GameAction("tiltDown");
    private GameAction tiltLeft = new GameAction("tiltLeft");
    private GameAction tiltRight = new GameAction("tiltRight");

    public void init(DisplayMode[] modes) {
        super.init(modes);

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setRelativeMouseMode(true);
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(goForward, KeyEvent.VK_W);
        inputManager.mapToKey(goForward, KeyEvent.VK_UP);
        inputManager.mapToKey(goBackward, KeyEvent.VK_S);
        inputManager.mapToKey(goBackward, KeyEvent.VK_DOWN);
        inputManager.mapToKey(goLeft, KeyEvent.VK_A);
        inputManager.mapToKey(goLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(goRight, KeyEvent.VK_D);
        inputManager.mapToKey(goRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(goUp, KeyEvent.VK_PAGE_UP);
        inputManager.mapToKey(goDown, KeyEvent.VK_PAGE_DOWN);
        inputManager.mapToMouse(turnLeft,
            InputManager.MOUSE_MOVE_LEFT);
        inputManager.mapToMouse(turnRight,
            InputManager.MOUSE_MOVE_RIGHT);
        inputManager.mapToMouse(tiltUp,
            InputManager.MOUSE_MOVE_UP);
        inputManager.mapToMouse(tiltDown,
            InputManager.MOUSE_MOVE_DOWN);

        inputManager.mapToKey(tiltLeft, KeyEvent.VK_INSERT);
        inputManager.mapToKey(tiltRight, KeyEvent.VK_DELETE);

        inputManager.mapToKey(smallerView, KeyEvent.VK_SUBTRACT);
        inputManager.mapToKey(smallerView, KeyEvent.VK_MINUS);
        inputManager.mapToKey(largerView, KeyEvent.VK_ADD);
        inputManager.mapToKey(largerView, KeyEvent.VK_PLUS);
        inputManager.mapToKey(largerView, KeyEvent.VK_EQUALS);
        inputManager.mapToKey(frameRateToggle, KeyEvent.VK_R);

        // create the polygon renderer
        createPolygonRenderer();

        // create polygons
        polygons = new ArrayList();
        createPolygons();
    }


    // create a house (convex polyhedra)
    public void createPolygons() {
        SolidPolygon3D poly;

        // walls
        poly = new SolidPolygon3D(
            new Vector3D(-200, 0, -1000),
            new Vector3D(200, 0, -1000),
            new Vector3D(200, 250, -1000),
            new Vector3D(-200, 250, -1000));
        poly.setColor(Color.WHITE);
        polygons.add(poly);
        poly = new SolidPolygon3D(
            new Vector3D(-200, 0, -1400),
            new Vector3D(-200, 250, -1400),
            new Vector3D(200, 250, -1400),
            new Vector3D(200, 0, -1400));
        poly.setColor(Color.WHITE);
        polygons.add(poly);
        poly = new SolidPolygon3D(
            new Vector3D(-200, 0, -1400),
            new Vector3D(-200, 0, -1000),
            new Vector3D(-200, 250, -1000),
            new Vector3D(-200, 250, -1400));
        poly.setColor(Color.GRAY);
        polygons.add(poly);
        poly = new SolidPolygon3D(
            new Vector3D(200, 0, -1000),
            new Vector3D(200, 0, -1400),
            new Vector3D(200, 250, -1400),
            new Vector3D(200, 250, -1000));
        poly.setColor(Color.GRAY);
        polygons.add(poly);

        // door and windows
        poly = new SolidPolygon3D(
            new Vector3D(0, 0, -1000),
            new Vector3D(75, 0, -1000),
            new Vector3D(75, 125, -1000),
            new Vector3D(0, 125, -1000));
        poly.setColor(new Color(0x660000));
        polygons.add(poly);
        poly = new SolidPolygon3D(
            new Vector3D(-150, 150, -1000),
            new Vector3D(-100, 150, -1000),
            new Vector3D(-100, 200, -1000),
            new Vector3D(-150, 200, -1000));
        poly.setColor(new Color(0x660000));
        polygons.add(poly);

        // roof
        poly = new SolidPolygon3D(
            new Vector3D(-200, 250, -1000),
            new Vector3D(200, 250, -1000),
            new Vector3D(75, 400, -1200),
            new Vector3D(-75, 400, -1200));
        poly.setColor(new Color(0x660000));
        polygons.add(poly);
        poly = new SolidPolygon3D(
            new Vector3D(-200, 250, -1400),
            new Vector3D(-200, 250, -1000),
            new Vector3D(-75, 400, -1200));
        poly.setColor(new Color(0x330000));
        polygons.add(poly);
        poly = new SolidPolygon3D(
            new Vector3D(200, 250, -1400),
            new Vector3D(-200, 250, -1400),
            new Vector3D(-75, 400, -1200),
            new Vector3D(75, 400, -1200));
        poly.setColor(new Color(0x660000));
        polygons.add(poly);
        poly = new SolidPolygon3D(
            new Vector3D(200, 250, -1000),
            new Vector3D(200, 250, -1400),
            new Vector3D(75, 400, -1200));
        poly.setColor(new Color(0x330000));
        polygons.add(poly);
    }


    public void createPolygonRenderer() {
        // make the view window the entire screen
        viewWindow = new ViewWindow(0, 0,
            screen.getWidth(), screen.getHeight(),
            (float)Math.toRadians(75));

        Transform3D camera = new Transform3D(0,100,0);
        polygonRenderer = new SolidPolygonRenderer(
            camera, viewWindow);
    }


    /**
        Sets the view bounds, centering the view on the screen.
    */
    public void setViewBounds(int width, int height) {
        width = Math.min(width, screen.getWidth());
        height = Math.min(height, screen.getHeight());
        width = Math.max(64, width);
        height = Math.max(48, height);
        viewWindow.setBounds((screen.getWidth() - width) /2,
            (screen.getHeight() - height) /2, width, height);
    }


    public void update(long elapsedTime) {
        if (exit.isPressed()) {
            stop();
            return;
        }

        // check options
        if (largerView.isPressed()) {
            setViewBounds(viewWindow.getWidth() + 64,
                viewWindow.getHeight() + 48);
        }
        else if (smallerView.isPressed()) {
            setViewBounds(viewWindow.getWidth() - 64,
                viewWindow.getHeight() - 48);
        }
        if (frameRateToggle.isPressed()) {
            drawFrameRate = !drawFrameRate;
        }

        // cap elapsedTime
        elapsedTime = Math.min(elapsedTime, 100);

        float angleChange = 0.0002f*elapsedTime;
        float distanceChange = .5f*elapsedTime;

        Transform3D camera = polygonRenderer.getCamera();
        Vector3D cameraLoc = camera.getLocation();

        // apply movement
        if (goForward.isPressed()) {
            cameraLoc.x -= distanceChange * camera.getSinAngleY();
            cameraLoc.z -= distanceChange * camera.getCosAngleY();
        }
        if (goBackward.isPressed()) {
            cameraLoc.x += distanceChange * camera.getSinAngleY();
            cameraLoc.z += distanceChange * camera.getCosAngleY();
        }
        if (goLeft.isPressed()) {
            cameraLoc.x -= distanceChange * camera.getCosAngleY();
            cameraLoc.z += distanceChange * camera.getSinAngleY();
        }
        if (goRight.isPressed()) {
            cameraLoc.x += distanceChange * camera.getCosAngleY();
            cameraLoc.z -= distanceChange * camera.getSinAngleY();
        }
        if (goUp.isPressed()) {
            cameraLoc.y += distanceChange;
        }
        if (goDown.isPressed()) {
            cameraLoc.y -= distanceChange;
        }

        // look up/down (rotate around x)
        int tilt = tiltUp.getAmount() - tiltDown.getAmount();
        tilt = Math.min(tilt, 200);
        tilt = Math.max(tilt, -200);

        // limit how far you can look up/down
        float newAngleX = camera.getAngleX() + tilt * angleChange;
        newAngleX = Math.max(newAngleX, (float)-Math.PI/2);
        newAngleX = Math.min(newAngleX, (float)Math.PI/2);
        camera.setAngleX(newAngleX);

        // turn (rotate around y)
        int turn = turnLeft.getAmount() - turnRight.getAmount();
        turn = Math.min(turn, 200);
        turn = Math.max(turn, -200);
        camera.rotateAngleY(turn * angleChange);

        // tilet head left/right (rotate around z)
        if (tiltLeft.isPressed()) {
            camera.rotateAngleZ(10*angleChange);
        }
        if (tiltRight.isPressed()) {
            camera.rotateAngleZ(-10*angleChange);
        }
    }


    public void draw(Graphics2D g) {

        // draw polygons
        polygonRenderer.startFrame(g);
        for (int i=0; i<polygons.size(); i++) {
            polygonRenderer.draw(g, (Polygon3D)polygons.get(i));
        }
        polygonRenderer.endFrame(g);

        drawText(g);
    }


    public void drawText(Graphics g) {

        // draw text
        g.setColor(Color.WHITE);
        if (drawInstructions) {
            g.drawString("Use the mouse/arrow keys to move. " +
                "Press Esc to exit.", 5, fontSize);
        }
        // (you may have to turn off the BufferStrategy in
        // ScreenManager for more accurate tests)
        if (drawFrameRate) {
            calcFrameRate();
            g.drawString(frameRate + " frames/sec", 5,
                screen.getHeight() - 5);
        }
    }


    public void calcFrameRate() {
        numFrames++;
        long currTime = System.currentTimeMillis();

        // calculate the frame rate every 500 milliseconds
        if (currTime > startTime + 500) {
            frameRate = (float)numFrames * 1000 /
                (currTime - startTime);
            startTime = currTime;
            numFrames = 0;
        }
    }

}