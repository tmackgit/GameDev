package com.brackeen.javagamebook.graphics3D;

/**
    The ZBuffer class implements a z-buffer, or depth-buffer,
    that records the depth of every pixel in a 3D view window.
    The value recorded for each pixel is the inverse of the
    depth (1/z), so there is higher precision for close objects
    and a lower precision for far-away objects (where high
    depth precision is not as visually important).
*/
public class ZBuffer {

    private short[] depthBuffer;
    private int width;
    private int height;

    /**
        Creates a new z-buffer with the specified width and height.
    */
    public ZBuffer(int width, int height) {
        depthBuffer = new short[width*height];
        this.width = width;
        this.height = height;
        clear();
    }


    /**
        Gets the width of this z-buffer.
    */
    public int getWidth() {
        return width;
    }


    /**
        Gets the height of this z-buffer.
    */
    public int getHeight() {
        return height;
    }


    /**
        Gets the array used for the depth buffer
    */
    public short[] getArray() {
        return depthBuffer;
    }


    /**
        Clears the z-buffer. All depth values are set to 0.
    */
    public void clear() {
        for (int i=0; i<depthBuffer.length; i++) {
            depthBuffer[i] = 0;
        }
    }


    /**
        Sets the depth of the pixel at at specified offset,
        overwriting its current depth.
    */
    public void setDepth(int offset, short depth) {
        depthBuffer[offset] = depth;
    }


    /**
        Checks the depth at the specified offset, and if the
        specified depth is lower (is greater than or equal to the
        current depth at the specified offset), then the depth is
        set and this method returns true. Otherwise, no action
        occurs and this method returns false.
    */
    public boolean checkDepth(int offset, short depth) {
        if (depth >= depthBuffer[offset]) {
            depthBuffer[offset] = depth;
            return true;
        }
        else {
            return false;
        }
    }

}