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

import javax.swing.*;

/**
 * Created by: ARass  on  Date: Sep 24, 2004
 */
public class UTestPropertiesDialog extends UTestFrameTest {
    JFrame frame;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    public void setUp() throws Exception {
        super.setUp();
        Main.loadProperties();
        JLabel label = new JLabel();
        label.setText("mo");
        label.setText("<HTML>bo<u>b</u></HTML>");
        add(label);
    }

    public void testDialog() throws Exception {
        window.setVisible(true);

        JDialog dialog = new PropertiesDialog(frame);
        dialog.setVisible(true);
    }
}
