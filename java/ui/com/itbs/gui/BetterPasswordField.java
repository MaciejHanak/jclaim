package com.itbs.gui;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by: ARass  on  Date: Mar 25, 2004
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
