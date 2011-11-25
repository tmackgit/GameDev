package com.brackeen.javagamebook.bsp2D;

import java.awt.Rectangle;
import java.awt.Point;
import java.util.List;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;

/**
    The BSPTree class represents a 2D Binary Space Partitioned
    tree of polygons. The BSPTree is built using a BSPTreeBuilder
    class, and can be travered using BSPTreeTraverser class.
*/
public class BSPTree {

    /**
        A Node of the tree. All children of the node are either
        to the front of back of the node's partition.
    */
    public static class Node {
        public Node front;
        public Node back;
        public BSPLine partition;
        public List polygons;
    }


    /**
        A Leaf of the tree. A leaf has no partition or front or
        back nodes.
    */
    public static class Leaf extends Node {
        public float floorHeight;
        public float ceilHeight;
        public boolean isBack;
        public List portals;
        public Rectangle bounds;
    }

    private Node root;

    /**
        Creates a new BSPTree with the specified root node.
    */
    public BSPTree(Node root) {
       this.root = root;
    }


    /**
        Gets the root node of this tree.
    */
    public Node getRoot() {
        return root;
    }


    /**
        Calculates the 2D boundary of all the polygons in this
        BSP tree. Returns a rectangle of the bounds.
    */
    public Rectangle calcBounds() {

        final Point min =
            new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        final Point max =
            new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

        BSPTreeTraverser traverser = new BSPTreeTraverser();
        traverser.setListener(new BSPTreeTraverseListener() {

            public boolean visitPolygon(BSPPolygon poly,
                boolean isBack)
            {
                for (int i=0; i<poly.getNumVertices(); i++) {
                    Vector3D v = poly.getVertex(i);
                    int x = (int)Math.floor(v.x);
                    int y = (int)Math.floor(v.z);
                    min.x = Math.min(min.x, x);
                    max.x = Math.max(max.x, x);
                    min.y = Math.min(min.y, y);
                    max.y = Math.max(max.y, y);
                }

                return true;
            }
        });

        traverser.traverse(this);

        return new Rectangle(min.x, min.y,
            max.x - min.x, max.y - min.y);
    }


    /**
        Gets the leaf the x,z coordinates are in.
    */
    public Leaf getLeaf(float x, float z) {
        return getLeaf(root, x, z);
    }


    protected Leaf getLeaf(Node node, float x, float z) {
        if (node == null || node instanceof Leaf) {
            return (Leaf)node;
        }
        int side = node.partition.getSideThin(x, z);
        if (side == BSPLine.BACK) {
            return getLeaf(node.back, x, z);
        }
        else {
            return getLeaf(node.front, x, z);
        }
    }


    /**
        Gets the Node that is collinear with the specified
        partition, or null if no such node exists.
    */
    public Node getCollinearNode(BSPLine partition) {
        return getCollinearNode(root, partition);
    }


    protected Node getCollinearNode(Node node, BSPLine partition) {
        if (node == null || node instanceof Leaf) {
            return null;
        }
        int side = node.partition.getSide(partition);
        if (side == BSPLine.COLLINEAR) {
            return node;
        }
        if (side == BSPLine.FRONT) {
            return getCollinearNode(node.front, partition);
        }
        else if (side == BSPLine.BACK) {
            return getCollinearNode(node.back, partition);
        }
        else {
            // BSPLine.SPANNING: first try front, then back
            Node front = getCollinearNode(node.front, partition);
            if (front != null) {
                return front;
            }
            else {
                return getCollinearNode(node.back, partition);
            }
        }
    }


    /**
        Gets the Leaf in front of the specified partition.
    */
    public Leaf getFrontLeaf(BSPLine partition) {
        return getLeaf(root, partition, BSPLine.FRONT);
    }


    /**
        Gets the Leaf in back of the specified partition.
    */
    public Leaf getBackLeaf(BSPLine partition) {
        return getLeaf(root, partition, BSPLine.BACK);
    }


    protected Leaf getLeaf(Node node, BSPLine partition, int side)
    {
        if (node == null || node instanceof Leaf) {
            return (Leaf)node;
        }
        int segSide = node.partition.getSide(partition);
        if (segSide == BSPLine.COLLINEAR) {
            segSide = side;
        }
        if (segSide == BSPLine.FRONT) {
            return getLeaf(node.front, partition, side);
        }
        else if (segSide == BSPLine.BACK) {
            return getLeaf(node.back, partition, side);
        }
        else { // BSPLine.SPANNING
            // shouldn't happen
            return null;
        }
    }


    /**
        Creates surface textures for every polygon in this tree.
    */
    public void createSurfaces(final List lights) {
        BSPTreeTraverser traverser = new BSPTreeTraverser();
        traverser.setListener(new BSPTreeTraverseListener() {

            public boolean visitPolygon(BSPPolygon poly,
                boolean isBack)
            {
                Texture texture = poly.getTexture();
                if (texture instanceof ShadedTexture) {
                    ShadedSurface.createShadedSurface(poly,
                        (ShadedTexture)texture,
                        poly.getTextureBounds(), lights,
                        poly.getAmbientLightIntensity());
                }
                return true;
            }
        });

        traverser.traverse(this);
    }

}
