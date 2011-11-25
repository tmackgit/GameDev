package com.brackeen.javagamebook.math3D;

import java.util.List;
import java.util.ArrayList;

/**
    The PolygonGroup is a group of polygons with a
    MovingTransform3D. PolygonGroups can also contain other
    PolygonGroups.
*/
public class PolygonGroup implements Transformable {

    private String name;
    private String filename;
    private List objects;
    private MovingTransform3D transform;
    private int iteratorIndex;

    /**
        Creates a new, empty PolygonGroup.
    */
    public PolygonGroup() {
        this("unnamed");
    }


    /**
        Creates a new, empty PolygonGroup with te specified name.
    */
    public PolygonGroup(String name) {
        setName(name);
        objects = new ArrayList();
        transform = new MovingTransform3D();
        iteratorIndex = 0;
    }


    /**
        Gets the MovingTransform3D for this PolygonGroup.
    */
    public MovingTransform3D getTransform() {
        return transform;
    }


    /**
        Gets the name of this PolygonGroup.
    */
    public String getName() {
        return name;
    }


    /**
        Sets the name of this PolygonGroup.
    */
    public void setName(String name) {
        this.name = name;
    }


    /**
        Gets the filename of this PolygonGroup.
    */
    public String getFilename() {
        return filename;
    }


    /**
        Sets the filename of this PolygonGroup.
    */
    public void setFilename(String filename) {
        this.filename = filename;
    }


    /**
        Adds a polygon to this group.
    */
    public void addPolygon(Polygon3D o) {
        objects.add(o);
    }


    /**
        Adds a PolygonGroup to this group.
    */
    public void addPolygonGroup(PolygonGroup p) {
        objects.add(p);
    }


    /**
        Clones this polygon group. Polygon3Ds are shared between
        this group and the cloned group; Transform3Ds are copied.
    */
    public Object clone() {
        PolygonGroup group = new PolygonGroup(name);
        group.setFilename(filename);
        for (int i=0; i<objects.size(); i++) {
            Object obj = objects.get(i);
            if (obj instanceof Polygon3D) {
                group.addPolygon((Polygon3D)obj);
            }
            else {
                PolygonGroup grp = (PolygonGroup)obj;
                group.addPolygonGroup((PolygonGroup)grp.clone());
            }
        }
        group.transform = (MovingTransform3D)transform.clone();
        return group;
    }


    /**
        Gets the PolygonGroup in this group with the specified
        name, or null if none found.
    */
    public PolygonGroup getGroup(String name) {
        // check for this group
        if (this.name != null && this.name.equals(name)) {
            return this;
        }
        for (int i=0; i<objects.size(); i++) {
            Object obj = objects.get(i);
            if (obj instanceof PolygonGroup) {
                PolygonGroup subgroup =
                    ((PolygonGroup)obj).getGroup(name);
                if (subgroup != null) {
                    return subgroup;
                }
            }
        }

        // group not found
        return null;
    }


    /**
        Resets the polygon iterator for this group.
        @see #hasNext
        @see #nextPolygon
    */
    public void resetIterator() {
        iteratorIndex = 0;
        for (int i=0; i<objects.size(); i++) {
            Object obj = objects.get(i);
            if (obj instanceof PolygonGroup) {
                ((PolygonGroup)obj).resetIterator();
            }
        }
    }


    /**
        Checks if there is another polygon in the current
        iteration.
        @see #resetIterator
        @see #nextPolygon
    */
    public boolean hasNext() {
        return (iteratorIndex < objects.size());
    }


    /**
        Gets the next polygon in the current iteration.
        @see #resetIterator
        @see #hasNext
    */
    public Polygon3D nextPolygon() {
        Object obj = objects.get(iteratorIndex);

        if (obj instanceof PolygonGroup) {
            PolygonGroup group = (PolygonGroup)obj;
            Polygon3D poly = group.nextPolygon();
            if (!group.hasNext()) {
                iteratorIndex++;
            }
            return poly;
        }
        else {
            iteratorIndex++;
            return (Polygon3D)obj;
        }
    }


    /**
        Gets the next polygon in the current iteration, applying
        the MovingTransform3Ds to it, and storing it in 'cache'.
    */
    public void nextPolygonTransformed(Polygon3D cache) {
        Object obj = objects.get(iteratorIndex);

        if (obj instanceof PolygonGroup) {
            PolygonGroup group = (PolygonGroup)obj;
            group.nextPolygonTransformed(cache);
            if (!group.hasNext()) {
                iteratorIndex++;
            }
        }
        else {
            iteratorIndex++;
            cache.setTo((Polygon3D)obj);
        }

        cache.add(transform);
    }


    /**
        Updates the MovingTransform3Ds of this group and any
        subgroups.
    */
    public void update(long elapsedTime) {
        transform.update(elapsedTime);
        for (int i=0; i<objects.size(); i++) {
            Object obj = objects.get(i);
            if (obj instanceof PolygonGroup) {
                PolygonGroup group = (PolygonGroup)obj;
                group.update(elapsedTime);
            }
        }
    }

    // from the Transformable interface

    public void add(Vector3D u) {
        transform.getLocation().add(u);
    }

    public void subtract(Vector3D u) {
        transform.getLocation().subtract(u);
    }

    public void add(Transform3D xform) {
        addRotation(xform);
        add(xform.getLocation());
    }

    public void subtract(Transform3D xform) {
        subtract(xform.getLocation());
        subtractRotation(xform);
    }

    public void addRotation(Transform3D xform) {
        transform.rotateAngleX(xform.getAngleX());
        transform.rotateAngleY(xform.getAngleY());
        transform.rotateAngleZ(xform.getAngleZ());
    }

    public void subtractRotation(Transform3D xform) {
        transform.rotateAngleX(-xform.getAngleX());
        transform.rotateAngleY(-xform.getAngleY());
        transform.rotateAngleZ(-xform.getAngleZ());
    }

}
