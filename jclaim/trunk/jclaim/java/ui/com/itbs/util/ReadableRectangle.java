/*
 * Copyright (c) 2006, ITBS LLC. All Rights Reserved.
 *
 *     This file is part of JClaim.
 *
 *     JClaim is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; version 2 of the License.
 *
 *     JClaim is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JClaim; if not, find it at gnu.org or write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.itbs.util;

import java.awt.*;
import java.util.StringTokenizer;

/**
 * 
 * @author Alex Rass
 * @since Sep 16, 2004
 */
public class ReadableRectangle extends Rectangle {
    public ReadableRectangle() {
    }
    
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
