package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ClientProperties;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalBorders;
import java.awt.*;

/**
 * @author Alex Rass
 * @since Sep 9, 2004
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


    public static void fixWidget(final JComponent comp, String title) {
        comp.setFont(new Font("Arial", Font.PLAIN, ClientProperties.INSTANCE.getFontSize()+1));
        comp.setOpaque(false);
        comp.setBorder(new TitledBorder(comp.getBorder(), title, TitledBorder.CENTER, TitledBorder.ABOVE_TOP));
    }
}
