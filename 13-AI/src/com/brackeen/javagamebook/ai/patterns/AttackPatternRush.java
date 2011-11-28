package com.brackeen.javagamebook.ai.pattern;

import java.util.Iterator;
import java.util.Collections;
import com.brackeen.javagamebook.game.GameObject;
import com.brackeen.javagamebook.math3D.Vector3D;
import com.brackeen.javagamebook.bsp2D.BSPTree;

/**
    Simple "attack" pattern to rush the player - get within
    a certain distance of the player and stop.
*/
public class AttackPatternRush extends AIPattern {

    private float desiredDistSq;

    public AttackPatternRush(BSPTree tree) {
        this(tree, 200);
    }

    public AttackPatternRush(BSPTree tree, float desiredDist) {
        super(tree);
        this.desiredDistSq = desiredDist * desiredDist;
    }

    public Iterator find(GameObject bot, GameObject player) {
        Vector3D goal = getLocationFromPlayer(bot, player,
            desiredDistSq);
        if (goal.equals(bot.getLocation())) {
            return null;
        }
        else {
            return Collections.singleton(goal).iterator();
        }
    }

}
