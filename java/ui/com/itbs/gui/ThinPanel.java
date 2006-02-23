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
