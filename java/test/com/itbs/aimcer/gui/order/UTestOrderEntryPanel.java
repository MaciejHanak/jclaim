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
