package com.brackeen.javagamebook.math3D;

import com.brackeen.javagamebook.graphics3D.texture.Texture;

/**
    The TexturedPolygon3D class is a Polygon with a texture.
*/
public class TexturedPolygon3D extends Polygon3D {

    protected Rectangle3D textureBounds;
    protected Texture texture;

    public TexturedPolygon3D() {
        textureBounds = new Rectangle3D();
    }

    public TexturedPolygon3D(Vector3D v0, Vector3D v1,
        Vector3D v2)
    {
        this(new Vector3D[] { v0, v1, v2 });
    }

    public TexturedPolygon3D(Vector3D v0, Vector3D v1,
        Vector3D v2, Vector3D v3)
    {
        this(new Vector3D[] { v0, v1, v2, v3 });
    }

    public TexturedPolygon3D(Vector3D[] vertices) {
        super(vertices);
        textureBounds = new Rectangle3D();
    }

    public void setTo(Polygon3D poly) {
        super.setTo(poly);
        if (poly instanceof TexturedPolygon3D) {
            TexturedPolygon3D tPoly = (TexturedPolygon3D)poly;
            textureBounds.setTo(tPoly.textureBounds);
            texture = tPoly.texture;
        }
    }


    /**
        Gets this polygon's texture.
    */
    public Texture getTexture() {
        return texture;
    }


    /**
        Gets this polygon's texture bounds.
    */
    public Rectangle3D getTextureBounds() {
        return textureBounds;
    }


    /**
        Sets this polygon's texture.
    */
    public void setTexture(Texture texture) {
        this.texture = texture;
        textureBounds.setWidth(texture.getWidth());
        textureBounds.setHeight(texture.getHeight());
    }


    /**
        Sets this polygon's texture and texture bounds.
    */
    public void setTexture(Texture texture, Rectangle3D bounds) {
        setTexture(texture);
        textureBounds.setTo(bounds);
    }

    public void add(Vector3D u) {
        super.add(u);
        textureBounds.add(u);
    }

    public void subtract(Vector3D u) {
        super.subtract(u);
        textureBounds.subtract(u);
    }

    public void addRotation(Transform3D xform) {
        super.addRotation(xform);
        textureBounds.addRotation(xform);
    }

    public void subtractRotation(Transform3D xform) {
        super.subtractRotation(xform);
        textureBounds.subtractRotation(xform);
    }


    /**
        Calculates the bounding rectangle for this polygon that
        is aligned with the texture bounds.
    */
    public Rectangle3D calcBoundingRectangle() {

        Vector3D u = new Vector3D(textureBounds.getDirectionU());
        Vector3D v = new Vector3D(textureBounds.getDirectionV());
        Vector3D d = new Vector3D();
        u.normalize();
        v.normalize();

        float uMin = 0;
        float uMax = 0;
        float vMin = 0;
        float vMax = 0;
        for (int i=0; i<getNumVertices(); i++) {
            d.setTo(getVertex(i));
            d.subtract(getVertex(0));
            float uLength = d.getDotProduct(u);
            float vLength = d.getDotProduct(v);
            uMin = Math.min(uLength, uMin);
            uMax = Math.max(uLength, uMax);
            vMin = Math.min(vLength, vMin);
            vMax = Math.max(vLength, vMax);
        }

        Rectangle3D boundingRect = new Rectangle3D();
        Vector3D origin = boundingRect.getOrigin();
        origin.setTo(getVertex(0));
        d.setTo(u);
        d.multiply(uMin);
        origin.add(d);
        d.setTo(v);
        d.multiply(vMin);
        origin.add(d);
        boundingRect.getDirectionU().setTo(u);
        boundingRect.getDirectionV().setTo(v);
        boundingRect.setWidth(uMax - uMin);
        boundingRect.setHeight(vMax - vMin);

        // explictly set the normal since the texture directions
        // could create a normal negative to the polygon normal
        boundingRect.setNormal(getNormal());

        return boundingRect;
    }

}
