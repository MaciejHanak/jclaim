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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author Alex Rass
 * @since Apr 20, 2006
 */
public class BetterTextPopupMenu extends JPopupMenu {
    protected int carret;
    protected Object source;

    Object getSource() {
        return source;
    }

    public int getCarret() {
        return carret;
    }

    @Override
    public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                && e.getID() == KeyEvent.KEY_PRESSED
            && (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK & KeyEvent.CTRL_DOWN_MASK & KeyEvent.ALT_DOWN_MASK) == 0) {
                setVisible(false);
                e.consume();
//        } else {
//            processKeyEvent(e, path, manager);
        }
    }

    public void show(Component invoker, int x, int y, JTextComponent parent, int carret) throws BadLocationException {
        this.carret = carret;
        source = parent;
    }

}
