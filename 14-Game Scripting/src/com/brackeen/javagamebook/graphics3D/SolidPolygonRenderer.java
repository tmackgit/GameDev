package com.brackeen.javagamebook.graphics3D;

import java.awt.Graphics2D;
import java.awt.Color;
import com.brackeen.javagamebook.math3D.*;

/**
    The SolidPolygonRenderer class transforms and draws
    solid-colored polygons onto the screen.
*/
public class SolidPolygonRenderer extends PolygonRenderer {

    public SolidPolygonRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        this(camera, viewWindow, true);
    }

    public SolidPolygonRenderer(Transform3D camera,
        ViewWindow viewWindow, boolean clearViewEveryFrame)
    {
        super(camera, viewWindow, clearViewEveryFrame);
    }


    /**
        Draws the current polygon. At this point, the current
        polygon is transformed, clipped, projected,
        scan-converted, and visible.
    */
    protected void drawCurrentPolygon(Graphics2D g) {

        // set the color
        if (sourcePolygon instanceof SolidPolygon3D) {
            g.setColor(((SolidPolygon3D)sourcePolygon).getColor());
        }
        else {
            g.setColor(Color.GREEN);
        }

        // draw the scans
        int y = scanConverter.getTopBoundary();
        while (y<=scanConverter.getBottomBoundary()) {
            ScanConverter.Scan scan = scanConverter.getScan(y);
            if (scan.isValid()) {
                g.drawLine(scan.left, y, scan.right, y);
            }
            y++;
        }
    }
}
