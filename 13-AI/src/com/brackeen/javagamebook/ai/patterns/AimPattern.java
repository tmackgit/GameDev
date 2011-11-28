package com.brackeen.javagamebook.ai.pattern;

import java.util.Iterator;
import java.util.Collections;
import com.brackeen.javagamebook.game.GameObject;
import com.brackeen.javagamebook.math3D.Vector3D;
import com.brackeen.javagamebook.bsp2D.BSPTree;
import com.brackeen.javagamebook.util.MoreMath;

/**
    Direct aim at the player, with a random offset. Aim patterns
    return a direction to fire, rather than a location.
*/
public class AimPattern extends AIPattern {

    protected float accuracy;

    public AimPattern(BSPTree tree) {
        super(tree);
    }

    /**
        Sets the accuracy of the aim from 0 (worst) to 1 (best).
    */
    public void setAccuracy(float p) {
        this.accuracy = p;
    }


    public Iterator find(GameObject bot, GameObject player) {
        Vector3D goal = new Vector3D(player.getLocation());
        goal.y += player.getBounds().getTopHeight() / 2;
        goal.subtract(bot.getLocation());

        // Rotate up to 10 degrees off y-axis
        // (This could use an up/down random offset as well.)
        if (accuracy < 1) {
            float maxAngle = 10 * (1-accuracy);
            float angle = MoreMath.random(-maxAngle, maxAngle);
            goal.rotateY((float)Math.toRadians(angle));
        }

        goal.normalize();

        // return an iterator
        return Collections.singleton(goal).iterator();
    }
}
