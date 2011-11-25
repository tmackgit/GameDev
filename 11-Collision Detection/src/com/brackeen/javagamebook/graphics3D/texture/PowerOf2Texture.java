package com.brackeen.javagamebook.graphics3D.texture;

/**
    The PowerOf2Texture class is a Texture with a width and height
    that are a power of 2 (32, 128, etc.).
*/
public final class PowerOf2Texture extends Texture {

    private short[] buffer;
    private int widthBits;
    private int widthMask;
    private int heightBits;
    private int heightMask;

    /**
        Creates a new PowerOf2Texture with the specified buffer.
        The width of the bitmap is 2 to the power of widthBits, or
        (1 << widthBits). Likewise, the height of the bitmap is 2
        to the power of heightBits, or (1 << heightBits).
    */
    public PowerOf2Texture(short[] buffer,
        int widthBits, int heightBits)
    {
        super(1 << widthBits, 1 << heightBits);
        this.buffer = buffer;
        this.widthBits = widthBits;
        this.heightBits = heightBits;
        this.widthMask = getWidth() - 1;
        this.heightMask = getHeight() - 1;
    }


    /**
        Gets the 16-bit color of the pixel at location (x,y) in
        the bitmap.
    */
    public short getColor(int x, int y) {
        return buffer[
            (x & widthMask) +
            ((y & heightMask) << widthBits)];
    }

}
