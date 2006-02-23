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

import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.GroupWrapper;

import java.awt.*;

/**
 * Created by  Administrator on Sep 11, 2004
 */
public class UTestListRenderer extends UTestFrameTest {

    public void testRenderer() throws InterruptedException {
        ListRenderer lr = new ListRenderer();
        Component comp;
        ContactWrapper bw;

        bw = ContactWrapper.create("blah", null);
        comp = lr.getListCellRendererComponent(null, bw, 1, false, false);
        add(comp);

        bw.getStatus().setOnline(true);
        comp = lr.getListCellRendererComponent(null, bw, 1, false, false);
        add(comp);

        bw.getStatus().setAway(true);
        comp = lr.getListCellRendererComponent(null, bw, 1, false, false);
        add(comp);

        comp = lr.getListCellRendererComponent(null, bw, 1, true, false);
        add(comp);

        GroupWrapper gw = GroupWrapper.create("group1");
        comp = lr.getListCellRendererComponent(null, gw, 1, false, false);
        add(comp);
        comp = lr.getListCellRendererComponent(null, gw, 1, true, false);
        add(comp);

        gw.swapShrunk();
        comp = lr.getListCellRendererComponent(null, gw, 1, false, false);
        add(comp);

        window.setVisible(true);
        Thread.sleep(10000);
    }

}
