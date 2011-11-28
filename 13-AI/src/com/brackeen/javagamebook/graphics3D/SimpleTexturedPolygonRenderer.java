package com.brackeen.javagamebook.graphics3D;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import com.brackeen.javagamebook.math3D.*;

/**
    The SimpleTexturedPolygonRenderer class demonstrates
    the fundamentals of perspective-correct texture mapping.
    It is very slow and maps the same texture for every polygon.
*/
public class SimpleTexturedPolygonRenderer extends PolygonRenderer
{
    protected Vector3D a = new Vector3D();
    protected Vector3D b = new Vector3D();
    protected Vector3D c = new Vector3D();
    protected Vector3D viewPos = new Vector3D();
    protected Rectangle3D textureBounds = new Rectangle3D();
    protected BufferedImage texture;

    public SimpleTexturedPolygonRenderer(Transform3D camera,
        ViewWindow viewWindow, String textureFile)
    {
        super(camera, viewWindow);
        texture = loadTexture(textureFile);
    }


    /**
        Loads the texture image from a file. This image is used
        for all polygons.
    */
    public BufferedImage loadTexture(String filename) {
        try {
            return ImageIO.read(new File(filename));
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    protected void drawCurrentPolygon(Graphics2D g) {

        // Calculate texture bounds.
        // Ideally texture bounds are pre-calculated and stored
        // with the polygon. Coordinates are computed here for
        // demonstration purposes.
        Vector3D textureOrigin = textureBounds.getOrigin();
        Vector3D textureDirectionU = textureBounds.getDirectionU();
        Vector3D textureDirectionV = textureBounds.getDirectionV();

        textureOrigin.setTo(sourcePolygon.getVertex(0));

        textureDirectionU.setTo(sourcePolygon.getVertex(3));
        textureDirectionU.subtract(textureOrigin);
        textureDirectionU.normalize();

        textureDirectionV.setTo(sourcePolygon.getVertex(1));
        textureDirectionV.subtract(textureOrigin);
        textureDirectionV.normalize();

        // transform the texture bounds
        textureBounds.subtract(camera);

        // start texture-mapping calculations
        a.setToCrossProduct(textureBounds.getDirectionV(),
            textureBounds.getOrigin());
        b.setToCrossProduct(textureBounds.getOrigin(),
            textureBounds.getDirectionU());
        c.setToCrossProduct(textureBounds.getDirectionU(),
            textureBounds.getDirectionV());

        int y = scanConverter.getTopBoundary();
        viewPos.z = -viewWindow.getDistance();

        while (y<=scanConverter.getBottomBoundary()) {
            ScanConverter.Scan scan = scanConverter.getScan(y);

            if (scan.isValid()) {
                viewPos.y =
                    viewWindow.convertFromScreenYToViewY(y);
                for (int x=scan.left; x<=scan.right; x++) {
                    viewPos.x =
                        viewWindow.convertFromScreenXToViewX(x);

                    // compute the texture location
                    int tx = (int)(a.getDotProduct(viewPos) /
                        c.getDotProduct(viewPos));
                    int ty = (int)(b.getDotProduct(viewPos) /
                        c.getDotProduct(viewPos));

                    // get the color to draw
                    try {
                        int color = texture.getRGB(tx, ty);

                        g.setColor(new Color(color));
                    }
                    catch (ArrayIndexOutOfBoundsException ex) {
                        g.setColor(Color.red);
                    }

                    // draw the pixel
                    g.drawLine(x,y,x,y);
                }
            }
            y++;
        }
    }

}
