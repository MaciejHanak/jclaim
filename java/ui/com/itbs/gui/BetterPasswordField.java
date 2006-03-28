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
import java.awt.event.KeyEvent;

/**
 * Gives password field all the same benefits of a {@link BetterTextField}.
 * @author Alex Rass
 * @since Date: Mar 25, 2004
 */
public class BetterPasswordField extends JPasswordField {
    public BetterPasswordField(int columns) {
        super(columns);
        init();
    }

    public BetterPasswordField(String password) {
        this(password, 5);
    }
    public BetterPasswordField(String password, int columns) {
        super(columns);
        setText(password);
        init();
    }

    private void init() {
        BetterTextField.typicalInit(this);
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
            if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == 0)
                transferFocus();
            else
                transferFocusBackward();
        }
        else
        {
            super.processKeyEvent(e);
        }
    }
}
