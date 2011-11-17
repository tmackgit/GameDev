import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.*;
import com.brackeen.javagamebook.graphics3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;
import com.brackeen.javagamebook.test.GameCore3D;

public class GameObjectTest extends GameCore3D {

    public static void main(String[] args) {
        new GameObjectTest().run();
    }

    private static final int NUM_BOTS = 5;
    private static final int NUM_POWER_UPS = 7;
    private static final int GAME_AREA_SIZE = 1500;
    private static final float PLAYER_SPEED = .5f;
    private static final float PLAYER_TURN_SPEED = 0.04f;
    private static final float BULLET_HEIGHT = 75;

    protected GameAction fire = new GameAction("fire",
        GameAction.DETECT_INITAL_PRESS_ONLY);

    private PolygonGroup robotModel;
    private PolygonGroup powerUpModel;
    private PolygonGroup blastModel;
    private GameObjectManager gameObjectManager;
    private TexturedPolygon3D floor;

    public void init() {
        init(LOW_RES_MODES);

        inputManager.mapToKey(fire, KeyEvent.VK_SPACE);
        inputManager.mapToMouse(fire, InputManager.MOUSE_BUTTON_1);
    }

    public void createPolygons() {

        // create floor
        Texture floorTexture = Texture.createTexture(
            "../images/roof1.png", true);
        ((ShadedTexture)floorTexture).setDefaultShadeLevel(
            ShadedTexture.MAX_LEVEL*3/4);
        Rectangle3D floorTextureBounds = new Rectangle3D(
            new Vector3D(0,0,0),
            new Vector3D(1,0,0),
            new Vector3D(0,0,1),
            floorTexture.getWidth(),
            floorTexture.getHeight());
        float s = GAME_AREA_SIZE;
        floor = new TexturedPolygon3D(new Vector3D[] {
            new Vector3D(-s, 0, s),
            new Vector3D(s, 0, s),
            new Vector3D(s, 0, -s),
            new Vector3D(-s, 0, -s)});
        floor.setTexture(floorTexture, floorTextureBounds);


        // set up the local lights for the model.
        float ambientLightIntensity = .5f;
        List lights = new LinkedList();
        lights.add(new PointLight3D(-100,100,100, .5f, -1));
        lights.add(new PointLight3D(100,100,0, .5f, -1));

        // load the object models
        ObjectLoader loader = new ObjectLoader();
        loader.setLights(lights, ambientLightIntensity);
        try {
            robotModel = loader.loadObject("../images/robot.obj");
            powerUpModel = loader.loadObject("../images/cube.obj");
            blastModel = loader.loadObject("../images/blast.obj");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        // create game objects
        gameObjectManager = new SimpleGameObjectManager();
        gameObjectManager.addPlayer(new GameObject(
            new PolygonGroup("Player")));
        gameObjectManager.getPlayer().getLocation().y = 5;
        for (int i=0; i<NUM_BOTS; i++) {
            Bot object = new Bot((PolygonGroup)robotModel.clone());
            placeObject(object);
        }
        for (int i=0; i<NUM_POWER_UPS; i++) {
            GameObject object =
                new GameObject((PolygonGroup)powerUpModel.clone());
            placeObject(object);
        }
    }

    // randomly place objects in game area
    public void placeObject(GameObject object) {
        float size = GAME_AREA_SIZE;
        object.getLocation().setTo(
            (float)(Math.random()*size-size/2),
            0,
            (float)(Math.random()*size-size/2));
        gameObjectManager.add(object);
    }

    public void createPolygonRenderer() {
        viewWindow = new ViewWindow(0, 0,
            screen.getWidth(), screen.getHeight(),
            (float)Math.toRadians(75));

        Transform3D camera = new Transform3D();
        polygonRenderer = new ZBufferedRenderer(
            camera, viewWindow);
    }

    public void updateWorld(long elapsedTime) {

        float angleVelocity;

        // cap elapsedTime
        elapsedTime = Math.min(elapsedTime, 100);

        GameObject player = gameObjectManager.getPlayer();
        MovingTransform3D playerTransform = player.getTransform();
        Vector3D velocity = playerTransform.getVelocity();

        playerTransform.stop();
        float x = -playerTransform.getSinAngleY();
        float z = -playerTransform.getCosAngleY();
        if (goForward.isPressed()) {
            velocity.add(x, 0, z);
        }
        if (goBackward.isPressed()) {
            velocity.add(-x, 0, -z);
        }
        if (goLeft.isPressed()) {
            velocity.add(z, 0, -x);
        }
        if (goRight.isPressed()) {
            velocity.add(-z, 0, x);
        }
        if (fire.isPressed()) {
            float cosX = playerTransform.getCosAngleX();
            float sinX = playerTransform.getSinAngleX();
            Blast blast = new Blast(
                (PolygonGroup)blastModel.clone(),
                new Vector3D(cosX*x, sinX, cosX*z));
            // blast starting location needs work. looks like
            // the blast is coming out of your forehead when
            // you're shooting down.
            blast.getLocation().setTo(
                player.getX(),
                player.getY() + BULLET_HEIGHT,
                player.getZ());
            gameObjectManager.add(blast);
        }


        velocity.multiply(PLAYER_SPEED);
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

        // for now, mark the entire world as visible in this frame.
        gameObjectManager.markAllVisible();

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
        camera.getLocation().add(0,100,0);

    }


    public void draw(Graphics2D g) {

        polygonRenderer.startFrame(g);

        // draw floor
        polygonRenderer.draw(g, floor);

        // draw objects
        gameObjectManager.draw(g,
            (GameObjectRenderer)polygonRenderer);

        polygonRenderer.endFrame(g);

        super.drawText(g);
    }

}