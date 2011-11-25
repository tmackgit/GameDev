import com.brackeen.javagamebook.test.GameCore;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;

import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.bsp2D.*;
import com.brackeen.javagamebook.graphics3D.*;

public class BSPTest2D extends GameCore
    implements BSPTreeTraverseListener
{

    public static void main(String[] args) {
        new BSPTest2D().run();
    }

    protected List polygons;
    protected BSPTree bspTree;
    protected BSPTreeTraverser traverser;
    protected int numWalls;
    protected int wallID;
    protected Vector3D viewLocation = new Vector3D();
    protected Image overlayImage;

    protected InputManager inputManager;
    private GameAction exit = new GameAction("exit");
    private GameAction addWall = new GameAction("addWall",
        GameAction.DETECT_INITAL_PRESS_ONLY);
    private GameAction removeWall = new GameAction("removeWall",
        GameAction.DETECT_INITAL_PRESS_ONLY);

    public void init() {
        super.init();

        inputManager = new InputManager(
            screen.getFullScreenWindow());

        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(addWall, KeyEvent.VK_SPACE);
        inputManager.mapToKey(removeWall, KeyEvent.VK_BACK_SPACE);

        traverser = new BSPTreeTraverser();
        // create walls
        numWalls = 1;
        polygons = new ArrayList();
        createPolygons();
        buildTree();

        // create overlay image for lines and text labels
        overlayImage = screen.createCompatibleImage(
            screen.getWidth(), screen.getHeight(),
            Transparency.BITMASK);
    }


    public void createPolygons() {
        // The floor polygon
        BSPPolygon floor = new BSPPolygon(new Vector3D[] {
            new Vector3D(0,0,0), new Vector3D(0,0,600),
            new Vector3D(800,0,600), new Vector3D(800,0,0)
            }, BSPPolygon.TYPE_FLOOR);
        polygons.add(floor);

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
        polygons.add(wallA);
        polygons.add(wallB);
        polygons.add(wallC);
        polygons.add(wallD);
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

    public void buildTree() {
        BSPTreeBuilder builder = new BSPTreeBuilder();
        bspTree = builder.build(polygons.subList(0, numWalls+1));
    }


    public void update(long elapsedTime) {
        if (exit.isPressed()) {
            stop();
            return;
        }

        if (addWall.isPressed() && numWalls < polygons.size()-1) {
            numWalls++;
            buildTree();
        }
        else if (removeWall.isPressed() && numWalls > 0) {
            numWalls--;
            buildTree();
        }

        viewLocation.x = inputManager.getMouseX();
        viewLocation.z = inputManager.getMouseY();

    }


    public void draw(Graphics2D g) {

        Graphics2D g2 = (Graphics2D)overlayImage.getGraphics();
        g2.setFont(g.getFont());

        // erase overlay image (set it to transparent);
        Composite defaultComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, screen.getWidth(), screen.getHeight());
        g2.setComposite(defaultComposite);

        // draw info on overlay image
        g2.setColor(Color.WHITE);
        g2.drawString("Press Space/Backspace to add/remove " +
            "walls. Press Esc to exit.", 5, fontSize);
        g2.drawString("The numbers represent front-to-back draw " +
            "order.", 5, fontSize*2);
        g2.drawString("The mouse represents the camera location.",
            5, fontSize*3);
        g2.dispose();

        // erase screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screen.getWidth(), screen.getHeight());

        // draw areas
        wallID = 1;
        traverser.setListener(this);
        traverser.traverse(bspTree, viewLocation);

        // draw overlay image on the screen
        g.drawImage(overlayImage, 0, 0, null);
    }

    public boolean visitPolygon(BSPPolygon poly, boolean isBack) {
        Shape shape;
        if (poly.isWall()) {
            shape = drawWall(poly);
        }
        else {
            shape = drawFloor(poly, isBack);
        }

        // draw wall id
        Graphics2D g2 = (Graphics2D)overlayImage.getGraphics();
        g2.setFont(screen.getGraphics().getFont());
        Rectangle2D bounds = shape.getBounds2D();
        int x = (int)bounds.getCenterX() - fontSize/4;
        int y = (int)bounds.getCenterY() + fontSize/2;
        g2.setColor(Color.WHITE);
        g2.drawString(wallID + ".", x, y);
        wallID++;

        return true;
    }

    public Shape drawWall(BSPPolygon wall) {
        Graphics2D g = (Graphics2D)overlayImage.getGraphics();
        g.setColor(Color.BLACK);
        BSPLine line = wall.getLine();
        g.draw(line);
        g.fillRect((int)line.x1-2, (int)line.y1-2, 5, 5);
        g.fillRect((int)line.x2-2, (int)line.y2-2, 5, 5);
        g.dispose();
        return line;
    }

    public Shape drawFloor(BSPPolygon floor, boolean isBack) {
        Graphics2D g = screen.getGraphics();
        if (isBack) {
            g.setColor(Color.DARK_GRAY);
        }
        else {
            g.setColor(Color.LIGHT_GRAY);
        }
        GeneralPath path = new GeneralPath();
        path.moveTo(floor.getVertex(0).x, floor.getVertex(0).z);
        for (int i=1; i<floor.getNumVertices(); i++) {
            path.lineTo(floor.getVertex(i).x,
                floor.getVertex(i).z);
        }
        g.fill(path);
        return path;
    }

}