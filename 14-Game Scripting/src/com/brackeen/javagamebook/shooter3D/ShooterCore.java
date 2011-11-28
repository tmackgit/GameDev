package com.brackeen.javagamebook.shooter3D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import com.brackeen.javagamebook.bsp2D.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.*;
import com.brackeen.javagamebook.graphics3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;
import com.brackeen.javagamebook.test.GameCore3D;

public abstract class ShooterCore extends GameCore3D {

    private static final float PLAYER_SPEED = .5f;
    private static final float PLAYER_TURN_SPEED = 0.04f;
    private static final float CAMERA_HEIGHT = 100;

    protected GameAction fire = new GameAction("fire",
        GameAction.DETECT_INITAL_PRESS_ONLY);
    protected GameAction jump = new GameAction("jump",
        GameAction.DETECT_INITAL_PRESS_ONLY);

    protected GameObjectManager gameObjectManager;
    protected DisplayMode[] modes;
    protected PolygonGroup botProjectileModel;
    protected BSPTree bspTree;
    protected CollisionDetection collisionDetection;
    protected String mapFile;

    public ShooterCore(String[] args, String defaultMap) {
        modes = LOW_RES_MODES;
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-lowres")) {
                modes = VERY_LOW_RES_MODES;
                fontSize = 12;
            }
        }
        for (int i=0; mapFile == null && i<args.length; i++) {
            if (mapFile == null && !args[i].startsWith("-")) {
                mapFile = args[i];
            }
        }
        if (mapFile == null) {
            mapFile = defaultMap;
        }
    }


    public void init() {


        // set up the local lights for the model.
        float ambientLightIntensity = .8f;
        List lights = new LinkedList();
        lights.add(new PointLight3D(-100,100,100, .5f, -1));
        lights.add(new PointLight3D(100,100,0, .5f, -1));

        // load the object model
        ObjectLoader loader = new ObjectLoader();
        loader.setLights(lights, ambientLightIntensity);
        PolygonGroup blastModel = null;
        try {
            blastModel = loader.loadObject("../images/blast.obj");
            botProjectileModel =
                loader.loadObject("../images/botprojectile.obj");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        init(modes);

        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToMouse(fire, InputManager.MOUSE_BUTTON_1);

        ((Player)gameObjectManager.getPlayer()).
            setBlastModel(blastModel);
    }


    public void createPolygonRenderer() {
        // make the view window the entire screen
        viewWindow = new ViewWindow(0, 0,
            screen.getWidth(), screen.getHeight(),
            (float)Math.toRadians(75));

        Transform3D camera = new Transform3D();
        polygonRenderer = new BSPRenderer(camera, viewWindow);
    }


    public void updateWorld(long elapsedTime) {

        float angleVelocity;

        Player player = (Player)gameObjectManager.getPlayer();
        MovingTransform3D playerTransform = player.getTransform();
        Vector3D velocity = playerTransform.getVelocity();

        //playerTransform.stop();
        velocity.x = 0;
        velocity.z = 0;
        float x = -playerTransform.getSinAngleY();
        float z = -playerTransform.getCosAngleY();
        if (goForward.isPressed()) {
            velocity.add(x * PLAYER_SPEED, 0, z * PLAYER_SPEED);
        }
        if (goBackward.isPressed()) {
            velocity.add(-x * PLAYER_SPEED, 0, -z * PLAYER_SPEED);
        }
        if (goLeft.isPressed()) {
            velocity.add(z * PLAYER_SPEED, 0, -x * PLAYER_SPEED);
        }
        if (goRight.isPressed()) {
            velocity.add(-z * PLAYER_SPEED, 0, x * PLAYER_SPEED);
        }
        if (jump.isPressed()) {
            player.setJumping(true);
        }
        if (fire.isPressed()) {
            player.fireProjectile();
        }


        playerTransform.setVelocity(velocity);

        // look up/down (rotate around x)
        angleVelocity = Math.min(tiltUp.getAmount(), 200);
        angleVelocity += Math.max(-tiltDown.getAmount(), -200);
        playerTransform.setAngleVelocityX(angleVelocity *
             PLAYER_TURN_SPEED / 200);

        // turn (rotate around y)
        angleVelocity = Math.min(turnLeft.getAmount(), 200);
        angleVelocity += Math.max(-turnRight.getAmount(), -200);
        playerTransform.setAngleVelocityY(angleVelocity *
             PLAYER_TURN_SPEED / 200);

        // update objects
        gameObjectManager.update(elapsedTime);

        // limit look up/down
        float angleX = playerTransform.getAngleX();
        float limit = (float)Math.PI / 2;
        if (angleX < -limit) {
            playerTransform.setAngleX(-limit);
        }
        else if (angleX > limit) {
            playerTransform.setAngleX(limit);
        }

        // set the camera to be 100 units above the player
        Transform3D camera = polygonRenderer.getCamera();
        camera.setTo(playerTransform);
        camera.getLocation().add(0,CAMERA_HEIGHT,0);

    }

    public void createPolygons() {
        Graphics2D g = screen.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0, screen.getWidth(), screen.getHeight());
        g.setColor(Color.WHITE);
        g.drawString("Loading...", 5, screen.getHeight() - 5);
        screen.update();

        float ambientLightIntensity = .2f;
        List lights = new LinkedList();
        lights.add(new PointLight3D(-100,100,100, .3f, -1));
        lights.add(new PointLight3D(100,100,0, .3f, -1));

        MapLoader loader = new MapLoader(
            new BSPTreeBuilderWithPortals());
        loader.setObjectLights(lights, ambientLightIntensity);

        try {
            bspTree = loader.loadMap(mapFile);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        collisionDetection =
            new CollisionDetectionWithSliding(bspTree);
        gameObjectManager = new GridGameObjectManager(
            bspTree.calcBounds(), collisionDetection);
        gameObjectManager.addPlayer(new Player());

        ((BSPRenderer)polygonRenderer).setGameObjectManager(
            gameObjectManager);


        createGameObjects(loader.getObjectsInMap());
        Transform3D start = loader.getPlayerStartLocation();
        gameObjectManager.getPlayer().getTransform().setTo(start);
    }


    protected abstract void createGameObjects(List mapObjects);

    public void drawPolygons(Graphics2D g) {

        polygonRenderer.startFrame(g);

        // draw polygons in bsp tree (set z buffer)
        ((BSPRenderer)polygonRenderer).draw(g, bspTree);

        // draw game object polygons (check and set z buffer)
        gameObjectManager.draw(g,
            (GameObjectRenderer)polygonRenderer);

        polygonRenderer.endFrame(g);

    }

}