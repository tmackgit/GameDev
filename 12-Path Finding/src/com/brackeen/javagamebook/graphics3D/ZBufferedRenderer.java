package com.brackeen.javagamebook.graphics3D;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;
import com.brackeen.javagamebook.game.*;

/**
    The ZBufferedRenderer is a PolygonRenderer that
    renders polygons with a Z-Buffer to ensure correct rendering
    (closer objects appear in front of farther away objects).
*/
public class ZBufferedRenderer
    extends ShadedSurfacePolygonRenderer
    implements GameObjectRenderer
{
    /**
        The minimum distance for z-buffering. Larger values give
        more accurate calculations for further distances.
    */
    protected static final int MIN_DISTANCE = 12;

    protected TexturedPolygon3D temp;
    protected ZBuffer zBuffer;
    // used for calculating depth
    protected float w;

    public ZBufferedRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        this(camera, viewWindow, true);
    }

    public ZBufferedRenderer(Transform3D camera,
        ViewWindow viewWindow, boolean eraseView)
    {
        super(camera, viewWindow, eraseView);
        temp = new TexturedPolygon3D();
    }

    protected void init() {
        destPolygon = new TexturedPolygon3D();
        scanConverter = new ScanConverter(viewWindow);

        // create renders for each texture (HotSpot optimization)
        scanRenderers = new HashMap();
        scanRenderers.put(PowerOf2Texture.class,
            new PowerOf2TextureZRenderer());
        scanRenderers.put(ShadedTexture.class,
            new ShadedTextureZRenderer());
        scanRenderers.put(ShadedSurface.class,
            new ShadedSurfaceZRenderer());
    }


    public void startFrame(Graphics2D g) {
        super.startFrame(g);
        // initialize depth buffer
        if (zBuffer == null ||
            zBuffer.getWidth() != viewWindow.getWidth() ||
            zBuffer.getHeight() != viewWindow.getHeight())
        {
            zBuffer = new ZBuffer(
                viewWindow.getWidth(), viewWindow.getHeight());
        }
        else if (clearViewEveryFrame) {
            zBuffer.clear();
        }
    }

    public boolean draw(Graphics2D g, GameObject object) {
        return draw(g, object.getPolygonGroup());
    }

    public boolean draw(Graphics2D g, PolygonGroup group) {
        boolean visible = false;
        group.resetIterator();
        while (group.hasNext()) {
            group.nextPolygonTransformed(temp);
            visible |= draw(g, temp);
        }
        return visible;
    }


    protected void drawCurrentPolygon(Graphics2D g) {
        if (!(sourcePolygon instanceof TexturedPolygon3D)) {
            // not a textured polygon - return
            return;
        }
        buildSurface();
        TexturedPolygon3D poly = (TexturedPolygon3D)destPolygon;
        Texture texture = poly.getTexture();
        ScanRenderer scanRenderer = (ScanRenderer)
            scanRenderers.get(texture.getClass());
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
            ScanConverter.Scan scan = scanConverter.getScan(y);

            if (scan.isValid()) {
                viewPos.x = viewWindow.
                    convertFromScreenXToViewX(scan.left);
                int offset = (y - viewWindow.getTopOffset()) *
                    viewWindow.getWidth() +
                    (scan.left - viewWindow.getLeftOffset());

                scanRenderer.render(offset, scan.left, scan.right);
            }
            y++;
            viewPos.y--;
        }
    }


    // the following three ScanRenderers are the same, but refer
    // to textures explicitly as either a PowerOf2Texture, a
    // ShadedTexture, or a ShadedSurface.
    // This allows HotSpot to do some inlining of the textures'
    // getColor() method, which significantly increases
    // performance.

    public class PowerOf2TextureZRenderer extends ScanRenderer {

        public void render(int offset, int left, int right) {
            PowerOf2Texture texture =
                (PowerOf2Texture)currentTexture;
            float u = SCALE * a.getDotProduct(viewPos);
            float v = SCALE * b.getDotProduct(viewPos);
            float z = c.getDotProduct(viewPos);
            float du = INTERP_SIZE * SCALE * a.x;
            float dv = INTERP_SIZE * SCALE * b.x;
            float dz = INTERP_SIZE * c.x;
            int nextTx = (int)(u/z);
            int nextTy = (int)(v/z);
            int depth = (int)(w*z);
            int dDepth = (int)(w*c.x);
            int x = left;
            while (x <= right) {
                int tx = nextTx;
                int ty = nextTy;
                int maxLength = right-x+1;
                if (maxLength > INTERP_SIZE) {
                    u+=du;
                    v+=dv;
                    z+=dz;
                    nextTx = (int)(u/z);
                    nextTy = (int)(v/z);
                    int dtx = (nextTx-tx) >> INTERP_SIZE_BITS;
                    int dty = (nextTy-ty) >> INTERP_SIZE_BITS;
                    int endOffset = offset + INTERP_SIZE;
                    while (offset < endOffset) {
                        if (zBuffer.checkDepth(offset,
                            (short)(depth >> SCALE_BITS)))
                        {
                            doubleBufferData[offset] =
                                texture.getColor(tx >> SCALE_BITS,
                                ty >> SCALE_BITS);
                        }
                        offset++;
                        tx+=dtx;
                        ty+=dty;
                        depth+=dDepth;
                    }
                    x+=INTERP_SIZE;
                }
                else {
                    // variable interpolation size
                    int interpSize = maxLength;
                    u += interpSize * SCALE * a.x;
                    v += interpSize * SCALE * b.x;
                    z += interpSize * c.x;
                    nextTx = (int)(u/z);
                    nextTy = (int)(v/z);
                    int dtx = (nextTx-tx) / interpSize;
                    int dty = (nextTy-ty) / interpSize;
                    int endOffset = offset + interpSize;
                    while (offset < endOffset) {
                        if (zBuffer.checkDepth(offset,
                            (short)(depth >> SCALE_BITS)))
                        {
                            doubleBufferData[offset++] =
                                texture.getColor(tx >> SCALE_BITS,
                                ty >> SCALE_BITS);
                        }
                        offset++;
                        tx+=dtx;
                        ty+=dty;
                        depth+=dDepth;
                    }
                    x+=interpSize;

                }

            }
        }
    }

    public class ShadedTextureZRenderer extends ScanRenderer {

        public void render(int offset, int left, int right) {
            ShadedTexture texture =
                (ShadedTexture)currentTexture;
            float u = SCALE * a.getDotProduct(viewPos);
            float v = SCALE * b.getDotProduct(viewPos);
            float z = c.getDotProduct(viewPos);
            float du = INTERP_SIZE * SCALE * a.x;
            float dv = INTERP_SIZE * SCALE * b.x;
            float dz = INTERP_SIZE * c.x;
            int nextTx = (int)(u/z);
            int nextTy = (int)(v/z);
            int depth = (int)(w*z);
            int dDepth = (int)(w*c.x);
            int x = left;
            while (x <= right) {
                int tx = nextTx;
                int ty = nextTy;
                int maxLength = right-x+1;
                if (maxLength > INTERP_SIZE) {
                    u+=du;
                    v+=dv;
                    z+=dz;
                    nextTx = (int)(u/z);
                    nextTy = (int)(v/z);
                    int dtx = (nextTx-tx) >> INTERP_SIZE_BITS;
                    int dty = (nextTy-ty) >> INTERP_SIZE_BITS;
                    int endOffset = offset + INTERP_SIZE;
                    while (offset < endOffset) {
                        if (zBuffer.checkDepth(offset,
                            (short)(depth >> SCALE_BITS)))
                        {
                            doubleBufferData[offset] =
                                texture.getColor(tx >> SCALE_BITS,
                                ty >> SCALE_BITS);
                        }
                        offset++;
                        tx+=dtx;
                        ty+=dty;
                        depth+=dDepth;
                    }
                    x+=INTERP_SIZE;
                }
                else {
                    // variable interpolation size
                    int interpSize = maxLength;
                    u += interpSize * SCALE * a.x;
                    v += interpSize * SCALE * b.x;
                    z += interpSize * c.x;
                    nextTx = (int)(u/z);
                    nextTy = (int)(v/z);
                    int dtx = (nextTx-tx) / interpSize;
                    int dty = (nextTy-ty) / interpSize;
                    int endOffset = offset + interpSize;
                    while (offset < endOffset) {
                        if (zBuffer.checkDepth(offset,
                            (short)(depth >> SCALE_BITS)))
                        {
                            doubleBufferData[offset] =
                                texture.getColor(tx >> SCALE_BITS,
                                ty >> SCALE_BITS);
                        }
                        offset++;
                        tx+=dtx;
                        ty+=dty;
                        depth+=dDepth;
                    }
                    x+=interpSize;
                }

            }
        }
    }

    public class ShadedSurfaceZRenderer extends ScanRenderer {

        public int checkBounds(int vScaled, int bounds) {
            int v = vScaled >> SCALE_BITS;
            if (v < 0) {
                vScaled = 0;
            }
            else if (v >= bounds) {
                vScaled = (bounds - 1) << SCALE_BITS;
            }
            return vScaled;
        }

        public void render(int offset, int left, int right) {
            ShadedSurface texture =
                (ShadedSurface)currentTexture;
            float u = SCALE * a.getDotProduct(viewPos);
            float v = SCALE * b.getDotProduct(viewPos);
            float z = c.getDotProduct(viewPos);
            float du = INTERP_SIZE * SCALE * a.x;
            float dv = INTERP_SIZE * SCALE * b.x;
            float dz = INTERP_SIZE * c.x;
            int nextTx = (int)(u/z);
            int nextTy = (int)(v/z);
            int depth = (int)(w*z);
            int dDepth = (int)(w*c.x);
            int x = left;
            while (x <= right) {
                int tx = nextTx;
                int ty = nextTy;
                int maxLength = right-x+1;
                if (maxLength > INTERP_SIZE) {
                    u+=du;
                    v+=dv;
                    z+=dz;
                    nextTx = (int)(u/z);
                    nextTy = (int)(v/z);
                    int dtx = (nextTx-tx) >> INTERP_SIZE_BITS;
                    int dty = (nextTy-ty) >> INTERP_SIZE_BITS;
                    int endOffset = offset + INTERP_SIZE;
                    while (offset < endOffset) {
                        if (zBuffer.checkDepth(offset,
                            (short)(depth >> SCALE_BITS)))
                        {
                            doubleBufferData[offset] =
                                texture.getColor(tx >> SCALE_BITS,
                                ty >> SCALE_BITS);
                        }
                        offset++;
                        tx+=dtx;
                        ty+=dty;
                        depth+=dDepth;
                    }
                    x+=INTERP_SIZE;
                }
                else {
                    // variable interpolation size
                    int interpSize = maxLength;
                    u += interpSize * SCALE * a.x;
                    v += interpSize * SCALE * b.x;
                    z += interpSize * c.x;
                    nextTx = (int)(u/z);
                    nextTy = (int)(v/z);

                    // make sure tx, ty, nextTx, and nextTy are
                    // all within bounds
                    tx = checkBounds(tx, texture.getWidth());
                    ty = checkBounds(ty, texture.getHeight());
                    nextTx = checkBounds(nextTx, texture.getWidth());
                    nextTy = checkBounds(nextTy, texture.getHeight());

                    int dtx = (nextTx-tx) / interpSize;
                    int dty = (nextTy-ty) / interpSize;
                    int endOffset = offset + interpSize;
                    while (offset < endOffset) {
                        if (zBuffer.checkDepth(offset,
                            (short)(depth >> SCALE_BITS)))
                        {
                            doubleBufferData[offset] =
                                texture.getColor(tx >> SCALE_BITS,
                                ty >> SCALE_BITS);
                        }
                        offset++;
                        tx+=dtx;
                        ty+=dty;
                        depth+=dDepth;
                    }
                    x+=interpSize;

                }

            }
        }
    }

}
