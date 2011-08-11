

import java.awt.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class ImageSpeedTest extends JFrame {

    public static void main(String args[]) {

        DisplayMode displayMode;

        if (args.length == 3) {
            displayMode = new DisplayMode(
                Integer.parseInt(args[0]),
                Integer.parseInt(args[1]),
                Integer.parseInt(args[2]),
                DisplayMode.REFRESH_RATE_UNKNOWN);
        }
        else {
            displayMode = new DisplayMode(800, 600, 16,
                DisplayMode.REFRESH_RATE_UNKNOWN);
        }

        ImageSpeedTest test = new ImageSpeedTest();
        test.run(displayMode);
    }

    private static final int FONT_SIZE = 24;
    private static final long TIME_PER_IMAGE = 1500;

    private ScreenManager screen;
    private Image bgImage;
    private Image opaqueImage;
    private Image transparentImage;
    private Image translucentImage;
    private Image antiAliasedImage;
    private boolean imagesLoaded;

    public void run(DisplayMode displayMode) {
        setBackground(Color.blue);
        setForeground(Color.white);
        setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
        imagesLoaded = false;

        screen = new ScreenManager();
        try {
            screen.setFullScreen(displayMode);
            synchronized (this) {
                loadImages();
                // wait for test to complete
                try {
                    wait();
                }
                catch (InterruptedException ex) { }
            }
        }
        finally {
            screen.restoreScreen();
        }
    }


    public void loadImages() {
        bgImage = loadImage("images/background.jpg");
        opaqueImage = loadImage("images/opaque.png");
        transparentImage = loadImage("images/transparent.png");
        translucentImage = loadImage("images/translucent.png");
        antiAliasedImage = loadImage("images/antialiased.png");
        imagesLoaded = true;
        // signal to AWT to repaint this window
        repaint();
    }


    private final Image loadImage(String fileName) {
        return new ImageIcon(fileName).getImage();
    }


    public void paint(Graphics g) {
        // set text anti-aliasing
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        // draw images
        if (imagesLoaded) {
            drawImage(g, opaqueImage, "Opaque");
            drawImage(g, transparentImage, "Transparent");
            drawImage(g, translucentImage, "Translucent");
            drawImage(g, antiAliasedImage,
              "Translucent (Anti-Aliased)");

            // notify that the test is complete
            synchronized (this) {
                notify();
            }
        }
        else {
            g.drawString("Loading Images...", 5, FONT_SIZE);
        }
    }


    public void drawImage(Graphics g, Image image, String name) {
        int width = screen.getFullScreenWindow().getWidth() -
            image.getWidth(null);
        int height = screen.getFullScreenWindow().getHeight() -
            image.getHeight(null);
        int numImages = 0;

        g.drawImage(bgImage, 0, 0, null);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime
            < TIME_PER_IMAGE)
        {
            int x = Math.round((float)Math.random() * width);
            int y = Math.round((float)Math.random() * height);
            g.drawImage(image, x, y, null);
            numImages++;
        }
        long time = System.currentTimeMillis() - startTime;
        float speed = numImages * 1000f / time;
        System.out.println(name + ": " + speed + " images/sec");

    }

}
