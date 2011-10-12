package com.brackeen.javagamebook.graphics3D.texture;

import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
   The Texture class is an sabstract class that represents a
   16-bit color texture.
*/
public abstract class Texture {

    protected int width;
    protected int height;

    /**
        Creates a new Texture with the specified width and height.
    */
    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
    }


    /**
        Gets the width of this Texture.
    */
    public int getWidth() {
        return width;
    }


    /**
        Gets the height of this Texture.
    */
    public int getHeight() {
        return height;
    }


    /**
        Gets the 16-bit color of this Texture at the specified
        (x,y) location.
    */
    public abstract short getColor(int x, int y);


    /**
        Creates an unshaded Texture from the specified image file.
    */
    public static Texture createTexture(String filename) {
        return createTexture(filename, false);
    }


    /**
        Creates an Texture from the specified image file. If
        shaded is true, then a ShadedTexture is returned.
    */
    public static Texture createTexture(String filename,
        boolean shaded)
    {
        try {
            return createTexture(ImageIO.read(new File(filename)),
                shaded);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
        Creates an unshaded Texture from the specified image.
    */
    public static Texture createTexture(
        BufferedImage image)
    {
        return createTexture(image, false);
    }


    /**
        Creates an Texture from the specified image. If
        shaded is true, then a ShadedTexture is returned.
    */
    public static Texture createTexture(
        BufferedImage image, boolean shaded)
    {
        int type = image.getType();
        int width = image.getWidth();
        int height = image.getHeight();

        if (!isPowerOfTwo(width) || !isPowerOfTwo(height)) {
            throw new IllegalArgumentException(
                "Size of texture must be a power of two.");
        }

        if (shaded) {
            // convert image to an indexed image
            if (type != BufferedImage.TYPE_BYTE_INDEXED) {
                System.out.println("Warning: image converted to " +
                    "256-color indexed image. Some quality may " +
                    "be lost.");
                BufferedImage newImage = new BufferedImage(
                    image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_BYTE_INDEXED);
                Graphics2D g = newImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = newImage;
            }
            DataBuffer dest = image.getRaster().getDataBuffer();
            return new ShadedTexture(
                ((DataBufferByte)dest).getData(),
                countbits(width-1), countbits(height-1),
                (IndexColorModel)image.getColorModel());
        }
        else {
            // convert image to an 16-bit image
            if (type != BufferedImage.TYPE_USHORT_565_RGB) {
                BufferedImage newImage = new BufferedImage(
                    image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_USHORT_565_RGB);
                Graphics2D g = newImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = newImage;
            }

            DataBuffer dest = image.getRaster().getDataBuffer();
            return new PowerOf2Texture(
                ((DataBufferUShort)dest).getData(),
                countbits(width-1), countbits(height-1));
        }
    }


    /**
        Returns true if the specified number is a power of 2.
    */
    public static boolean isPowerOfTwo(int n) {
        return ((n & (n-1)) == 0);
    }


    /**
        Counts the number of "on" bits in an integer.
    */
    public static int countbits(int n) {
        int count = 0;
        while (n > 0) {
            count+=(n & 1);
            n>>=1;
        }
        return count;
    }
}
