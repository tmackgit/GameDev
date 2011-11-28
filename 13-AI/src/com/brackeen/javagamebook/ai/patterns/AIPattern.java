package com.brackeen.javagamebook.ai.pattern;

import java.util.*;
import com.brackeen.javagamebook.math3D.Vector3D;
import com.brackeen.javagamebook.path.PathFinder;
import com.brackeen.javagamebook.game.GameObject;
import com.brackeen.javagamebook.bsp2D.BSPTree;

/**
    Simple abstract PathFinder that implements the
    find(Vector3D, Vector3D) method to return null.
    Unimplemented ideas: AttackPatternSneak and DodgePatternHide
*/
public abstract class AIPattern implements PathFinder {


    protected BSPTree bspTree;

    /**
        The BSP tree is used to get correct y values for the
        world.
    */
    public AIPattern(BSPTree bspTree) {
        this.bspTree = bspTree;
    }

    public void setBSPTree(BSPTree bspTree) {
        this.bspTree = bspTree;
    }

    /**
        The method isn't implemented for AIPatterns
    */
    public Iterator find(Vector3D start, Vector3D goal) {
        return null;
    }

    public abstract Iterator find(GameObject bot,
        GameObject player);


    /**
        Calculates the floor for the location specified. If
        the floor cannot be defertmined, the specified default
        value is used.
    */
    protected void calcFloorHeight(Vector3D v, float defaultY) {
       BSPTree.Leaf leaf = bspTree.getLeaf(v.x, v.z);
       if (leaf == null || leaf.floorHeight == Float.MIN_VALUE) {
           v.y = defaultY;
       }
       else {
           v.y = leaf.floorHeight;
       }
    }


    /**
        Gets the location between the player and the bot
        that is the specified distance away from the player.
    */
    protected Vector3D getLocationFromPlayer(GameObject bot,
        GameObject player, float desiredDistSq)
    {
        // get actual distance (squared)
        float distSq = bot.getLocation().
            getDistanceSq(player.getLocation());

        // if within 5 units, we're close enough
        if (Math.abs(desiredDistSq - distSq) < 25) {
            return new Vector3D(bot.getLocation());
        }

        // calculate vector to player from the bot
        Vector3D goal = new Vector3D(bot.getLocation());
        goal.subtract(player.getLocation());

        // find the goal distance from the player
        goal.multiply((float)Math.sqrt(desiredDistSq / distSq));

        goal.add(player.getLocation());
        calcFloorHeight(goal, bot.getFloorHeight());

        return goal;
    }

    public String toString() {
        // return the class name (not including the package name)
        String fullName = getClass().getName();
        int index = fullName.lastIndexOf('.');
        return fullName.substring(index+1);
    }

}
