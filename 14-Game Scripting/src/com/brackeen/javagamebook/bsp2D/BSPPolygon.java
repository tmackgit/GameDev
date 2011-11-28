package com.brackeen.javagamebook.bsp2D;

import com.brackeen.javagamebook.math3D.*;

/**
    A BSPPolygon is a TexturedPolygon3D with a type
    (TYPE_FLOOR, TYPE_WALL, or TYPE_PASSABLE_WALL) an
    ambient light intensity value, and a BSPLine representation
    if the type is a TYPE_WALL or TYPE_PASSABLE_WALL.
*/
public class BSPPolygon extends TexturedPolygon3D {

    public static final int TYPE_FLOOR = 0;
    public static final int TYPE_WALL = 1;
    public static final int TYPE_PASSABLE_WALL = 2;

    /**
        How short a wall must be so that monsters/players can
        step over it.
    */
    public static final int PASSABLE_WALL_THRESHOLD = 32;

    /**
        How tall an entryway must be so that monsters/players can
        pass through it
    */
    public static final int PASSABLE_ENTRYWAY_THRESHOLD = 128;


    private int type;
    private float ambientLightIntensity;
    private BSPLine line;

    /**
        Creates a new BSPPolygon with the specified vertices
        and type (TYPE_FLOOR, TYPE_WALL, or TYPE_PASSABLE_WALL).
    */
    public BSPPolygon(Vector3D[] vertices, int type) {
        super(vertices);
        this.type = type;
        ambientLightIntensity = 0.5f;
        if (isWall()) {
            line = new BSPLine(this);
        }
    }


    /**
        Clone this polygon, but with a different set of vertices.
    */
    public BSPPolygon clone(Vector3D[] vertices) {
        BSPPolygon clone = new BSPPolygon(vertices, type);
        clone.setNormal(getNormal());
        clone.setAmbientLightIntensity(getAmbientLightIntensity());
        if (getTexture() != null) {
            clone.setTexture(getTexture(), getTextureBounds());
        }
        return clone;
    }


    /**
        Returns true if the BSPPolygon is a wall.
    */
    public boolean isWall() {
        return (type == TYPE_WALL) || (type == TYPE_PASSABLE_WALL);
    }


    /**
        Returns true if the BSPPolygon is a solid wall (not
        passable).
    */
    public boolean isSolidWall() {
        return type == TYPE_WALL;
    }


    /**
        Gets the line representing the BSPPolygon. Returns null if
        this BSPPolygon is not a wall.
    */
    public BSPLine getLine() {
        return line;
    }


    public void setAmbientLightIntensity(float a) {
        ambientLightIntensity = a;
    }


    public float getAmbientLightIntensity() {
        return ambientLightIntensity;
    }

}
