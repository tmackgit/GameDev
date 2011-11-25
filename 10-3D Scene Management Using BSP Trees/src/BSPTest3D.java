
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.bsp2D.*;
import com.brackeen.javagamebook.graphics3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;
import com.brackeen.javagamebook.test.GameCore3D;

public class BSPTest3D extends GameCore3D {

    public static void main(String[] args) {
        new BSPTest3D().run();
    }

    protected BSPTree bspTree;

    public void init() {
        init(LOW_RES_MODES);
    }


    public void createPolygons() {
        ShadedTexture floorTexture = (ShadedTexture)
            Texture.createTexture("../images/roof1.png", true);
        ShadedTexture ceilingTexture = (ShadedTexture)
            Texture.createTexture("../images/roof2.png", true);
        ShadedTexture wallTexture = (ShadedTexture)
            Texture.createTexture("../images/wall1.png", true);

        // The floor/ceiling polygons
        Polygon3D floor = new BSPPolygon(new Vector3D[] {
            new Vector3D(0,0,150), new Vector3D(0,0,450),
            new Vector3D(800,0,450), new Vector3D(800,0,300),
            new Vector3D(500,0,300), new Vector3D(500,0,75),
        }, BSPPolygon.TYPE_FLOOR);
        Polygon3D ceiling = new BSPPolygon(new Vector3D[] {
            new Vector3D(0,300,450), new Vector3D(0,300,150),
            new Vector3D(500,300,75), new Vector3D(500,300,300),
            new Vector3D(800,300,300), new Vector3D(800,300,450),
        }, BSPPolygon.TYPE_FLOOR);
        polygons.add(floor);
        polygons.add(ceiling);
        setTexture(floor, floorTexture);
        setTexture(ceiling, ceilingTexture);

        // vertices defined from left to right as the viewer
        // looks at the wall
        BSPPolygon wallA = createPolygon(
            new BSPLine(0, 150, 500, 75), 0, 300);
        BSPPolygon wallB = createPolygon(
            new BSPLine(500, 75, 500, 300), 0, 300);
        BSPPolygon wallC = createPolygon(
            new BSPLine(500, 300, 800, 300), 0, 300);
        BSPPolygon wallD = createPolygon(
            new BSPLine(800, 450, 0, 450), 0, 300);
        BSPPolygon wallE = createPolygon(
            new BSPLine(0, 450, 0, 150), 0, 300);
        BSPPolygon wallF = createPolygon(
            new BSPLine(800, 300, 800, 450), 0, 300);
        polygons.add(wallA);
        polygons.add(wallB);
        polygons.add(wallC);
        polygons.add(wallD);
        polygons.add(wallE);
        polygons.add(wallF);

        setTexture(wallA, wallTexture);
        setTexture(wallB, wallTexture);
        setTexture(wallC, wallTexture);
        setTexture(wallD, wallTexture);
        setTexture(wallE, wallTexture);
        setTexture(wallF, wallTexture);

        BSPTreeBuilder builder = new BSPTreeBuilder();
        bspTree = builder.build(polygons);

        // build surfaces
        ArrayList lights = new ArrayList();
        lights.add(new PointLight3D(400, 200, 100, 1, 300));
        lights.add(new PointLight3D(700, 200, 400, .5f, 1000));
        lights.add(new PointLight3D(65, 200, 385, 1, 100));
        bspTree.createSurfaces(lights);

    }

    public BSPPolygon createPolygon(BSPLine line, float bottom,
        float top)
    {
        return new BSPPolygon(new Vector3D[] {
            new Vector3D(line.x1, bottom, line.y1),
            new Vector3D(line.x2, bottom, line.y2),
            new Vector3D(line.x2, top, line.y2),
            new Vector3D(line.x1, top, line.y1)
            }, BSPPolygon.TYPE_WALL);
    }

    public void setTexture(Polygon3D poly, Texture texture) {

        Vector3D origin = poly.getVertex(1);

        Vector3D dv = new Vector3D(poly.getVertex(0));
        dv.subtract(origin);

        Vector3D du = new Vector3D();
        du.setToCrossProduct(poly.getNormal(), dv);

        Rectangle3D texBounds = new Rectangle3D(origin, du, dv,
            texture.getWidth(), texture.getHeight());

        ((TexturedPolygon3D)poly).setTexture(texture, texBounds);
    }

    public void createPolygonRenderer() {
        // make the view window the entire screen
        viewWindow = new ViewWindow(0, 0,
            screen.getWidth(), screen.getHeight(),
            (float)Math.toRadians(75));

        Transform3D camera = new Transform3D(400,100,300);
        polygonRenderer = new SimpleBSPRenderer(
            camera, viewWindow);
    }

     public void draw(Graphics2D g) {

        // draw polygons
        polygonRenderer.startFrame(g);
        ((SimpleBSPRenderer)polygonRenderer).draw(g, bspTree);
        polygonRenderer.endFrame(g);

        super.drawText(g);
    }

}
