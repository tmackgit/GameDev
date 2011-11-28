package com.brackeen.javagamebook.shooter3D;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import com.brackeen.javagamebook.graphics3D.Overlay;
import com.brackeen.javagamebook.math3D.ViewWindow;

public class HeadsUpDisplay implements Overlay {

    // increase health display by 20 points per second
    private static final float DISPLAY_INC_RATE = 0.04f;

    private Player player;
    private float displayedHealth;
    private Font font;

    public HeadsUpDisplay(Player player) {
        this.player = player;
        displayedHealth = 0;
    }

    public void update(long elapsedTime) {
        // increase or descrease displayedHealth a small amount
        // at a time, instead of just setting it to the player's
        // health.
        float actualHealth = player.getHealth();
        if (actualHealth > displayedHealth) {
            displayedHealth = Math.min(actualHealth,
                displayedHealth + elapsedTime * DISPLAY_INC_RATE);
        }
        else if (actualHealth < displayedHealth) {
            displayedHealth = Math.max(actualHealth,
                displayedHealth - elapsedTime * DISPLAY_INC_RATE);
        }
    }

    public void draw(Graphics2D g, ViewWindow window) {

        // set the font (scaled for this view window)
        int fontHeight = Math.max(9, window.getHeight() / 20);
        int spacing = fontHeight / 5;
        if (font == null || fontHeight != font.getSize()) {
            font = new Font("Dialog", Font.PLAIN, fontHeight);
        }
        g.setFont(font);
        g.translate(window.getLeftOffset(), window.getTopOffset());

        // draw health value (number)
        String str = Integer.toString(Math.round(displayedHealth));
        Rectangle2D strBounds = font.getStringBounds(str,
            g.getFontRenderContext());
        g.setColor(Color.WHITE);
        g.drawString(str, spacing, (int)strBounds.getHeight());

        // draw health bar
        Rectangle bar = new Rectangle(
            (int)strBounds.getWidth() + spacing * 2,
            (int)strBounds.getHeight() / 2,
            window.getWidth() / 4,
            window.getHeight() / 60);
        g.setColor(Color.GRAY);
        g.fill(bar);

        // draw highlighted part of health bar
        bar.width = Math.round(bar.width *
            displayedHealth / player.getMaxHealth());
        g.setColor(Color.WHITE);
        g.fill(bar);
    }

    public boolean isEnabled() {
        return (player != null &&
            (player.isAlive() || displayedHealth > 0));
    }
}
