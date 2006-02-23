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
