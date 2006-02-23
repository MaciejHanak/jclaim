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

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;

/**
 * @author Created by  Administrator on Oct 8, 2004
 */
public class UTestFrameTest extends TestCase {
    public JFrame window;
    public void setUp() throws Exception {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.getContentPane().setLayout(new FlowLayout());
        window.setSize(100, 200);
    }

    public void add(Component comp) {
        window.getContentPane().add(comp);
    }

    public void tearDown() throws Exception {
        window.dispose();
    }

    protected void waitForMe(int secondsToWait) throws InterruptedException {
        for(int seconds = 0; seconds < secondsToWait && window.isVisible(); seconds++) {
            Thread.sleep(1000);
        }
    }
}
