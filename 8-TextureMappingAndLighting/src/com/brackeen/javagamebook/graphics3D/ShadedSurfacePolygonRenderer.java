package com.brackeen.javagamebook.graphics3D;

import java.awt.*;
import java.awt.image.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;

/**
    The ShadedSurfacePolygonRenderer is a PolygonRenderer that
    renders polygons with ShadedSurfaces. It keeps track of
    built surfaces, and clears any surfaces that weren't used
    in the last rendered frame to save memory.
*/
public class ShadedSurfacePolygonRenderer
    extends FastTexturedPolygonRenderer
{

    private List builtSurfaces = new LinkedList();

    public ShadedSurfacePolygonRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        this(camera, viewWindow, true);
    }

    public ShadedSurfacePolygonRenderer(Transform3D camera,
        ViewWindow viewWindow, boolean eraseView)
    {
        super(camera, viewWindow, eraseView);
    }

    public void endFrame(Graphics2D g) {
        super.endFrame(g);

        // clear all built surfaces that weren't used this frame.
        Iterator i = builtSurfaces.iterator();
        while (i.hasNext()) {
            ShadedSurface surface = (ShadedSurface)i.next();
            if (surface.isDirty()) {
                surface.clearSurface();
                i.remove();
            }
            else {
                surface.setDirty(true);
            }
        }
    }

    protected void drawCurrentPolygon(Graphics2D g) {
        buildSurface();
        super.drawCurrentPolygon(g);
    }


    /**
        Builds the surface of the polygon if it has a
        ShadedSurface that is cleared.
    */
    protected void buildSurface() {
        // build surface, if needed
        if (sourcePolygon instanceof TexturedPolygon3D) {
            Texture texture =
                ((TexturedPolygon3D)sourcePolygon).getTexture();
            if (texture instanceof ShadedSurface) {
                ShadedSurface surface =
                    (ShadedSurface)texture;
                if (surface.isCleared()) {
                    surface.buildSurface();
                    builtSurfaces.add(surface);
                }
                surface.setDirty(false);
            }
        }
    }

}
