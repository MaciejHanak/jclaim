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
