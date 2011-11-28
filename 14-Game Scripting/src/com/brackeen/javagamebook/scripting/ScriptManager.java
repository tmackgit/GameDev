package com.brackeen.javagamebook.scripting;

import java.io.IOException;
import java.util.*;
import com.brackeen.javagamebook.game.*;
import bsh.*;

/**
    The ScriptManager class handles the invoking of scripted
    methods. Scripted methods can add functionality to different
    GameObject notifications for each unique object. For example,
    the methods for collision notifications for the "player"
    object are:

    <pre>
    playerFloorCollision()
    playerWallCollision()
    playerCeilingCollision()
    </pre>
    Likewise, if "player" collides with the "box" object, these
    methods are called:

    <pre>
    player_boxCollision()
    player_boxTouch()
    player_boxRelease()
    </pre>

    Also, the initLevel() method is called on startup.

*/
public class ScriptManager {

    /**
        A GameObjectEventListener that delgates calls to scripted
        methods. A ScriptedListener is added to every GameObject
        that has at least one scripted method.
    */
    public class ScriptedListener
        implements GameObjectEventListener
    {

        public void notifyVisible(GameObject object,
            boolean visible)
        {
            invoke(object.getName() +
                (visible?"Visible":"NotVisible"));
        }

        public void notifyObjectCollision(GameObject object,
            GameObject otherObject)
        {
            if (otherObject.getName() != null) {
                invoke(object.getName() + "_" +
                    otherObject.getName() + "Collision");
            }

        }

        public void notifyObjectTouch(GameObject object,
            GameObject otherObject)
        {
            if (otherObject.getName() != null) {
                invoke(object.getName() + "_" +
                    otherObject.getName() + "Touch");
            }

        }

        public void notifyObjectRelease(GameObject object,
            GameObject otherObject)
        {
            if (otherObject.getName() != null) {
                invoke(object.getName() + "_" +
                    otherObject.getName() + "Release");
            }

        }

        public void notifyFloorCollision(GameObject object) {
            invoke(object.getName() + "FloorCollision");
        }

        public void notifyCeilingCollision(GameObject object) {
            invoke(object.getName() +"CeilingCollision");
        }

        public void notifyWallCollision(GameObject object) {
            invoke(object.getName() + "WallCollision");
        }
    }

    private static final Class[] NO_ARGS = new Class[0];

    private Interpreter bsh;
    private GameObjectEventListener scriptedListener;

    public ScriptManager() {
         scriptedListener = new ScriptedListener();
    }


    /**
        Sets up the ScriptManager for a level. The list of
        script files are executed, and every object in the
        GameObjectManager that has a name are added as named
        variables for the scripts.
        Also, the scripted method initLevel() is called if
        it exists.
    */
    public void setupLevel(GameObjectManager gameObjectManager,
        GameTaskManager gameTaskManager, String[] scriptFiles)
    {
        bsh = new Interpreter();
        try {
            // execute source files (and load methods).
            for (int i=0; i<scriptFiles.length; i++) {
                bsh.source(scriptFiles[i]);
            }

            bsh.set("gameTaskManager", gameTaskManager);

            // Treat all named objects as named variables in
            // beanshell.
            // NOTE: this keeps all objects in memory (live) until
            // next level, even if they are destroyed during the
            // level gameplay.
            Iterator i = gameObjectManager.iterator();
            while (i.hasNext()) {
                GameObject object = (GameObject)i.next();
                if (object.getName() != null) {
                    bsh.set(object.getName(), object);

                    // add scripted listener to object
                    if (hasScripts(object)) {
                        object.addListener(scriptedListener);
                    }
                }
            }

            // init level code - call initLevel()
            invoke("initLevel");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (EvalError ex) {
            ex.printStackTrace();
        }

    }


    /**
        Checks to see if the specified game object has any
        scripts (check to see if any scripted method starts with
        the object's name).
    */
    public boolean hasScripts(GameObject object) {
        if (object.getName() != null) {
            String[] names = bsh.getNameSpace().getMethodNames();
            for (int i=0; i<names.length; i++) {
                if (names[i].startsWith(object.getName())) {
                    return true;
                }
            }
        }

        // none found
        return false;
    }


    /**
        Returns true if the specified method name is an exsting
        scripted method.
    */
    public boolean isMethod(String methodName) {
        return (bsh.getNameSpace().
            getMethod(methodName, NO_ARGS) != null);
    }


    /**
        Invokes the specified scripted method.
    */
    public void invoke(String methodName) {
        if (isMethod(methodName)) {
            try {
                bsh.eval(methodName + "()");
            }
            catch (EvalError e) {
                e.printStackTrace();
            }
        }
    }

}
