package com.brackeen.javagamebook.bsp2D;

import java.util.List;
import java.util.ArrayList;
import com.brackeen.javagamebook.math3D.*;

/**
    The BSPTreeBuilderWithPortals class builds a BSP tree
    and adds portals to the leaves of the tree.

    Note that the portals aren't optimized. For example,
    adjacent collinear portals aren't merged, and "useless"
    portals aren't removed.
*/
public class BSPTreeBuilderWithPortals extends BSPTreeBuilder {


    /**
        Builds a BSP tree and adds portals to the leaves.
    */
    public BSPTree build(List polygons) {
        super.build(polygons);
        findPortalsOfLeaves(currentTree.getRoot());
        return currentTree;
    }


    /**
        Finds all the portals of the leaves of the specified node.
    */
    protected void findPortalsOfLeaves(BSPTree.Node node) {
        if (node instanceof BSPTree.Leaf) {
            findPortals((BSPTree.Leaf)node);
        }
        else {
            findPortalsOfLeaves(node.front);
            findPortalsOfLeaves(node.back);
        }
    }


    /**
        Finds all the portals of the specified leaf.
    */
    protected void findPortals(BSPTree.Leaf leaf) {
        ArrayList lines = new ArrayList();
        leaf.portals = new ArrayList();
        for (int i=0; i<leaf.polygons.size(); i++) {
            Polygon3D poly = (Polygon3D)leaf.polygons.get(i);
            for (int j=0; j<poly.getNumVertices(); j++) {
                int next = (j+1) % poly.getNumVertices();
                Vector3D v1 = poly.getVertex(j);
                Vector3D v2 = poly.getVertex(next);
                BSPLine line = new BSPLine(v1.x, v1.z, v2.x, v2.z);

                // check to see if line was already checked
                boolean checked = false;
                for (int k=0; !checked && k<lines.size(); k++) {
                    if (line.equalsIgnoreOrder(
                        (BSPLine)lines.get(k)))
                    {
                        checked = true;
                    }
                }

                // create the portal
                if (!checked) {
                    lines.add(line);
                    Portal portal = createPortal(line);
                    if (portal != null) {
                        leaf.portals.add(portal);
                    }
                }
            }
        }
        ((ArrayList)leaf.portals).trimToSize();
    }


    /**
        Creates a portal for the specified line segment. Returns
        null if no portal could be created (if the line represents
        a solid wall or the line isn't found).
    */
    protected Portal createPortal(BSPLine line) {
        BSPTree.Node node = currentTree.getCollinearNode(line);
        if (node != null && node.polygons != null) {
            for (int i=0; i<node.polygons.size(); i++) {
                BSPPolygon poly = (BSPPolygon)node.polygons.get(i);
                if (poly.isSolidWall() &&
                    line.equalsIgnoreOrder(poly.getLine()))
                {
                    // wall not passable
                    return null;
                }
            }
        }

        BSPTree.Leaf frontLeaf = currentTree.getFrontLeaf(line);
        BSPTree.Leaf backLeaf = currentTree.getBackLeaf(line);
        if (frontLeaf != null && backLeaf != null &&
            frontLeaf != backLeaf && frontLeaf.bounds != null &&
            backLeaf.bounds != null)
        {
            return new Portal(line, frontLeaf, backLeaf);
        }
        else {
            return null;
        }

    }
}
