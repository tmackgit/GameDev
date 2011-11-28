package com.brackeen.javagamebook.math3D;


/**
    The PolygonGroupBounds represents a cylinder bounds around a
    PolygonGroup that can be used for collision detection.
*/
public class PolygonGroupBounds {

    private float topHeight;
    private float bottomHeight;
    private float radius;


    /**
        Creates a new PolygonGroupBounds with no bounds.
    */
    public PolygonGroupBounds() {

    }


    /**
        Creates a new PolygonGroupBounds with the bounds of
        the specified PolygonGroup.
    */
    public PolygonGroupBounds(PolygonGroup group) {
        setToBounds(group);
    }


    /**
        Sets this to the bounds of the specified PolygonGroup.
    */
    public void setToBounds(PolygonGroup group) {
        topHeight = Float.MIN_VALUE;
        bottomHeight = Float.MAX_VALUE;
        radius = 0;

        group.resetIterator();
        while (group.hasNext()) {
            Polygon3D poly = group.nextPolygon();
            for (int i=0; i<poly.getNumVertices(); i++) {
                Vector3D v = poly.getVertex(i);
                topHeight = Math.max(topHeight, v.y);
                bottomHeight = Math.min(bottomHeight, v.y);
                // compute radius squared
                radius = Math.max(radius, v.x*v.x + v.z*v.z);
            }
        }

        if (radius == 0) {
            // empty polygon group!
            topHeight = 0;
            bottomHeight = 0;
        }
        else {
            radius = (float)Math.sqrt(radius);
        }
    }

    public float getTopHeight() {
        return topHeight;
    }

    public void setTopHeight(float topHeight) {
        this.topHeight = topHeight;
    }

    public float getBottomHeight() {
        return bottomHeight;
    }

    public void setBottomHeight(float bottomHeight) {
        this.bottomHeight = bottomHeight;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }


}
