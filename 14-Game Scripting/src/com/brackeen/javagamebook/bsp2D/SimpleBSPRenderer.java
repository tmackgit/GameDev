package com.brackeen.javagamebook.bsp2D;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.bsp2D.*;
import com.brackeen.javagamebook.graphics3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;


/**
    The SimpleBSPRenderer class is a renderer capable of drawing
    polygons in a BSP tree and any polygon objects in the scene.
    No Z-buffering is used.
*/
public class SimpleBSPRenderer extends ShadedSurfacePolygonRenderer
    implements BSPTreeTraverseListener
{
    protected Graphics2D currentGraphics2D;
    protected BSPTreeTraverser traverser;
    protected boolean viewNotFilledFirstTime;


    /**
        Creates a new BSP renderer with the specified camera
        object and view window.
    */
    public SimpleBSPRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        super(camera, viewWindow, false);
        viewNotFilledFirstTime = true;
    }


    protected void init() {
        traverser = new BSPTreeTraverser(this);
        destPolygon = new TexturedPolygon3D();
        scanConverter = new SortedScanConverter(viewWindow);
        ((SortedScanConverter)scanConverter).setSortedMode(true);

        // create renderers for each texture (HotSpot optimization)
        scanRenderers = new HashMap();
        scanRenderers.put(PowerOf2Texture.class,
            new PowerOf2TextureRenderer());
        scanRenderers.put(ShadedTexture.class,
            new ShadedTextureRenderer());
        scanRenderers.put(ShadedSurface.class,
            new ShadedSurfaceRenderer());
    }


    public void startFrame(Graphics2D g) {
        super.startFrame(g);
        ((SortedScanConverter)scanConverter).clear();
    }


    public void endFrame(Graphics2D g) {
        super.endFrame(g);
        if (!((SortedScanConverter)scanConverter).isFilled()) {
            g.drawString("View not completely filled", 5,
                viewWindow.getTopOffset() +
                viewWindow.getHeight() - 5);
            if (viewNotFilledFirstTime) {
                viewNotFilledFirstTime = false;
                // print message to console in case user missed it
                System.out.println("View not completely filled.");
            }
            // clear the background next time
            clearViewEveryFrame = true;
        }
        else {
            clearViewEveryFrame = false;
        }
    }


    /**
        Draws the visible polygons in a BSP tree based on
        the camera location. The polygons are drawn front-to-back.
    */
    public void draw(Graphics2D g, BSPTree tree) {
        currentGraphics2D = g;
        traverser.traverse(tree, camera.getLocation());
    }


    // from the BSPTreeTraverseListener interface
    public boolean visitPolygon(BSPPolygon poly, boolean isBack) {
        draw(currentGraphics2D, poly);
        return !((SortedScanConverter)scanConverter).isFilled();
    }


    protected void drawCurrentPolygon(Graphics2D g) {
        if (!(sourcePolygon instanceof TexturedPolygon3D)) {
            // not a textured polygon - return
            return;
        }
        buildSurface();
        SortedScanConverter scanConverter =
            (SortedScanConverter)this.scanConverter;
        TexturedPolygon3D poly = (TexturedPolygon3D)destPolygon;
        Texture texture = poly.getTexture();
        ScanRenderer scanRenderer =
            (ScanRenderer)scanRenderers.get(texture.getClass());
        scanRenderer.setTexture(texture);
        Rectangle3D textureBounds = poly.getTextureBounds();

        a.setToCrossProduct(textureBounds.getDirectionV(),
            textureBounds.getOrigin());
        b.setToCrossProduct(textureBounds.getOrigin(),
            textureBounds.getDirectionU());
        c.setToCrossProduct(textureBounds.getDirectionU(),
            textureBounds.getDirectionV());

        int y = scanConverter.getTopBoundary();
        viewPos.y = viewWindow.convertFromScreenYToViewY(y);
        viewPos.z = -viewWindow.getDistance();

        while (y<=scanConverter.getBottomBoundary()) {
            for (int i=0; i<scanConverter.getNumScans(y); i++) {
                ScanConverter.Scan scan =
                    scanConverter.getScan(y, i);

                if (scan.isValid()) {
                    viewPos.x = viewWindow.
                        convertFromScreenXToViewX(scan.left);
                    int offset = (y - viewWindow.getTopOffset()) *
                        viewWindow.getWidth() +
                        (scan.left - viewWindow.getLeftOffset());

                    scanRenderer.render(offset, scan.left,
                        scan.right);
                }
            }
            y++;
            viewPos.y--;
        }
    }
}
