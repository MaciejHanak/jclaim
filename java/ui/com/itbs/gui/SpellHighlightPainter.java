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

package com.itbs.gui;

import javax.swing.text.*;
import java.awt.*;
import java.util.logging.Level;

/**
 * Simple highlight painter that can underline a highlighted area with red color.
 * Used by the spell checker.
 * todo make an wavy underline (use the shape to paint with)
 * To use:
 * Create the DefaultHighlighter and then use
 * public Object addHighlight(int p0, int p1, Highlighter.HighlightPainter p) throws BadLocationException {
 *
 * @author Alex Rass
 * @since Nov 7, 2004
 */
public class SpellHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
    Color color = Color.RED;
    public static Highlighter.HighlightPainter singleton = new SpellHighlightPainter();

    /**
     * No need to create, just use the singleton.
     */
    private SpellHighlightPainter() {
        super(Color.red);
    }


    /*
     * Paints a highlight.
     *
     * @param g      the graphics context
     * @param offs0  the starting model offset >= 0
     * @param offs1  the ending model offset >= offs1
     * @param bounds the bounding box for the highlight
     * @param c      the editor


    public void paintZ(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
        Rectangle alloc = bounds.getBounds();
        try {
            // --- determine locations ---
            TextUI mapper = c.getUI();
            Rectangle p0 = mapper.modelToView(c, offs0);
            Rectangle p1 = mapper.modelToView(c, offs1);

            // --- render ---
            g.setColor(color);
            if (p0.y == p1.y) {
                // same line, render a rectangle
                Rectangle r = p0.union(p1);
//                g.drawRect(r.x, r.y, r.width, r.height);
                g.drawLine(r.x, r.y + r.height, r.x + r.width, r.y + r.height);
            } else {
//                 different lines
                int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                g.drawLine(p0.x, p0.y + p0.height, p0.x + p0ToMarginWidth, p0.y+p0.height);
                // todo this wouldn't work for highlighting more than 2 lines.  fix later
                if ((p0.y + p0.height) != p1.y) {
                    g.drawLine(alloc.x, p0.y + p0.height + p1.y - (p0.y + p0.height),
                            alloc.x + alloc.width, alloc.x + p1.y - (p0.y + p0.height));
                }
                g.drawLine(alloc.x, p1.y + p1.height, alloc.x + (p1.x - alloc.x), p1.y + p1.height);
            }
        } catch (BadLocationException e) {
            log.log(Level.SEVERE, "", e);// can't render
        }

//        return null;
    }
     */

    /** Vertical shift up */
    public static final int SHIFT = 1;
    /**
     * Paints a portion of a highlight.
     *
     * @param g      the graphics context
     * @param offs0  the starting model offset >= 0
     * @param offs1  the ending model offset >= offs1
     * @param bounds the bounding box of the view, which is not
     *               necessarily the region to paint.
     * @param c      the editor
     * @param view   View painting for
     * @return region drawing occured in
     */
    public Shape paintLayer(Graphics g, int offs0, int offs1,
                            Shape bounds, JTextComponent c, View view) {
        Color color = getColor();
        if (color == null) {
            g.setColor(c.getSelectionColor());
        } else {
            g.setColor(color);
        }
        if (offs0 == view.getStartOffset() &&
                offs1 == view.getEndOffset()) {
            // Contained in view, can just use bounds.
            Rectangle alloc;
            if (bounds instanceof Rectangle) {
                alloc = (Rectangle) bounds;
            } else {
                alloc = bounds.getBounds();
            }
            g.drawLine(alloc.x, alloc.y + alloc.height - SHIFT , alloc.x + alloc.width, alloc.y + alloc.height - SHIFT);
            return alloc;
        } else {
            // Should only render part of View.
            try {
                // --- determine locations ---
                Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                        offs1, Position.Bias.Backward,
                        bounds);
                Rectangle r = (shape instanceof Rectangle) ?
                        (Rectangle) shape : shape.getBounds();
                g.drawLine(r.x, r.y + r.height - SHIFT, r.x + r.width, r.y + r.height - SHIFT);
                return r;
            } catch (BadLocationException e) {
                // can't render
            }
        }
        // Only if exception
        return null;
    }




} // class SpellHighlightPainter
