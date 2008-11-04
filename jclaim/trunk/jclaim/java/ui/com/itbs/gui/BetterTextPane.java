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
import javax.swing.event.CaretListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Alex Rass on Sep 12, 2004
 */
public class BetterTextPane extends JTextPane {
    private Action action;
    private int modifier;
    CaretListener autoCopy;

    public BetterTextPane() {
        super();
        init();
    }

    public BetterTextPane(Action act) {
        super();
        action = act;
        init();
    }

    void init() {
        // setup the auto-copy
        autoCopy = new BetterTextField.AutoCopyCaretListener(this);
        // setup the auto-copy via mouse\
        BetterTextField.typicalInit(this);
    }


    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Sets whether or not this component is enabled.
     * A component that is enabled may respond to user input,
     * while a component that is not enabled cannot respond to
     * user input.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    public void setEditable(boolean enabled) {
        super.setEditable(enabled);
        if (enabled)
            removeCaretListener(autoCopy);
        else
            addCaretListener(autoCopy);
    }

    public void addModifier(int modifier) {
        this.modifier += modifier;
    }

    // todo replace with input maps.
    protected void processKeyEvent(KeyEvent e) {
        if (action != null && e.getKeyCode() == KeyEvent.VK_ENTER
                && e.getID() == KeyEvent.KEY_PRESSED && (modifier == 0 || (e.getModifiersEx() & modifier) > 0)) {
            action.actionPerformed(new ActionEvent(this, 0, action.getValue(Action.NAME).toString()));
//        } else if (keyTripped(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK, e)) {
//            paste();
//        } else if (keyTripped(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK, e)) {
//            copy();
//        } else if (keyTripped(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK, e)) {
//            cut();
        } else {
            super.processKeyEvent(e);
        }
    }

    /**
     * Returns true if the test matched.
     *
     * @param key       to test
     * @param modifiers to test
     * @param event     that came in
     * @return true if this is the right key
     */
    static boolean keyTripped(int key, int modifiers, KeyEvent event) {
        return event.getKeyCode() == key && event.getID() == KeyEvent.KEY_PRESSED && (event.getModifiersEx() & modifiers) > 0;
    }


} // class BetterTextPane