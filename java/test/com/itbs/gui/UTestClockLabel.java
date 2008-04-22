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
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author  Alex Rass
 * @since Sep 12, 2006
 */
public class UTestClockLabel extends UTestFrameTest {
    boolean trigger;
    /**
     * Didn't feel like working the robot. this unit test is a hands on one.
     * @throws InterruptedException stuff broke
     */
    public void testTextPane() throws InterruptedException {
        final JLabel label = new ClockLabel();
        Action action = new ActionAdapter("nm", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                label.setVisible(false);
                System.out.println("Fired.");
            }
        });
//        label.setText("blah");
        add(label);
        JButton button = new JButton(action);
        button.setToolTipText("blah");
        add(button);
        window.setVisible(true);
        waitForMe(50);
        System.out.println(""+ trigger);
    }
    /**
     * Didn't feel like working the robot. this unit test is a hands on one.
     * @throws InterruptedException stuff broke
     */
    public void testOffsetLabel() throws InterruptedException {
        final JLabel plainLabel = new JLabel("Blah");
        final int WIDTH_INC = 3;
        final JLabel offsetLabel = new JLabel("Blah") {
            // -------------- This removes the need for a whole panel.
            /**
             * Up the width.
             * @return new width.
             */
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                dim.width += WIDTH_INC;
                return dim;
            }

            protected void paintComponent(Graphics g) {
                g.translate(WIDTH_INC, 0);
                super.paintComponent(g);
            }

        };
        window.setLayout(new VerticalLayout());
        add(plainLabel);
        add(offsetLabel);
        window.setVisible(true);
        waitForMe(50);
        System.out.println(""+ trigger);
    }
}
