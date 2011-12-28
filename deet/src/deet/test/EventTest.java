package deet.test;
import java.util.*;

import deet.game.*;
import deet.game.shooter3D.ShooterCore;

import deet.bsp2D.*;
import deet.math3D.*;
import deet.object.GameObject;
import deet.path.*;
import deet.scripting.*;
import deet.game.shooter3D.*;

public class EventTest extends ShooterCore {

    public static void main(String[] args) {
        new EventTest(args, "../images/level1.map").run();
    }

    protected GameTaskManager gameTaskManager;
    protected ScriptManager scriptManager;

    public EventTest(String[] args, String defaultMap) {
        super(args, defaultMap);
    }

    public void init() {
        super.init();
        gameTaskManager = new GameTaskManager();
        scriptManager = new ScriptManager();
        scriptManager.setupLevel(gameObjectManager,
            gameTaskManager, new String[] {
            "../scripts/main.bsh", "../scripts/level1.bsh" });
    }

    protected void createGameObjects(List mapObjects) {

        Iterator i = mapObjects.iterator();
        while (i.hasNext()) {
            Object object = i.next();
            if (object instanceof PolygonGroup) {
                PolygonGroup group = (PolygonGroup)object;
                if ("toy2".equals(group.getName())) {
                    gameObjectManager.add(new PathBot(group));
                }
                else {
                    gameObjectManager.add(new GameObject(group));
                }
            }
            else if (object instanceof GameObject) {
                gameObjectManager.add((GameObject)object);
            }

        }
    }

    public void updateWorld(long elapsedTime) {
        super.updateWorld(elapsedTime);
        gameTaskManager.update(elapsedTime);

    }




}
