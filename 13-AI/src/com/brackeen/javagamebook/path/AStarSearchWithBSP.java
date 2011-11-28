package com.brackeen.javagamebook.path;

import java.util.*;
import com.brackeen.javagamebook.game.GameObject;
import com.brackeen.javagamebook.math3D.Vector3D;
import com.brackeen.javagamebook.bsp2D.BSPTree;
import com.brackeen.javagamebook.bsp2D.Portal;

/**
    The AStarSearchWithBSP class is a PathFinder that finds
    a path in a BSP tree using an A* search algorithm.
*/
public class AStarSearchWithBSP extends AStarSearch
    implements PathFinder
{

    /**
        The LeafNode class is an AStarNode that repesents a
        location in a leaf of a BSP tree. Used for the start
        and goal nodes of a search.
    */
    public static class LeafNode extends AStarNode {
        BSPTree.Leaf leaf;
        Vector3D location;

        public LeafNode(BSPTree.Leaf leaf, Vector3D location) {
            this.leaf = leaf;
            this.location = location;
        }

        public float getCost(AStarNode node) {
            return getEstimatedCost(node);
        }

        public float getEstimatedCost(AStarNode node) {
            float otherX;
            float otherZ;
            if (node instanceof Portal) {
                Portal other = (Portal)node;
                otherX = other.getMidPoint().x;
                otherZ = other.getMidPoint().z;
            }
            else {
                LeafNode other = (LeafNode)node;
                otherX = other.location.x;
                otherZ = other.location.z;
            }
            float dx = location.x - otherX;
            float dz = location.z - otherZ;
            return (float)Math.sqrt(dx * dx + dz * dz);
        }

        public List getNeighbors() {
            return leaf.portals;
        }
    }

    private BSPTree bspTree;


    /**
        Creates a new AStarSearchWithBSP for the specified
        BSP tree.
    */
    public AStarSearchWithBSP(BSPTree bspTree) {
        setBSPTree(bspTree);
    }

    public void setBSPTree(BSPTree bspTree) {
        this.bspTree = bspTree;
    }


    public Iterator find(GameObject a, GameObject b) {
        return find(a.getLocation(), b.getLocation());
    }


    public Iterator find(Vector3D start, Vector3D goal) {

        BSPTree.Leaf startLeaf = bspTree.getLeaf(start.x, start.z);
        BSPTree.Leaf goalLeaf = bspTree.getLeaf(goal.x, goal.z);

        // if start and goal is in the same leaf, no need to do
        // A* search
        if (startLeaf == goalLeaf) {
            return Collections.singleton(goal).iterator();
        }

        AStarNode startNode = new LeafNode(startLeaf, start);
        AStarNode goalNode = new LeafNode(goalLeaf, goal);

        // temporarily add the goalNode we just created to
        // the neighbors list
        List goalNeighbors = goalNode.getNeighbors();
        for (int i=0; i<goalNeighbors.size(); i++) {
            Portal portal = (Portal)goalNeighbors.get(i);
            portal.addNeighbor(goalNode);
        }

        // do A* search
        List path = super.findPath(startNode, goalNode);

        // remove the goal node from the neighbors list
        for (int i=0; i<goalNeighbors.size(); i++) {
            Portal portal = (Portal)goalNeighbors.get(i);
            portal.removeNeighbor(goalNode);
        }

        return convertPath(path);
    }


    /**
        Converts path of AStarNodes to a path of Vector3D
        locations.
    */
    protected Iterator convertPath(List path) {
        if (path == null) {
            return null;
        }
        for (int i=0; i<path.size(); i++) {
            Object node = path.get(i);
            if (node instanceof Portal) {
                path.set(i, ((Portal)node).getMidPoint());
            }
            else {
                path.set(i, ((LeafNode)node).location);
            }
        }
        return Collections.unmodifiableList(path).iterator();
    }

    public String toString() {
        return "AStarSearchWithBSP";
    }
}
