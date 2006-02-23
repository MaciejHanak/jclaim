package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.GroupWrapper;
import com.itbs.aimcer.bean.Status;
import com.itbs.aimcer.commune.Connection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by  Administrator on Sep 11, 2004
 */
public class UTestContactListModel extends UTestFrameTest {
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

    static class FakeStatus implements Status {
        boolean online;
        boolean away;
        public FakeStatus(boolean online) { this.online = online; }
        public boolean isOnline() { return online; }
        public boolean isAway() { return away; }
        public int getTimeAway() { return 0; }
        public Contact getParent() { return null; }
        public void setOnline(boolean online) { }
        public void setAway(boolean away) { }
        public boolean isWireless() { return false; }
        public void setWireless(boolean wireless) { }
        public void setIdleTime(int idleMins) { }
        public int getIdleTime() { return 0; }
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
                    dataModel.statusChanged(connection, b1, false, false, 1);
//                    yield();
                    dataModel.statusChanged(connection, b2, false, false, 1);
//                    yield();

                    dataModel.statusChanged(connection, b1, false, false, 1);
//                    yield();
                    dataModel.statusChanged(connection, b2, true, false, 1);
//                    yield();

                    dataModel.statusChanged(connection, b1, true, false, 1);
//                    yield();
                    dataModel.statusChanged(connection, b2, false, false, 1);
//                    yield();

                    dataModel.statusChanged(connection, b1, true, false, 1);
//                    yield();
                    dataModel.statusChanged(connection, b2, true, false, 1);
//                    yield();
                }
            } catch (Exception e) {
                e.printStackTrace();  //Todo change
            }
        }
    }
}
