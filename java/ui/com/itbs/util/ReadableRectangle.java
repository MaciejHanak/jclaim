package com.itbs.util;

import java.awt.*;
import java.util.StringTokenizer;

/**
 * 
 * @author Alex Rass
 * @since Sep 16, 2004
 */
public class ReadableRectangle extends Rectangle {
    public ReadableRectangle(Point xy, Dimension size) {
        super(xy, size);
    }
    public ReadableRectangle(Rectangle defaultValue) {
        super(defaultValue);
    }
    public ReadableRectangle(String line, Rectangle defaultValue) {
        int x,y,w,h;
        StringTokenizer tzr = new StringTokenizer(line, " \t\n\r\f,");
        x = getNum(tzr, defaultValue.x);
        y = getNum(tzr, defaultValue.y);
        w = getNum(tzr, defaultValue.width);
        h = getNum(tzr, defaultValue.height);
        setBounds(x,y,w,h);
    }

    private static int getNum(StringTokenizer tokenizer, int defaultValue) {
        if (tokenizer.hasMoreElements()) {
            try {
                return Integer.parseInt(tokenizer.nextToken());
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    public String toString() {
        return getX() + ", " + getY() + ", " + getWidth() + ", " + getHeight();
    }

}
