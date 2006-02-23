package com.itbs.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alex Rass on Oct 9, 2004
 */
public class ThinPanel extends JPanel {
    /**
     * Creates a quick thin panel that holds a few things right next to each other.
     * @param comp1 comp1
     * @param comp2 comp2
     */
    public ThinPanel(JComponent comp1, JComponent comp2) {
        this(comp1);
        add(comp2);
    }
    /**
     * Creates a quick thin panel that holds a few things right next to each other.
     * @param comp comp
     */
    public ThinPanel(JComponent comp) {
        super(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setOpaque(false);
        add(comp);
    }


}
