package com.brackeen.javagamebook.ai.pattern;

import java.util.*;
import com.brackeen.javagamebook.game.GameObject;
import com.brackeen.javagamebook.math3D.Vector3D;
import com.brackeen.javagamebook.bsp2D.BSPTree;
import com.brackeen.javagamebook.util.MoreMath;

/**
    An "attack" pattern to strafe around the player in a
    circle with the specified radius from the player.
*/
public class AttackPatternStrafe extends AIPattern {

    private float radiusSq;

    public AttackPatternStrafe(BSPTree tree) {
        this(tree, 250);
    }

    public AttackPatternStrafe(BSPTree tree, float radius) {
        super(tree);
        this.radiusSq = radius * radius;
    }


    public Iterator find(GameObject bot, GameObject player) {

        List path = new ArrayList();

        // find first location within desired radius
        Vector3D firstGoal = getLocationFromPlayer(bot, player,
            radiusSq);
        if (!firstGoal.equals(bot.getLocation())) {
            path.add(firstGoal);
        }

        // make a counter-clockwise circle around the player
        // (since circle movement is not available, it's actually
        // an octagon).
        int numPoints = 8;
        float angle = (float)(2 * Math.PI / numPoints);
        if (MoreMath.chance(.5f)) {
            angle*=-1;
        }
        float lastY = bot.getFloorHeight();
        for (int i=1; i<numPoints; i++) {
            Vector3D goal = new Vector3D(firstGoal);
            goal.subtract(player.getLocation());
            goal.rotateY(angle * i);
            goal.add(player.getLocation());
            calcFloorHeight(goal, lastY);
            lastY = goal.y;
            path.add(goal);
        }

        // add last location (back to start)
        path.add(firstGoal);


        return path.iterator();
    }
}
