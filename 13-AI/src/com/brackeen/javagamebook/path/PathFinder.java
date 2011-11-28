package com.brackeen.javagamebook.path;

import java.util.Iterator;
import com.brackeen.javagamebook.game.GameObject;
import com.brackeen.javagamebook.math3D.Vector3D;

/**
    The PathFinder interface is a function that finds a path
    (represented by a List of Vector3Ds) from one location to
    another, or from one GameObject to another. Note that the
    find() method can ignore the requested goal, and instead
    give an arbitrary path, like patrolling in a set path or
    running away from the goal.
*/
public interface PathFinder {

    /**
        Finds a path from GameObject A to GameObject B. The path
        is an Iterator of Vector3Ds, not including the start
        location (GameObject A) but including the goal location
        (GameObject B). The Vector3D objects may be used in
        other objects and should not be modified.
        Returns null if no path found.
    */
    public Iterator find(GameObject a, GameObject b);


    /**
        Finds a path from the start location to the goal
        location. The path is an Iterator of Vector3Ds, not
        including the start location, but including the goal
        location. The Vector3D objects may be used in other
        objects and should not be modified. Returns null if no
        path found.
    */
    public Iterator find(Vector3D start, Vector3D goal);
}
