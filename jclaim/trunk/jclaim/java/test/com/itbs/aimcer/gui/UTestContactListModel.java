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
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.gui.userlist.ContactListModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stress Test for Conltact List.
 * 
 * @author Alex Rass
 * @since Sep 11, 2004
 */
public class UTestContactListModel extends UTestFrameTest {
    private static final Logger log = Logger.getLogger(UTestContactListModel.class.getName());
    Connection connection = new UTestFakeConnection();
    public void testRenderer() throws InterruptedException {
        java.util.List <Connection> connections = new ArrayList<Connection>();
        connections.add(connection);
        final ContactListModel dataModel = ContactListModel.getInstance();
        JList list = new JList(dataModel);
        window.getContentPane().setLayout(new BorderLayout());
        add(list);
        window.setVisible(true);
        Thread.sleep(500); // settle
        new EvilRunner(dataModel).start();
        waitForMe(10);
    }

    class EvilRunner extends Thread {
        private static final int REPETITIONS = 100000;
        ContactListModel dataModel;

        public EvilRunner(ContactListModel dataModel) {
            this.dataModel = dataModel;
        }

        public void run() {
            try {
                final GroupWrapper group = (GroupWrapper) connection.getGroupFactory().create("Group");
                connection.getGroupList().add(group);
                ContactWrapper b1,b2,b3,b4;
                b1 = ContactWrapper.create("Bob", connection);
                b2 = ContactWrapper.create("Steve", connection);
                b3 = ContactWrapper.create("Jill", connection);
                b4 = ContactWrapper.create("Jack", connection);
                group.add(b1);
                group.add(b2);
                group.add(b3);
                group.add(b4);
                for (int i=0; i<REPETITIONS; i++) {
                    dataModel.statusChanged(connection, b1, b1.getStatus());
//                    yield();
                    dataModel.statusChanged(connection, b2, b1.getStatus());
//                    yield();

                    dataModel.statusChanged(connection, b1, b1.getStatus());
//                    yield();
                    dataModel.statusChanged(connection, b2, b1.getStatus());
//                    yield();

                    dataModel.statusChanged(connection, b1, b1.getStatus());
//                    yield();
                    dataModel.statusChanged(connection, b2, b1.getStatus());
//                    yield();

                    dataModel.statusChanged(connection, b1, b1.getStatus());
//                    yield();
                    dataModel.statusChanged(connection, b2, b1.getStatus());
//                    yield();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "", e);  //Todo change
            }
        }
    }
}
