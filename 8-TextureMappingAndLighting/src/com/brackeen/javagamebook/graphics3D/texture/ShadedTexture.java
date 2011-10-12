package com.brackeen.javagamebook.graphics3D.texture;

import java.awt.Color;
import java.awt.image.IndexColorModel;

/**
    The ShadedTexture class is a Texture that has multiple
    shades. The texture source image is stored as a 8-bit image
    with a palette for every shade.
*/
public final class ShadedTexture extends Texture {

    public static final int NUM_SHADE_LEVELS = 64;
    public static final int MAX_LEVEL = NUM_SHADE_LEVELS-1;

    private static final int PALETTE_SIZE_BITS = 8;
    private static final int PALETTE_SIZE = 1 << PALETTE_SIZE_BITS;

    private byte[] buffer;
    private IndexColorModel palette;
    private short[] shadeTable;
    private int defaultShadeLevel;
    private int widthBits;
    private int widthMask;
    private int heightBits;
    private int heightMask;

    // the row set in setCurrRow and used in getColorCurrRow
    private int currRow;

    /**
        Creates a new ShadedTexture from the specified 8-bit image
        buffer and palette. The width of the bitmap is 2 to the
        power of widthBits, or (1 << widthBits). Likewise, the
        height of the bitmap is 2 to the power of heightBits, or
        (1 << heightBits). The texture is shaded from it's
        original color to black.
    */
    public ShadedTexture(byte[] buffer,
        int widthBits, int heightBits,
        IndexColorModel palette)
    {
        this(buffer, widthBits, heightBits, palette, Color.BLACK);
    }


    /**
        Creates a new ShadedTexture from the specified 8-bit image
        buffer, palette, and target shaded. The width of the
        bitmap is 2 to the power of widthBits, or (1 << widthBits).
        Likewise, the height of the bitmap is 2 to the power of
        heightBits, or (1 << heightBits). The texture is shaded
        from it's original color to the target shade.
    */
    public ShadedTexture(byte[] buffer,
        int widthBits, int heightBits,
        IndexColorModel palette, Color targetShade)
    {
        super(1 << widthBits, 1 << heightBits);
        this.buffer = buffer;
        this.widthBits = widthBits;
        this.heightBits = heightBits;
        this.widthMask = getWidth() - 1;
        this.heightMask = getHeight() - 1;
        this.buffer = buffer;
        this.palette = palette;
        defaultShadeLevel = MAX_LEVEL;

        makeShadeTable(targetShade);
    }


    /**
        Creates the shade table for this ShadedTexture. Each entry
        in the palette is shaded from the original color to the
        specified target color.
    */
    public void makeShadeTable(Color targetShade) {

        shadeTable = new short[NUM_SHADE_LEVELS*PALETTE_SIZE];

        for (int level=0; level<NUM_SHADE_LEVELS; level++) {
            for (int i=0; i<palette.getMapSize(); i++) {
                int red = calcColor(palette.getRed(i),
                    targetShade.getRed(), level);
                int green = calcColor(palette.getGreen(i),
                    targetShade.getGreen(), level);
                int blue = calcColor(palette.getBlue(i),
                    targetShade.getBlue(), level);

                int index = level * PALETTE_SIZE + i;
                // RGB 5:6:5
                shadeTable[index] = (short)(
                            ((red >> 3) << 11) |
                            ((green >> 2) << 5) |
                            (blue >> 3));
            }
        }
    }

    private int calcColor(int palColor, int target, int level) {
        return (palColor - target) * (level+1) /
            NUM_SHADE_LEVELS + target;
    }


    /**
        Sets the default shade level that is used when getColor()
        is called.
    */
    public void setDefaultShadeLevel(int level) {
        defaultShadeLevel = level;
    }


    /**
        Gets the default shade level that is used when getColor()
        is called.
    */
    public int getDefaultShadeLevel() {
        return defaultShadeLevel;
    }


    /**
        Gets the 16-bit color of this Texture at the specified
        (x,y) location, using the default shade level.
    */
    public short getColor(int x, int y) {
        return getColor(x, y, defaultShadeLevel);
    }


    /**
        Gets the 16-bit color of this Texture at the specified
        (x,y) location, using the specified shade level.
    */
    public short getColor(int x, int y, int shadeLevel) {
        return shadeTable[(shadeLevel << PALETTE_SIZE_BITS) |
            (0xff & buffer[
            (x & widthMask) |
            ((y & heightMask) << widthBits)])];
    }


    /**
        Sets the current row for getColorCurrRow(). Pre-calculates
        the offset for this row.
    */
    public void setCurrRow(int y) {
        currRow = (y & heightMask) << widthBits;
    }


    /**
        Gets the color at the specified x location at the specified
        shade level. The current row defined in setCurrRow is
        used.
    */
    public short getColorCurrRow(int x, int shadeLevel) {
        return shadeTable[(shadeLevel << PALETTE_SIZE_BITS) |
            (0xff & buffer[(x & widthMask) | currRow])];
    }

}
