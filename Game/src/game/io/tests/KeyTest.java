package game.io.tests;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import game.graphics.*;
import game.core.*;


/**
    A simple keyboard test. Displays keys pressed and released to
    the screen. Useful for debugging key input, too.
*/
public class KeyTest extends GameCore implements KeyListener {

    public static void main(String[] args) {
        new KeyTest().run();
    }

    private LinkedList messages = new LinkedList();

    public void init() {
        super.init();

        Window window = screen.getFullScreenWindow();

        // allow input of the TAB key and other keys normally
        // used for focus traversal
        window.setFocusTraversalKeysEnabled(false);

        // register this object as a key listener for the window
        window.addKeyListener(this);

        addMessage("KeyInputTest. Press Escape to exit");
    }


    // a method from the KeyListner interface
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // exit the program
        if (keyCode == KeyEvent.VK_ESCAPE) {
             stop();
        }
        else {
            addMessage("Pressed: " +
                KeyEvent.getKeyText(keyCode));

            // make sure the key isn't processed for anything else
            e.consume();
        }
    }


    // a method from the KeyListner interface
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        addMessage("Released: " + KeyEvent.getKeyText(keyCode));

        // make sure the key isn't processed for anything else
        e.consume();
    }


    // a method from the KeyListener interface
    public void keyTyped(KeyEvent e) {
        // this is called after the key is released - ignore it
        // make sure the key isn't processed for anything else
        e.consume();
    }


    /**
        Add a message to the list of messages.
    */
    public synchronized void addMessage(String message) {
        messages.add(message);
        if (messages.size() >= screen.getHeight() / FONT_SIZE) {
            messages.remove(0);
        }
    }


    /**
        Draw the list of messages
    */
    public synchronized void draw(Graphics2D g) {

        Window window = screen.getFullScreenWindow();

        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // draw background
        g.setColor(window.getBackground());
        g.fillRect(0, 0, screen.getWidth(), screen.getHeight());

        // draw messages
        g.setColor(window.getForeground());
        int y = FONT_SIZE;
        for (int i=0; i<messages.size(); i++) {
            g.drawString((String)messages.get(i), 5, y);
            y+=FONT_SIZE;
        }
    }
}

