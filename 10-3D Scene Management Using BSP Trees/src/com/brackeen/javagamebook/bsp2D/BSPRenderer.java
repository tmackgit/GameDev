package com.brackeen.javagamebook.bsp2D;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.bsp2D.*;
import com.brackeen.javagamebook.graphics3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;
import com.brackeen.javagamebook.game.GameObjectManager;

/**
    The BSPRenderer class is a renderer capable of drawing
    polygons in a BSP tree and any polygon objects in the scene.
    When drawing BSP polygons, the BSPRenderer writes the BSP
    polygon depth to a z-buffer. Polygon objects use the z-buffer
    to determine their visibility within the scene on a per-pixel
    basis.
*/
public class BSPRenderer extends ZBufferedRenderer
    implements BSPTreeTraverseListener
{

    /**
        How many polygons to draw before checking if the view
        is filled.
    */
    private static final int FILLED_CHECK = 3;

    protected HashMap bspScanRenderers;
    protected BSPTreeTraverser traverser;
    protected Graphics2D currentGraphics2D;
    protected boolean viewNotFilledFirstTime;
    protected int polygonCount;

    /**
        Creates a new BSP renderer with the specified camera
        object and view window.
    */
    public BSPRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        super(camera, viewWindow, false);
        viewNotFilledFirstTime = true;
        traverser = new BSPTreeTraverser(this);
    }


    /**
        Sets the GamebjectManager. The BSP traverser sets the
        visibily of the objects.
    */
    public void setGameObjectManager(
        GameObjectManager gameObjectManager)
    {
        traverser.setGameObjectManager(gameObjectManager);
    }


    protected void init() {
        destPolygon = new TexturedPolygon3D();
        scanConverter = new SortedScanConverter(viewWindow);

        // create renderers for each texture (HotSpot optimization)
        scanRenderers = new HashMap();
        scanRenderers.put(PowerOf2Texture.class,
            new PowerOf2TextureZRenderer());
        scanRenderers.put(ShadedTexture.class,
            new ShadedTextureZRenderer());
        scanRenderers.put(ShadedSurface.class,
            new ShadedSurfaceZRenderer());

        // same thing, for bsp tree polygons
        bspScanRenderers = new HashMap();
        bspScanRenderers.put(PowerOf2Texture.class,
            new PowerOf2TextureRenderer());
        bspScanRenderers.put(ShadedTexture.class,
            new ShadedTextureRenderer());
        bspScanRenderers.put(ShadedSurface.class,
            new ShadedSurfaceRenderer());
    }


    public void startFrame(Graphics2D g) {
        super.startFrame(g);
        ((SortedScanConverter)scanConverter).clear();
        polygonCount = 0;
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
        ((SortedScanConverter)scanConverter).setSortedMode(true);
        currentGraphics2D = g;
        traverser.traverse(tree, camera.getLocation());
        ((SortedScanConverter)scanConverter).setSortedMode(false);
    }


    // from the BSPTreeTraverseListener interface
    public boolean visitPolygon(BSPPolygon poly, boolean isBack) {
        SortedScanConverter scanConverter =
            (SortedScanConverter)this.scanConverter;

        draw(currentGraphics2D, poly);

        // check if view is filled every three polygons
        polygonCount++;
        if (polygonCount == FILLED_CHECK) {
            polygonCount = 0;
            return
                !((SortedScanConverter)scanConverter).isFilled();
        }
        return true;
    }


    protected void drawCurrentPolygon(Graphics2D g) {
        if (!(sourcePolygon instanceof BSPPolygon)) {
            super.drawCurrentPolygon(g);
            return;
        }
        buildSurface();
        SortedScanConverter scanConverter =
            (SortedScanConverter)this.scanConverter;
        TexturedPolygon3D poly = (TexturedPolygon3D)destPolygon;
        Texture texture = poly.getTexture();
        ScanRenderer scanRenderer = (ScanRenderer)
            bspScanRenderers.get(texture.getClass());
        scanRenderer.setTexture(texture);
        Rectangle3D textureBounds = poly.getTextureBounds();

        a.setToCrossProduct(textureBounds.getDirectionV(),
            textureBounds.getOrigin());
        b.setToCrossProduct(textureBounds.getOrigin(),
            textureBounds.getDirectionU());
        c.setToCrossProduct(textureBounds.getDirectionU(),
            textureBounds.getDirectionV());

        // w is used to compute depth at each pixel
        w = SCALE * MIN_DISTANCE * Short.MAX_VALUE /
            (viewWindow.getDistance() *
            c.getDotProduct(textureBounds.getOrigin()));

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
                    setScanDepth(offset, scan.right-scan.left+1);
                }
            }
            y++;
            viewPos.y--;
        }
    }


    /**
        Sets the z-depth for the current polygon scan.
    */
    private void setScanDepth(int offset, int width) {
        float z = c.getDotProduct(viewPos);
        float dz = c.x;
        int depth = (int)(w*z);
        int dDepth = (int)(w*dz);
        short[] depthBuffer = zBuffer.getArray();
        int endOffset = offset + width;

        // depth will be constant for many floors and ceilings
        if (dDepth == 0) {
            short d = (short)(depth >> SCALE_BITS);
            while (offset < endOffset) {
                depthBuffer[offset++] = d;
            }
        }
        else {
            while (offset < endOffset) {
                depthBuffer[offset++] =
                    (short)(depth >> SCALE_BITS);
                depth += dDepth;
            }
        }
    }

}
