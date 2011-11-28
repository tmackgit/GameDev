package com.brackeen.javagamebook.bsp2D;

import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.GameObjectManager;

/**
    A BSPTreeTraverer traverses a 2D BSP tree either with a
    in-order or draw-order (front-to-back) order. Visited
    polygons are signaled using a BSPTreeTraverseListener.
*/
public class BSPTreeTraverser {

    private boolean traversing;
    private float x;
    private float z;
    private GameObjectManager objectManager;
    private BSPTreeTraverseListener listener;

    /**
        Creates a new BSPTreeTraverser with no
        BSPTreeTraverseListener.
    */
    public BSPTreeTraverser() {
        this(null);
    }


    /**
        Creates a new BSPTreeTraverser with the specified
        BSPTreeTraverseListener.
    */
    public BSPTreeTraverser(BSPTreeTraverseListener listener) {
        setListener(listener);
    }


    /**
        Sets the BSPTreeTraverseListener to use during traversals.
    */
    public void setListener(BSPTreeTraverseListener listener) {
        this.listener = listener;
    }


    /**
        Sets the GameObjectManager. If the GameObjectManager is
        not null during traversal, then the manager's markVisible()
        method is called to specify visible parts of the tree.
    */
    public void setGameObjectManager(
        GameObjectManager objectManager)
    {
        this.objectManager = objectManager;
    }


    /**
        Traverses a tree in draw-order (front-to-back) using
        the specified view location.
    */
    public void traverse(BSPTree tree, Vector3D viewLocation) {
        x = viewLocation.x;
        z = viewLocation.z;
        traversing = true;
        traverseDrawOrder(tree.getRoot());
    }


    /**
        Traverses a tree in in-order.
    */
    public void traverse(BSPTree tree) {
        traversing = true;
        traverseInOrder(tree.getRoot());
    }


    /**
        Traverses a node in draw-order (front-to-back) using
        the current view location.
    */
    private void traverseDrawOrder(BSPTree.Node node) {
        if (traversing && node != null) {
            if (node instanceof BSPTree.Leaf) {
                // no partition, just handle polygons
                visitNode(node);
            }
            else if (node.partition.getSideThin(x,z) != BSPLine.BACK) {
                traverseDrawOrder(node.front);
                visitNode(node);
                traverseDrawOrder(node.back);
            }
            else {
                traverseDrawOrder(node.back);
                visitNode(node);
                traverseDrawOrder(node.front);
            }
        }

    }


    /**
        Traverses a node in in-order.
    */
    private void traverseInOrder(BSPTree.Node node) {
        if (traversing && node != null) {
            traverseInOrder(node.front);
            visitNode(node);
            traverseInOrder(node.back);
        }
    }


    /**
        Visits a node in the tree. The BSPTreeTraverseListener's
        visitPolygon() method is called for every polygon in
        the node.
    */
    private void visitNode(BSPTree.Node node) {
        if (!traversing || node.polygons == null) {
            return;
        }

        boolean isBack = false;
        if (node instanceof BSPTree.Leaf) {
            BSPTree.Leaf leaf = (BSPTree.Leaf)node;
            isBack = leaf.isBack;
            // mark the bounds of this leaf as visible in
            // the game object manager.
            if (objectManager != null && leaf.bounds != null) {
                objectManager.markVisible(leaf.bounds);
            }
        }

        // visit every polygon
        for (int i=0; traversing && i<node.polygons.size(); i++) {
            BSPPolygon poly = (BSPPolygon)node.polygons.get(i);
            traversing = listener.visitPolygon(poly, isBack);
        }
    }

}
