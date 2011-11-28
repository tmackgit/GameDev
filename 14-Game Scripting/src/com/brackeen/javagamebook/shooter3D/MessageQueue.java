package com.brackeen.javagamebook.shooter3D;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.geom.Rectangle2D;
import com.brackeen.javagamebook.graphics3D.Overlay;
import com.brackeen.javagamebook.math3D.ViewWindow;

public class MessageQueue implements Overlay {

    static class Message {
        String text;
        long remainingTime;
    }

    private static final long MESSAGE_TIME = 5000;
    private static final long MAX_SIZE = 10;

    private static MessageQueue instance;

    private List messages;
    private boolean debug;
    private Font font;

    public static synchronized MessageQueue getInstance() {
        if (instance == null) {
            instance = new MessageQueue();
        }
        return instance;
    }

    private MessageQueue() {
        messages = new LinkedList();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void debug(String text) {
        if (debug) {
            add(text);
        }
    }

    public boolean isEnabled() {
        return true;
    }

    public void add(String text) {
        Message message = new Message();
        message.text = text;
        message.remainingTime = MESSAGE_TIME;
        messages.add(message);
        if (messages.size() > MAX_SIZE) {
            messages.remove(0);
        }
    }

    public void update(long elapsedTime) {
        Iterator i = messages.iterator();
        while (i.hasNext()) {
            Message message = (Message)i.next();
            message.remainingTime-=elapsedTime;
            if (message.remainingTime < 0) {
                i.remove();
            }
        }
    }

    public void draw(Graphics2D g, ViewWindow window) {
        // set the font (scaled for this view window)
        int fontHeight = Math.max(9, window.getHeight() / 40);
        if (font == null || fontHeight != font.getSize()) {
            font = new Font("Dialog", Font.PLAIN, fontHeight);
        }
        g.setFont(font);

        int x = window.getLeftOffset() + window.getWidth() -
            fontHeight/4;
        int y = window.getTopOffset();


        g.setColor(Color.WHITE);

        Iterator i = messages.iterator();
        while (i.hasNext()) {
            String text = ((Message)i.next()).text;
            Rectangle2D displayBounds = font.getStringBounds(text,
                g.getFontRenderContext());
            y+=(int)displayBounds.getHeight();
            g.drawString(text,
                x - (int)displayBounds.getWidth(), y);
        }
    }
}
