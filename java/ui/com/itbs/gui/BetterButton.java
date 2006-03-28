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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Listens to Enter for clicking.
 * No big deal, but convenient.
 * @author Ales Rass  
 * @since Date: Mar 25, 2004
 */
public class BetterButton extends JButton {
    Insets insets;
    public BetterButton() {
    }
    
    public BetterButton(String name) {
        super(name);
    }

    public BetterButton(Action a) {
        super(a);
        if (getText() == null && getIcon() != null) {
            insets = new Insets(super.getInsets().top, super.getInsets().top, super.getInsets().bottom, super.getInsets().top);
        }
    }

    /**
     * If a border has been set on this component, returns the
     * border's insets; otherwise calls <code>super.getInsets</code>.
     *
     * @return the value of the insets property
     * @see #setBorder
     */
    public Insets getInsets() {
        if (insets == null)
            return super.getInsets();
        return insets;
    }

    /**
     * Makes the enter move to next item.
     * @param e event
     */
    protected void processKeyEvent(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER
                && e.getID() == KeyEvent.KEY_PRESSED)
        {
            doClick();
        }
        else
        {
            super.processKeyEvent(e);
        }
    }
}
