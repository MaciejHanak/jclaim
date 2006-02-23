package com.itbs.aimcer.gui;

import javax.swing.*;

/**
 * Created by: ARass  on  Date: Sep 24, 2004
 */
public class UTestPropertiesDialog extends UTestFrameTest {
    JFrame frame;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    public void setUp() throws Exception {
        super.setUp();
        Main.loadProperties();
        JLabel label = new JLabel();
        label.setText("mo");
        label.setText("<HTML>bo<u>b</u></HTML>");
        add(label);
    }

    public void testDialog() throws Exception {
        window.setVisible(true);

        JDialog dialog = new PropertiesDialog(frame);
        dialog.setVisible(true);
    }
}
