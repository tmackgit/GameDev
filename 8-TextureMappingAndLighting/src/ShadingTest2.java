
import java.awt.*;
import java.awt.image.*;
import java.util.List;
import java.util.ArrayList;

import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;

public class ShadingTest2 extends TextureMapTest2 {

    public static void main(String[] args) {
        new ShadingTest2().run();
    }

    private List lights;
    private float ambientLightIntensity;

    public void init() {
        ambientLightIntensity = .05f;
        lights = new ArrayList();
        lights.add(new PointLight3D(-100,100,-975, 1f, 500));
        lights.add(new PointLight3D(50,150,-700, 1f, 500));
        lights.add(new PointLight3D(2000,2000,-2000, .1f, -1));
        lights.add(new PointLight3D(-250,250,-1200, 1f, 500));

        super.init(LOW_RES_MODES);
    }

    public void setTexture(TexturedPolygon3D poly,
        Texture texture)
    {
        ShadedSurface.createShadedSurface(
            poly, (ShadedTexture)texture,
            lights, ambientLightIntensity);
    }

    public Texture loadTexture(String imageName) {
        return Texture.createTexture(imageName, true);
    }

    public void createPolygonRenderer() {
        viewWindow = new ViewWindow(0, 0,
            screen.getWidth(), screen.getHeight(),
            (float)Math.toRadians(75));

        Transform3D camera = new Transform3D(0,100,0);
        polygonRenderer =
            new ShadedSurfacePolygonRenderer(camera, viewWindow);

    }

}