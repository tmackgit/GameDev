package com.brackeen.javagamebook.graphics3D;

import java.awt.*;
import java.awt.image.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;

/**
    The ShadedTexturedPolygonRenderer class is a PolygonRenderer
    that renders ShadedTextured dynamically with one light source.
    By default, the ambient light intensity is 0.5 and there
    is no point light.
*/
public class ShadedTexturedPolygonRenderer
    extends FastTexturedPolygonRenderer
{

    private PointLight3D lightSource;
    private float ambientLightIntensity = 0.5f;
    private Vector3D directionToLight = new Vector3D();

    public ShadedTexturedPolygonRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        this(camera, viewWindow, true);
    }

    public ShadedTexturedPolygonRenderer(Transform3D camera,
        ViewWindow viewWindow, boolean clearViewEveryFrame)
    {
        super(camera, viewWindow, clearViewEveryFrame);
    }


    /**
        Gets the light source for this renderer.
    */
    public PointLight3D getLightSource() {
        return lightSource;
    }


    /**
        Sets the light source for this renderer.
    */
    public void setLightSource(PointLight3D lightSource) {
        this.lightSource = lightSource;
    }


    /**
        Gets the ambient light intensity.
    */
    public float getAmbientLightIntensity() {
        return ambientLightIntensity;
    }


    /**
        Sets the ambient light intensity, generally between 0 and
        1.
    */
    public void setAmbientLightIntensity(float i) {
        ambientLightIntensity = i;
    }


    protected void drawCurrentPolygon(Graphics2D g) {
        // set the shade level of the polygon before drawing it
        if (sourcePolygon instanceof TexturedPolygon3D) {
            TexturedPolygon3D poly =
                ((TexturedPolygon3D)sourcePolygon);
            Texture texture = poly.getTexture();
            if (texture instanceof ShadedTexture) {
                calcShadeLevel();
            }
        }
        super.drawCurrentPolygon(g);
    }


    /**
        Calculates the shade level of the current polygon
    */
    private void calcShadeLevel() {
        TexturedPolygon3D poly = (TexturedPolygon3D)sourcePolygon;
        float intensity = 0;
        if (lightSource != null) {


            // average all the vertices in the polygon
            directionToLight.setTo(0,0,0);
            for (int i=0; i<poly.getNumVertices(); i++) {
                directionToLight.add(poly.getVertex(i));
            }
            directionToLight.divide(poly.getNumVertices());

            // make the vector from the average vertex
            // to the light
            directionToLight.subtract(lightSource);
            directionToLight.multiply(-1);

            // get the distance to the light for falloff
            float distance = directionToLight.length();

            // compute the diffuse reflect
            directionToLight.normalize();
            Vector3D normal = poly.getNormal();
            intensity = lightSource.getIntensity(distance)
                * directionToLight.getDotProduct(normal);
            intensity = Math.min(intensity, 1);
            intensity = Math.max(intensity, 0);
        }

        intensity+=ambientLightIntensity;
        intensity = Math.min(intensity, 1);
        intensity = Math.max(intensity, 0);
        int level =
            Math.round(intensity*ShadedTexture.MAX_LEVEL);
        ((ShadedTexture)poly.getTexture()).
            setDefaultShadeLevel(level);
    }

}
