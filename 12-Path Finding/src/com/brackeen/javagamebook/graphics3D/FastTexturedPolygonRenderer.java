package com.brackeen.javagamebook.graphics3D;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;

/**
    The FastTexturedPolygonRenderer is a PolygonRenderer that
    efficiently renders Textures.
*/
public class FastTexturedPolygonRenderer extends PolygonRenderer {

    public static final int SCALE_BITS = 12;
    public static final int SCALE = 1 << SCALE_BITS;

    public static final int INTERP_SIZE_BITS = 4;
    public static final int INTERP_SIZE = 1 << INTERP_SIZE_BITS;

    protected Vector3D a = new Vector3D();
    protected Vector3D b = new Vector3D();
    protected Vector3D c = new Vector3D();
    protected Vector3D viewPos = new Vector3D();
    protected BufferedImage doubleBuffer;
    protected short[] doubleBufferData;
    protected HashMap scanRenderers;

    public FastTexturedPolygonRenderer(Transform3D camera,
        ViewWindow viewWindow)
    {
        this(camera, viewWindow, true);
    }

    public FastTexturedPolygonRenderer(Transform3D camera,
        ViewWindow viewWindow, boolean clearViewEveryFrame)
    {
        super(camera, viewWindow, clearViewEveryFrame);
    }

    protected void init() {
        destPolygon = new TexturedPolygon3D();
        scanConverter = new ScanConverter(viewWindow);

        // create renders for each texture (HotSpot optimization)
        scanRenderers = new HashMap();
        scanRenderers.put(PowerOf2Texture.class,
            new PowerOf2TextureRenderer());
        scanRenderers.put(ShadedTexture.class,
            new ShadedTextureRenderer());
        scanRenderers.put(ShadedSurface.class,
            new ShadedSurfaceRenderer());
    }


    public void startFrame(Graphics2D g) {
        // initialize buffer
        if (doubleBuffer == null ||
            doubleBuffer.getWidth() != viewWindow.getWidth() ||
            doubleBuffer.getHeight() != viewWindow.getHeight())
        {
            doubleBuffer = new BufferedImage(
                viewWindow.getWidth(), viewWindow.getHeight(),
                BufferedImage.TYPE_USHORT_565_RGB);
            //doubleBuffer = g.getDeviceConfiguration().createCompatibleImage(
            //viewWindow.getWidth(), viewWindow.getHeight());

            DataBuffer dest =
                doubleBuffer.getRaster().getDataBuffer();
            doubleBufferData = ((DataBufferUShort)dest).getData();
        }
        // clear view
        if (clearViewEveryFrame) {
            for (int i=0; i<doubleBufferData.length; i++) {
                doubleBufferData[i] = 0;
            }
        }
    }

    public void endFrame(Graphics2D g) {
        // draw the double buffer onto the screen
        g.drawImage(doubleBuffer, viewWindow.getLeftOffset(),
            viewWindow.getTopOffset(), null);
    }

    protected void drawCurrentPolygon(Graphics2D g) {
        if (!(sourcePolygon instanceof TexturedPolygon3D)) {
            // not a textured polygon - return
            return;
        }
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

    /**
        The ScanRenderer class is an abstract inner class of
        FastTexturedPolygonRenderer that provides an interface for
        rendering a horizontal scan line.
    */
    public abstract class ScanRenderer {

        protected Texture currentTexture;

        public void setTexture(Texture texture) {
            this.currentTexture = texture;
        }

        public abstract void render(int offset,
            int left, int right);

    }

    //================================================
    // FASTEST METHOD: no texture (for comparison)
    //================================================
    public class Method0 extends ScanRenderer {

        public void render(int offset, int left, int right) {
            for (int x=left; x<=right; x++) {
                doubleBufferData[offset++] = (short)0x0007;
            }
        }
    }


    //================================================
    // METHOD 1: access pixel buffers directly
    // and use textures sizes that are a power of 2
    //================================================
    public class Method1 extends ScanRenderer {

        public void render(int offset, int left, int right) {
            for (int x=left; x<=right; x++) {
                int tx = (int)(a.getDotProduct(viewPos) /
                    c.getDotProduct(viewPos));
                int ty = (int)(b.getDotProduct(viewPos) /
                    c.getDotProduct(viewPos));
                doubleBufferData[offset++] =
                    currentTexture.getColor(tx, ty);
                viewPos.x++;
            }
        }
    }


    //================================================
    // METHOD 2: avoid redundant calculations
    //================================================
    public class Method2 extends ScanRenderer {

        public void render(int offset, int left, int right) {
            float u = a.getDotProduct(viewPos);
            float v = b.getDotProduct(viewPos);
            float z = c.getDotProduct(viewPos);
            float du = a.x;
            float dv = b.x;
            float dz = c.x;
            for (int x=left; x<=right; x++) {
                doubleBufferData[offset++] =
                    currentTexture.getColor(
                    (int)(u/z), (int)(v/z));
                u+=du;
                v+=dv;
                z+=dz;
            }
        }
    }


    //================================================
    // METHOD 3: use ints instead of floats
    //================================================
    public class Method3 extends ScanRenderer {

        public void render(int offset, int left, int right) {
            int u = (int)(SCALE * a.getDotProduct(viewPos));
            int v = (int)(SCALE * b.getDotProduct(viewPos));
            int z = (int)(SCALE * c.getDotProduct(viewPos));
            int du = (int)(SCALE * a.x);
            int dv = (int)(SCALE * b.x);
            int dz = (int)(SCALE * c.x);
            for (int x=left; x<=right; x++) {
                doubleBufferData[offset++] =
                    currentTexture.getColor(u/z, v/z);
                u+=du;
                v+=dv;
                z+=dz;
            }
        }
    }


    //================================================
    // METHOD 4: reduce the number of divides
    // (interpolate every 16 pixels)
    // Also, apply a VM optimization by referring to
    // the texture's class rather than it's parent class.
    //================================================

    // the following three ScanRenderers are the same, but refer
    // to textures explicitly as either a PowerOf2Texture, a
    // ShadedTexture, or a ShadedSurface.
    // This allows HotSpot to do some inlining of the textures'
    // getColor() method, which significantly increases
    // performance.

    public class PowerOf2TextureRenderer extends ScanRenderer {

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
                        doubleBufferData[offset++] =
                            texture.getColor(
                            tx >> SCALE_BITS, ty >> SCALE_BITS);
                        tx+=dtx;
                        ty+=dty;
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
                        doubleBufferData[offset++] =
                            texture.getColor(
                            tx >> SCALE_BITS, ty >> SCALE_BITS);
                        tx+=dtx;
                        ty+=dty;
                    }
                    x+=interpSize;
                }

            }
        }
    }


    public class ShadedTextureRenderer extends ScanRenderer {

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
                        doubleBufferData[offset++] =
                            texture.getColor(
                            tx >> SCALE_BITS, ty >> SCALE_BITS);
                        tx+=dtx;
                        ty+=dty;
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
                        doubleBufferData[offset++] =
                            texture.getColor(
                            tx >> SCALE_BITS, ty >> SCALE_BITS);
                        tx+=dtx;
                        ty+=dty;
                    }
                    x+=interpSize;
                }

            }
        }
    }


    public class ShadedSurfaceRenderer extends ScanRenderer {

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
                        doubleBufferData[offset++] =
                            texture.getColor(
                            tx >> SCALE_BITS, ty >> SCALE_BITS);
                        tx+=dtx;
                        ty+=dty;
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
                        doubleBufferData[offset++] =
                            texture.getColor(
                            tx >> SCALE_BITS, ty >> SCALE_BITS);
                        tx+=dtx;
                        ty+=dty;
                    }
                    x+=interpSize;
                }
            }

        }
    }


}
