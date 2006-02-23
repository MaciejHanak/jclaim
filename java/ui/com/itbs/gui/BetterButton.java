package com.itbs.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Listens to Enter for clicking.
 * Created by: ARass  on  Date: Mar 25, 2004
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
