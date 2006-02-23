package com.itbs.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Created by Alex Rass on May 28, 2004
 */
public class GradientPanel extends JPanel {
    public static final int ALIGN_LEFT_RIGHT = 0;
    public static final int ALIGN_TOP_DOWN = 1;
    public static final int ALIGN_DIAGNALY = 2;

    private int alignment = ALIGN_TOP_DOWN;
    private Color color1 = Color.WHITE, color2 = Color.LIGHT_GRAY;
    public GradientPanel() {
        setOpaque(false);
    }

    public GradientPanel(LayoutManager manager) {
        super(manager);
        setOpaque(false);
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setColors(Color from, Color to) {
        color1 = from;
        color2 = to;
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        if (alignment == ALIGN_LEFT_RIGHT)
            g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), 0, color2, false));
        else if (alignment == ALIGN_TOP_DOWN)
            g2d.setPaint(new GradientPaint(0, (getHeight() + 1) / 2, color1, 0, getHeight(), color2, false));
        else if (alignment == ALIGN_DIAGNALY)
            g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2, false));
        g2d.fillRect(0,0,getWidth(),getHeight());
        super.paint(g);
    }

}
