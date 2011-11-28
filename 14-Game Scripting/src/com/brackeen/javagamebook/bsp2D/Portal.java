package com.brackeen.javagamebook.bsp2D;


import java.util.*;
import com.brackeen.javagamebook.math3D.Vector3D;
import com.brackeen.javagamebook.path.AStarNode;

/**
    A Portal represents a passable divider between two
    leaves in a BSP tree (think: entryway between rooms).
    The Portal class is also an AStarNode, so AI creatures
    can use the A* algorithm to find paths throughout the
    BSP tree.
*/
public class Portal extends AStarNode {

    private BSPLine divider;
    private BSPTree.Leaf front;
    private BSPTree.Leaf back;
    private ArrayList neighbors;
    private Vector3D midPoint;

    /**
        Create a new Portal with the specified divider and front/
        back leaves.
    */
    public Portal(BSPLine divider, BSPTree.Leaf front,
        BSPTree.Leaf back)
    {
        this.divider = divider;
        this.front = front;
        this.back = back;
        midPoint = new Vector3D(
            (divider.x1 + divider.x2) / 2,
            Math.max(front.floorHeight, back.floorHeight),
            (divider.y1 + divider.y2) / 2);
    }


    /**
        Gets the mid-point along this Portal's divider.
    */
    public Vector3D getMidPoint() {
        return midPoint;
    }


    /**
        Builds the list of neighbors for the AStarNode
        representation. The neighbors are the portals of the
        front and back leaves, not including this portal.
    */
    public void buildNeighborList() {
        neighbors = new ArrayList();
        if (front != null) {
            neighbors.addAll(front.portals);
        }
        if (back != null) {
            neighbors.addAll(back.portals);
        }

        // trim to size, then remove references to this node.
        // (ensures extra capacity for calls to addNeighbor()
        // without enlarging the array capacity)
        neighbors.trimToSize();
        while (neighbors.remove(this));
    }


    /**
        Adds a neighbor node to the list of neighbors.
    */
    public void addNeighbor(AStarNode node) {
        if (neighbors == null) {
            buildNeighborList();
        }
        neighbors.add(node);
    }


    /**
        Removes a neighbor node to the list of neighbors.
    */
    public void removeNeighbor(AStarNode node) {
        if (neighbors == null) {
            buildNeighborList();
        }
        neighbors.remove(node);
    }

    // AStarNode methods

    public float getCost(AStarNode node) {
        return getEstimatedCost(node);
    }


    public float getEstimatedCost(AStarNode node) {
        if (node instanceof Portal) {
            Portal other = (Portal)node;
            float dx = midPoint.x - other.midPoint.x;
            float dz = midPoint.z - other.midPoint.z;
            return (float)Math.sqrt(dx * dx + dz * dz);
        }
        else {
            return node.getEstimatedCost(this);
        }
    }


    public List getNeighbors() {
        if (neighbors == null) {
            buildNeighborList();
        }
        return neighbors;
    }

}