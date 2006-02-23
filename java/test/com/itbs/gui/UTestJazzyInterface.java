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

import com.itbs.aimcer.gui.UTestFrameTest;

import javax.swing.*;
import java.awt.*;

/**
 * @author Created by  Administrator on Nov 7, 2004
 */
public class UTestJazzyInterface extends UTestFrameTest {
    JTextArea comp = new JTextArea(5, 20);




    public void testMain() throws Exception {
        comp.setText("This is the story\nof the hare who\nlost his spectacles.");
        JazzyInterface.create().addSpellCheckComponent(comp);

        window.add(new JScrollPane(comp), BorderLayout.CENTER);

        window.setSize(500, 400);
        window.setVisible(true);
        waitForMe(10000);
    }

}
