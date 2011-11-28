package com.brackeen.javagamebook.bsp2D;

import java.util.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.graphics3D.texture.*;

/**
    The RoomDef class represents a convex room with walls, a
    floor, and a ceiling. The floor may be above the ceiling, in
    which case the RoomDef is a "pillar" or "block" structure,
    rather than a "room". RoomDefs are used as a shortcut
    to create the actual BSPPolygons used in the 2D BSP tree.
*/
public class RoomDef {

    private static final Vector3D FLOOR_NORMAL =
        new Vector3D(0,1,0);

    private static final Vector3D CEIL_NORMAL =
        new Vector3D(0,-1,0);

    private HorizontalAreaDef floor;
    private HorizontalAreaDef ceil;
    private List vertices;
    private float ambientLightIntensity;


    /**
        The HorizontalAreaDef class represents a floor or ceiling.
    */
    private static class HorizontalAreaDef {
        float height;
        Texture texture;
        Rectangle3D textureBounds;

        public HorizontalAreaDef(float height, Texture texture,
            Rectangle3D textureBounds)
        {
            this.height = height;
            this.texture = texture;
            this.textureBounds = textureBounds;
        }
    }


    /**
        The Vertex class represents a Wall vertex.
    */
    private static class Vertex {
        float x;
        float z;
        float bottom;
        float top;
        Texture texture;
        Rectangle3D textureBounds;

        public Vertex(float x, float z, float bottom, float top,
            Texture texture, Rectangle3D textureBounds)
        {
            this.x = x;
            this.z = z;
            this.bottom = bottom;
            this.top = top;
            this.texture = texture;
            this.textureBounds = textureBounds;
        }

        public boolean isWall() {
            return (bottom != top) && (texture != null);
        }
    }


    /**
        Creates a new RoomDef with an ambient light intensity of
        0.5. The walls, floors and ceiling all use this
        ambient light intensity.
    */
    public RoomDef() {
        this(0.5f);
    }


    /**
        Creates a new RoomDef with the specified
        ambient light intensity. The walls, floors and ceiling
        all use this ambient light intensity.
    */
    public RoomDef(float ambientLightIntensity) {
        this.ambientLightIntensity = ambientLightIntensity;
        vertices = new ArrayList();
    }


    /**
        Adds a new wall vertex at the specified (x,z) location,
        with the specified texture. The wall stretches from
        the floor to the ceiling. If the texture is null,
        no polygon for the wall is created.
    */
    public void addVertex(float x, float z, Texture texture) {
        addVertex(x, z, Math.min(floor.height, ceil.height),
             Math.max(floor.height, ceil.height),
             texture);
    }


    /**
        Adds a new wall vertex at the specified (x,z) location,
        with the specified texture, bottom location, and top
        location. If the texture is null, no polygon for the wall
        is created.
    */
    public void addVertex(float x, float z,
        float bottom, float top, Texture texture)
    {
        vertices.add(
            new Vertex(x, z, bottom, top, texture, null));
    }


    /**
        Adds a new wall vertex at the specified (x,z) location,
        with the specified texture, texture bounds, bottom
        location, and top location. If the texture is null, no
        polygon for the wall is created.
    */
    public void addVertex(float x, float z, float bottom,
        float top, Texture texture, Rectangle3D texBounds)
    {
        vertices.add(
            new Vertex(x, z, bottom, top, texture, texBounds));
    }


    /**
        Sets the floor height and floor texture of this room. If
        the texture is null, no floor polygon is created, but the
        height of the floor is used as the default bottom wall
        boundary.
    */
    public void setFloor(float height, Texture texture) {
        setFloor(height, texture, null);
    }


    /**
        Sets the floor height, floor texture, and floor texture
        bounds of this room. If the texture is null, no floor
        polygon is created, but the height of the floor is used as
        the default bottom wall boundary. If the texture bounds is
        null, a default texture bounds is used.
    */
    public void setFloor(float height, Texture texture,
        Rectangle3D texBounds)
    {
        if (texture != null && texBounds == null) {
            texBounds = new Rectangle3D(
                new Vector3D(0,height,0),
                new Vector3D(1,0,0),
                new Vector3D(0,0,-1),
                texture.getWidth(), texture.getHeight());
        }
        floor = new HorizontalAreaDef(height, texture, texBounds);
    }


    /**
        Sets the ceiling height and ceiling texture of this room.
        If the texture is null, no ceiling polygon is created, but
        the height of the ceiling is used as the default top wall
        boundary.
    */
    public void setCeil(float height, Texture texture) {
        setCeil(height, texture, null);
    }


     /**
        Sets the ceiling height, ceiling texture, and ceiling
        texture bounds of this room. If the texture is null, no
        floor polygon is created, but the height of the floor is
        used as the default bottom wall boundary. If the texture
        bounds is null, a default texture bounds is used.
    */
    public void setCeil(float height, Texture texture,
        Rectangle3D texBounds)
    {
        if (texture != null && texBounds == null) {
            texBounds = new Rectangle3D(
                new Vector3D(0,height,0),
                new Vector3D(1,0,0),
                new Vector3D(0,0,1),
                texture.getWidth(), texture.getHeight());
        }
        ceil = new HorizontalAreaDef(height, texture, texBounds);
    }


    /**
        Creates and returns a list of BSPPolygons that represent
        the walls, floor, and ceiling of this room.
    */
    public List createPolygons() {
        List walls = createVerticalPolygons();
        List floors = createHorizontalPolygons();

        List list = new ArrayList(walls.size() + floors.size());
        list.addAll(walls);
        list.addAll(floors);
        return list;
    }


    /**
        Creates and returns a list of BSPPolygons that represent
        the vertical walls of this room.
    */
    public List createVerticalPolygons() {
        int size = vertices.size();
        List list = new ArrayList(size);
        if (size == 0) {
            return list;
        }
        Vertex origin = (Vertex)vertices.get(0);
        Vector3D textureOrigin =
            new Vector3D(origin.x, ceil.height, origin.z);
        Vector3D textureDy = new Vector3D(0,-1,0);

        for (int i=0; i<size; i++) {
            Vertex curr = (Vertex)vertices.get(i);

            if (!curr.isWall()) {
                continue;
            }

            // determine if wall is passable (useful for portals)
            int type = BSPPolygon.TYPE_WALL;
            if (floor.height > ceil.height) {
                if (floor.height - ceil.height <=
                    BSPPolygon.PASSABLE_WALL_THRESHOLD)
                {
                    type = BSPPolygon.TYPE_PASSABLE_WALL;
                }
            }
            else if (curr.top - curr.bottom <=
               BSPPolygon.PASSABLE_WALL_THRESHOLD)
            {
                type = BSPPolygon.TYPE_PASSABLE_WALL;
            }
            else if (curr.bottom - floor.height >=
                BSPPolygon.PASSABLE_ENTRYWAY_THRESHOLD)
            {
                type = BSPPolygon.TYPE_PASSABLE_WALL;
            }

            List wallVertices = new ArrayList();
            Vertex prev;
            Vertex next;
            if (floor.height < ceil.height) {
                prev = (Vertex)vertices.get((i+size-1) % size);
                next = (Vertex)vertices.get((i+1) % size);
            }
            else {
                prev = (Vertex)vertices.get((i+1) % size);
                next = (Vertex)vertices.get((i+size-1) % size);
            }

            // bottom vertices
            wallVertices.add(
                new Vector3D(next.x, curr.bottom, next.z));
            wallVertices.add(
                new Vector3D(curr.x, curr.bottom, curr.z));

            // optional vertices at T-Junctions on left side
            if (prev.isWall()) {
                if (prev.bottom > curr.bottom &&
                    prev.bottom < curr.top)
                {
                    wallVertices.add(
                        new Vector3D(curr.x, prev.bottom, curr.z));
                }
                if (prev.top > curr.bottom &&
                    prev.top < curr.top)
                {
                    wallVertices.add(
                        new Vector3D(curr.x, prev.top, curr.z));
                }

            }

            // top vertives
            wallVertices.add(
                new Vector3D(curr.x, curr.top, curr.z));
            wallVertices.add(
                new Vector3D(next.x, curr.top, next.z));

            // optional vertices at T-Junctions on left side
            if (next.isWall()) {
                if (next.top > curr.bottom &&
                    next.top < curr.top)
                {
                    wallVertices.add(
                        new Vector3D(next.x, next.top, next.z));
                }
                if (next.bottom > curr.bottom &&
                    next.bottom < curr.top)
                {
                    wallVertices.add(
                        new Vector3D(next.x, next.bottom, next.z));
                }

            }

            // create wall polygon
            Vector3D[] array = new Vector3D[wallVertices.size()];
            wallVertices.toArray(array);
            BSPPolygon poly = new BSPPolygon(array, type);
            poly.setAmbientLightIntensity(ambientLightIntensity);
            if (curr.textureBounds == null) {
                Vector3D textureDx = new Vector3D(next.x,0,next.z);
                textureDx.subtract(new Vector3D(curr.x,0,curr.z));
                textureDx.normalize();
                curr.textureBounds = new Rectangle3D(
                    textureOrigin,
                    textureDx,
                    textureDy,
                    curr.texture.getWidth(),
                    curr.texture.getHeight());
            }
            poly.setTexture(curr.texture, curr.textureBounds);
            list.add(poly);
        }
        return list;
    }


    /**
        Creates and returns a list of BSPPolygons that represent
        the horizontal floor and ceiling of this room.
    */
    public List createHorizontalPolygons() {

        List list = new ArrayList(2);
        int size = vertices.size();
        Vector3D[] floorVertices = new Vector3D[size];
        Vector3D[] ceilVertices = new Vector3D[size];

        // create vertices
        for (int i=0; i<size; i++) {
            Vertex v = (Vertex)vertices.get(i);
            floorVertices[i] =
                new Vector3D(v.x, floor.height, v.z);
            ceilVertices[size-(i+1)] =
                new Vector3D(v.x, ceil.height, v.z);
        }

        // create floor polygon
        if (floor.texture != null) {
            BSPPolygon poly = new BSPPolygon(floorVertices,
                BSPPolygon.TYPE_FLOOR);
            poly.setTexture(floor.texture, floor.textureBounds);
            poly.setNormal(FLOOR_NORMAL);
            poly.setAmbientLightIntensity(ambientLightIntensity);
            list.add(poly);
        }

        // create ceiling polygon
        if (ceil.texture != null) {
            BSPPolygon poly = new BSPPolygon(ceilVertices,
                BSPPolygon.TYPE_FLOOR);
            poly.setTexture(ceil.texture, ceil.textureBounds);
            poly.setNormal(CEIL_NORMAL);
            poly.setAmbientLightIntensity(ambientLightIntensity);
            list.add(poly);
        }

        return list;
    }

}
