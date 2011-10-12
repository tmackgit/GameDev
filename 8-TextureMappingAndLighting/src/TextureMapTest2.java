
import java.awt.*;
import java.awt.image.*;

import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;
import com.brackeen.javagamebook.test.GameCore3D;

public class TextureMapTest2 extends GameCore3D {

    public static void main(String[] args) {
        new TextureMapTest2().run();
    }

    public void init() {
        init(LOW_RES_MODES);
    }

    // create a house (convex polyhedra)
    public void createPolygons() {

        // create Textures
        Texture wall = loadTexture("images/wall1.png");
        Texture roof = loadTexture("images/roof1.png");

        TexturedPolygon3D poly;

        // walls
        poly = new TexturedPolygon3D(
            new Vector3D(-200, 250, -1000),
            new Vector3D(-200, 0, -1000),
            new Vector3D(200, 0, -1000),
            new Vector3D(200, 250, -1000));
        setTexture(poly, wall);
        polygons.add(poly);

        poly = new TexturedPolygon3D(
            new Vector3D(200, 250, -1400),
            new Vector3D(200, 0, -1400),
            new Vector3D(-200, 0, -1400),
            new Vector3D(-200, 250, -1400));
        setTexture(poly, wall);
        polygons.add(poly);

        poly = new TexturedPolygon3D(
            new Vector3D(-200, 250, -1400),
            new Vector3D(-200, 0, -1400),
            new Vector3D(-200, 0, -1000),
            new Vector3D(-200, 250, -1000));
        setTexture(poly, wall);
        polygons.add(poly);

        poly = new TexturedPolygon3D(
            new Vector3D(200, 250, -1000),
            new Vector3D(200, 0, -1000),
            new Vector3D(200, 0, -1400),
            new Vector3D(200, 250, -1400));
        setTexture(poly, wall);
        polygons.add(poly);

        // roof
        poly = new TexturedPolygon3D(
            new Vector3D(-200, 250, -1000),
            new Vector3D(200, 250, -1000),
            new Vector3D(75, 400, -1200),
            new Vector3D(-75, 400, -1200));
        setTexture(poly, roof);
        polygons.add(poly);

        poly = new TexturedPolygon3D(
            new Vector3D(-200, 250, -1400),
            new Vector3D(-200, 250, -1000),
            new Vector3D(-75, 400, -1200));
        setTexture(poly, roof);
        polygons.add(poly);

        poly = new TexturedPolygon3D(
            new Vector3D(200, 250, -1400),
            new Vector3D(-200, 250, -1400),
            new Vector3D(-75, 400, -1200),
            new Vector3D(75, 400, -1200));
        setTexture(poly, roof);
        polygons.add(poly);

        poly = new TexturedPolygon3D(
            new Vector3D(200, 250, -1000),
            new Vector3D(200, 250, -1400),
            new Vector3D(75, 400, -1200));
        setTexture(poly, roof);
        polygons.add(poly);
    }

    public void setTexture(TexturedPolygon3D poly,
        Texture texture)
    {
        Vector3D origin = poly.getVertex(0);

        Vector3D dv = new Vector3D(poly.getVertex(1));
        dv.subtract(origin);

        Vector3D du = new Vector3D();
        du.setToCrossProduct(poly.getNormal(), dv);

        Rectangle3D textureBounds = new Rectangle3D(origin, du, dv,
            texture.getWidth(), texture.getHeight());

        poly.setTexture(texture, textureBounds);
    }

    public Texture loadTexture(String imageName) {
        return Texture.createTexture(imageName);
    }

    public void createPolygonRenderer() {
        viewWindow = new ViewWindow(0, 0,
            screen.getWidth(), screen.getHeight(),
            (float)Math.toRadians(75));


        Transform3D camera = new Transform3D(0,100,0);
        polygonRenderer = new FastTexturedPolygonRenderer(
            camera, viewWindow);
    }

}