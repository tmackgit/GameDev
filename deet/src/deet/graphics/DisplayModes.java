package deet.graphics;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Window;

public class DisplayModes {
	
	// various lists of modes, ordered by preference
    protected static final DisplayMode[] MID_RES_MODES = {
        new DisplayMode(800, 600, 16, 0),
        new DisplayMode(800, 600, 32, 0),
        new DisplayMode(800, 600, 24, 0),
        new DisplayMode(640, 480, 16, 0),
        new DisplayMode(640, 480, 32, 0),
        new DisplayMode(640, 480, 24, 0),
        new DisplayMode(1024, 768, 16, 0),
        new DisplayMode(1024, 768, 32, 0),
        new DisplayMode(1024, 768, 24, 0),
    };

    protected static final DisplayMode[] LOW_RES_MODES = {
        new DisplayMode(640, 480, 16, 0),
        new DisplayMode(640, 480, 32, 0),
        new DisplayMode(640, 480, 24, 0),
        new DisplayMode(800, 600, 16, 0),
        new DisplayMode(800, 600, 32, 0),
        new DisplayMode(800, 600, 24, 0),
        new DisplayMode(1024, 768, 16, 0),
        new DisplayMode(1024, 768, 32, 0),
        new DisplayMode(1024, 768, 24, 0),
    };

    protected static final DisplayMode[] VERY_LOW_RES_MODES = {
        new DisplayMode(320, 240, 16, 0),
        new DisplayMode(400, 300, 16, 0),
        new DisplayMode(512, 384, 16, 0),
        new DisplayMode(640, 480, 16, 0),
        new DisplayMode(800, 600, 16, 0),
    };    

}
