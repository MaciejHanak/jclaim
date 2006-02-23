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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created by  Administrator on Sep 12, 2004
 */
public class UTestBetterTextPane extends UTestFrameTest {
    boolean trigger;
    /**
     * Didn't feel like working the robot. this unit test is a hands on one.
     * @throws InterruptedException stuff broke
     */
    public void testTextPane() throws InterruptedException {
        Action action = new ActionAdapter("nm", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                trigger = true;
                System.out.println("Fired.");
            }
        });
        BetterTextPane pane = new BetterTextPane(action);
        pane.addModifier(KeyEvent.SHIFT_DOWN_MASK);
        pane.addModifier(KeyEvent.CTRL_DOWN_MASK);
        add(pane);
        window.setVisible(true);
        waitForMe(50);
        System.out.println(""+ trigger);
    }
}
