package com.brackeen.javagamebook.math3D;

import java.awt.Color;

/**
    The SolidPolygon3D class is a Polygon with a color.
*/
public class SolidPolygon3D extends Polygon3D {

    private Color color = Color.GREEN;

    public SolidPolygon3D() {
        super();
    }


    public SolidPolygon3D(Vector3D v0, Vector3D v1, Vector3D v2) {
        this(new Vector3D[] { v0, v1, v2 });
    }


    public SolidPolygon3D(Vector3D v0, Vector3D v1, Vector3D v2,
        Vector3D v3)
    {
        this(new Vector3D[] { v0, v1, v2, v3 });
    }


    public SolidPolygon3D(Vector3D[] vertices) {
        super(vertices);
    }


    public void setTo(Polygon3D polygon) {
        super.setTo(polygon);
        if (polygon instanceof SolidPolygon3D) {
            color = ((SolidPolygon3D)polygon).color;
        }
    }


    /**
        Gets the color of this solid-colored polygon used for
        rendering this polygon.
    */
    public Color getColor() {
        return color;
    }


    /**
        Sets the color of this solid-colored polygon used for
        rendering this polygon.
    */
    public void setColor(Color color) {
        this.color = color;
    }

}
