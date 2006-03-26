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

import com.itbs.aimcer.Logger;
import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.MessageImpl;
import junit.framework.TestCase;

import java.awt.*;

/**
 * Tests Message Window.
 * 
 * @author Alex Rass
 * @since Oct 6, 2004
 */
public class UTestMessageWindow extends TestCase {
    MessageWindow messageWindow;

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        if (messageWindow!=null)
            messageWindow.frame.dispose();
    }

    public static void monitorWindow(Window watchMe) throws InterruptedException {
        for(int  count = 0;  count < 500 && watchMe.isDisplayable(); count++) {
            Thread.sleep(1000);
        }
        if(watchMe.isDisplayable()) {
            watchMe.dispose();
        }
    }

    public void testDialog() throws Exception  {
/*
        JFrame fr = new JFrame("pt");
        fr.add(new JLabel("testing this out"));
        fr.setBounds(500,500,200, 700);
        fr.pack();
        fr.setVisible(true);
*/
        ClientProperties.INSTANCE.setShowOrderEntry(true);
        ContactWrapper cw = ContactWrapper.create("utest_bob", new UTestFakeConnection());
        Main.logger = new Logger(ClientProperties.INSTANCE.getLogPath());
        messageWindow = MessageWindow.openWindow(cw, true);
        monitorWindow(messageWindow.frame);
    }

    public static final int num = 100;
    public void XtestOpens() throws Exception  {
        UTestFakeConnection utfc = new UTestFakeConnection();
        utfc.addEventListener(MessageWindow.getConnectionEventListener());
        for ( int i=0; i<num; i++) {
            MessageImpl mesg = new MessageImpl(ContactWrapper.create(""+i, utfc), false, "blah " + i);
            utfc.sendMessage(mesg);
            utfc.sendMessage(mesg); // make sure we don't create more windows
        }
        Thread.sleep(60000);
        int count=0;
        for ( int i=0; i<num; i++) {
            MessageWindow mw = MessageWindow.findWindow(ContactWrapper.create(""+i, utfc));
            if (mw!=null)
              mw.closeWindow();
            else {
                count++;
                System.out.println("Failed to find " + i);
            }
        }
        System.out.println("Failed to find total " + count);
        Thread.sleep(60000);
    }
}
