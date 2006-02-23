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

package com.itbs.aimcer.gui.order;

import com.itbs.aimcer.Logger;
import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.MessageWindow;
import com.itbs.aimcer.gui.UTestFakeConnection;
import com.itbs.aimcer.gui.UTestFrameTest;

import javax.swing.*;
import java.util.Date;

/**
 * @author Created by  Administrator on Oct 21, 2004
 */
public class UTestOrderEntryPanel extends UTestFrameTest {
    public void testIOIPanel() throws Exception {
        final String name = "utest_bob";
        Main.logger = new Logger(ClientProperties.INSTANCE.getLogPath());
        UTestFakeConnection fc = new UTestFakeConnection();
        fc.assignContactFactory(Main.standardContactFactory);
        fc.assignGroupFactory(Main.standardGroupFactory);
        ContactWrapper cw = ContactWrapper.create(name, new UTestFakeConnection());
        MessageWindow messageWindow = MessageWindow.openWindow(cw, true);

        Main.logger = new Logger(ClientProperties.INSTANCE.getLogPath());
        JPanel panel = new IOIEntryPanel(new UTestFakeConnection(), cw, messageWindow);
        add(panel);
        window.pack();
        window.setVisible(true);
        waitForMe(60);
    }

    public void testPanel() throws Exception {
        final String name = "utest_bob";
        Main.logger = new Logger(ClientProperties.INSTANCE.getLogPath());
        UTestFakeConnection fc = new UTestFakeConnection();
        fc.assignContactFactory(Main.standardContactFactory);
        fc.assignGroupFactory(Main.standardGroupFactory);
        ContactWrapper cw = ContactWrapper.create(name, new UTestFakeConnection());
        MessageWindow messageWindow = MessageWindow.openWindow(cw, true);

        Main.logger = new Logger(ClientProperties.INSTANCE.getLogPath());
        JPanel panel = new OrderEntryPanel(new UTestFakeConnection(), cw, messageWindow);
        add(panel);
        window.pack();
        window.setVisible(true);
        waitForMe(60);
    }


    public void testIOIUpdate() throws Exception {
        OrderEntryPanel.IOIUpdateDialog dlg = new OrderEntryPanel.IOIUpdateDialog(null,
                new OrderEntryItem(new Date(), "from", "to", "sym", "1.0", "DAY", "200", "Comment", "buy", null, Boolean.FALSE, Boolean.FALSE));
        dlg.setVisible(true);
        waitForMe(60);
    }
}
