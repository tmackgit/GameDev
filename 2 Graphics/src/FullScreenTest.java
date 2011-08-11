

import java.awt.*;

import javax.swing.JFrame;

public class FullScreenTest extends JFrame {
	public static void main(String[] args) {
		DisplayMode displayMode;
		
		if (args.length == 3) {
			displayMode = new DisplayMode(
				Integer.parseInt(args[0]), 
				Integer.parseInt(args[1]),
				Integer.parseInt(args[2]),
				DisplayMode.REFRESH_RATE_UNKNOWN);
		}
		else {
			displayMode = new DisplayMode(800, 600, 16, DisplayMode.REFRESH_RATE_UNKNOWN);
		}
		
		FullScreenTest test = new FullScreenTest();
		test.run(displayMode);
	}
	
	public static final long DEMO_TIME = 5000;
	
	public void run(DisplayMode displayMode) {
		setBackground(Color.blue);
		setForeground(Color.white);
		setFont(new Font("Dialog", Font.PLAIN, 24));
		
		ScreenManager screen = new ScreenManager();
		try {
			screen.setFullScreen(displayMode);
			try {
				Thread.sleep(DEMO_TIME);
			}
			catch (InterruptedException ex) {}
		}
		finally {
			screen.restoreScreen();
		}
	}
	
	public void paint(Graphics g) {
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		g.drawString("Hello World", 20, 20);
	}
}
