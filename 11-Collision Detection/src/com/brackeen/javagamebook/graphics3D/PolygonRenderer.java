package com.brackeen.javagamebook.graphics3D;

import java.awt.Graphics2D;
import java.awt.Color;
import com.brackeen.javagamebook.math3D.*;

/**
    The PolygonRenderer class is an abstract class that transforms
    and draws polygons onto the screen.
*/
public abstract class PolygonRenderer {

    protected ScanConverter scanConverter;
    protected Transform3D camera;
    protected ViewWindow viewWindow;
    protected boolean clearViewEveryFrame;
    protected Polygon3D sourcePolygon;
    protected Polygon3D destPolygon;

    /**
        Creates a new PolygonRenderer with the specified
        Transform3D (camera) and ViewWindow. The view is cleared
        when startFrame() is called.
    */
    public PolygonRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        this(camera, viewWindow, true);
    }


    /**
        Creates a new PolygonRenderer with the specified
        Transform3D (camera) and ViewWindow. If
        clearViewEveryFrame is true, the view is cleared when
        startFrame() is called.
    */
    public PolygonRenderer(Transform3D camera,
        ViewWindow viewWindow, boolean clearViewEveryFrame)
    {
        this.camera = camera;
        this.viewWindow = viewWindow;
        this.clearViewEveryFrame = clearViewEveryFrame;
        init();
    }


    /**
        Create the scan converter and dest polygon.
    */
    protected void init() {
        destPolygon = new Polygon3D();
        scanConverter = new ScanConverter(viewWindow);
    }


    /**
        Gets the camera used for this PolygonRenderer.
    */
    public Transform3D getCamera() {
        return camera;
    }


    /**
        Indicates the start of rendering of a frame. This method
        should be called every frame before any polygons are drawn.
    */
    public void startFrame(Graphics2D g) {
        if (clearViewEveryFrame) {
            g.setColor(Color.black);
            g.fillRect(viewWindow.getLeftOffset(),
                viewWindow.getTopOffset(),
                viewWindow.getWidth(), viewWindow.getHeight());
        }
    }


    /**
        Indicates the end of rendering of a frame. This method
        should be called every frame after all polygons are drawn.
    */
    public void endFrame(Graphics2D g) {
        // do nothing, for now.
    }


    /**
        Transforms and draws a polygon.
    */
    public boolean draw(Graphics2D g, Polygon3D poly) {
        if (poly.isFacing(camera.getLocation())) {
            sourcePolygon = poly;
            destPolygon.setTo(poly);
            destPolygon.subtract(camera);
            boolean visible = destPolygon.clip(-1);
            if (visible) {
                destPolygon.project(viewWindow);
                visible = scanConverter.convert(destPolygon);
                if (visible) {
                    drawCurrentPolygon(g);
                    return true;
                }
            }
        }
        return false;
    }


    /**
        Draws the current polygon. At this point, the current
        polygon is transformed, clipped, projected,
        scan-converted, and visible.
    */
    protected abstract void drawCurrentPolygon(Graphics2D g);
}
