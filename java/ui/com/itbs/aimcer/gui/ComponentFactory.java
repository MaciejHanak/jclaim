package com.itbs.aimcer.gui;

import javax.swing.*;
import javax.swing.plaf.metal.MetalBorders;
import java.awt.*;

/**
 * @author Created by Alex Rass on Sep 9, 2004
 */
public class ComponentFactory {
    public static JComponent getTopBar(String displayedText) {
        JLabel label = new JLabel(displayedText, JLabel.CENTER);
        label.setBorder(new MetalBorders.Flush3DBorder());
        label.setBackground(Color.DARK_GRAY.brighter());
        label.setForeground(Color.YELLOW);
        label.setOpaque(true);
        label.setFont(label.getFont().deriveFont(16F));
        return label;
    }
}
